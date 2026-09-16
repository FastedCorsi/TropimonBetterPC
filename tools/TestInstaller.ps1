# By FastedCorsi. Offline regression tests; only generated fixture instances are modified.
$ErrorActionPreference = 'Stop'
Import-Module (Join-Path $PSHOME 'Modules/Microsoft.PowerShell.Utility') -Force
Import-Module (Join-Path $PSHOME 'Modules/CimCmdlets') -Force
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$fixture = Join-Path $project ('build/installer-test-' + [Guid]::NewGuid().ToString('N'))
$instance = Join-Path $fixture 'instance'
$mods = Join-Path $instance 'mods'
$sources = Join-Path $fixture 'sources'
New-Item -ItemType Directory -Force $mods, $sources | Out-Null
$installer = Join-Path $PSScriptRoot 'InstallWhenClosed.ps1'

function Assert-Test([bool]$Condition, [string]$Message) {
    if (-not $Condition) { throw $Message }
    Write-Output "INSTALLER_CHECK: $Message"
}

function New-FixtureJar([string]$Version) {
    $path = Join-Path $sources ("fixture-$Version.jar")
    $zip = [IO.Compression.ZipFile]::Open($path, [IO.Compression.ZipArchiveMode]::Create)
    try {
        $entry = $zip.CreateEntry('fabric.mod.json')
        $writer = [IO.StreamWriter]::new($entry.Open())
        try {
            $writer.Write((@{ id = 'tropimon_better_pc'; version = $Version; authors = @('By FastedCorsi') } | ConvertTo-Json))
        } finally { $writer.Dispose() }
    } finally { $zip.Dispose() }
    return $path
}

function Install-Fixture([string]$Jar, [string]$ExpectedState) {
    $hash = (Get-FileHash -LiteralPath $Jar -Algorithm SHA256).Hash
    & $installer -Source $Jar -ExpectedHash $hash -LauncherDirectory $instance -Once
    $state = (Get-Content -LiteralPath (Join-Path $sources 'install-status.json') -Raw | ConvertFrom-Json).state
    Assert-Test ($state -eq $ExpectedState) "Expected installer state: $ExpectedState"
}

$older = New-FixtureJar '0.9.2'
$newer = New-FixtureJar '0.9.4'
$late = New-FixtureJar '0.9.3'
Install-Fixture $older 'installed-verified'
Install-Fixture $newer 'installed-verified'
$target = Join-Path $mods 'TropimonBetterPC-0.9.4+1.21.1-LOCAL.jar'
$hash = (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash
$backup = Join-Path $instance 'mod-archive/TropimonBetterPC'
$backupCount = @(Get-ChildItem -LiteralPath $backup -Filter '*.bak').Count
Install-Fixture $late 'skipped-newer-installed'
Assert-Test ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -eq $hash) 'Late older worker keeps newer JAR unchanged'
Assert-Test (@(Get-ChildItem -LiteralPath $mods -Filter '*.jar').Count -eq 1) 'Only one mod JAR remains loaded'
Assert-Test (@(Get-ChildItem -LiteralPath $backup -Filter '*.bak').Count -eq $backupCount) 'Skipped downgrade does not move or back up the newer JAR'
Install-Fixture $newer 'installed-verified'
Assert-Test (@(Get-ChildItem -LiteralPath $backup -Filter '*.bak').Count -eq $backupCount) 'Installing the same verified JAR is idempotent'
Assert-Test ($backupCount -eq 1) 'Upgrade retains the old JAR outside loaded mods'

# Unknown version ordering must also preserve the installed artifact.
$unknown = New-FixtureJar 'development'
Copy-Item -LiteralPath $unknown -Destination $target -Force
$unknownHash = (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash
$rejected = $false
try { Install-Fixture $newer 'blocked-review-required' } catch { $rejected = $_.Exception.Message -like 'Ordre des versions indeterminable*' }
Assert-Test $rejected 'Unorderable installed version prevents replacement'
Assert-Test ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -eq $unknownHash) 'Unorderable version stays byte-identical'
Write-Output 'INSTALLER_TESTS_OK'
