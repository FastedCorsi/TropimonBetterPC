param([Parameter(Mandatory = $true)][string]$Source)

# By FastedCorsi. Isolated copy/lock checks; never targets the real launcher instance.
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$fixture = Join-Path $project ('build/installer-verification-' + [Guid]::NewGuid().ToString('N'))
$mods = Join-Path $fixture 'mods'
$archive = Join-Path $fixture 'mod-archive/TropimonBetterPC'
$staging = Join-Path $fixture 'staging'
New-Item -ItemType Directory -Force $mods, $archive, $staging | Out-Null
$prepared = Join-Path $staging 'prepared.jar'
Copy-Item -LiteralPath (Resolve-Path -LiteralPath $Source).Path -Destination $prepared
$hash = (Get-FileHash -LiteralPath $prepared -Algorithm SHA256).Hash
$previous = Join-Path $mods 'previous.jar'
Copy-Item -LiteralPath $prepared -Destination $previous
$lock = [IO.File]::Open((Join-Path $archive 'install.lock'), [IO.FileMode]::OpenOrCreate,
    [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
try {
    & (Join-Path $PSScriptRoot 'InstallWhenClosed.ps1') -Source $prepared -ExpectedHash $hash -LauncherDirectory $fixture -Once
    $versionStatus = Get-ChildItem -LiteralPath $staging -Filter 'install-status-*.json' | Select-Object -First 1
    $status = Get-Content -LiteralPath $versionStatus.FullName -Raw | ConvertFrom-Json
    if ($status.state -ne 'waiting-for-previous-update') { throw 'Busy installer was not queued.' }
    if (!(Test-Path -LiteralPath $previous)) { throw 'Busy installer modified the target.' }
} finally { $lock.Dispose() }
& (Join-Path $PSScriptRoot 'InstallWhenClosed.ps1') -Source $prepared -ExpectedHash $hash -LauncherDirectory $fixture -Once
$status = Get-Content -LiteralPath (Join-Path $staging 'install-status.json') -Raw | ConvertFrom-Json
if ($status.state -ne 'installed-verified') { throw 'Isolated installation did not complete.' }
$installed = @(Get-ChildItem -LiteralPath $mods -Filter '*.jar')
$backups = @(Get-ChildItem -LiteralPath $archive -Filter '*.bak')
if ($installed.Count -ne 1 -or $backups.Count -ne 1) { throw 'Wrong number of installed or backed-up files.' }
foreach ($file in @($installed[0], $backups[0])) {
    if ((Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash -ne $hash) { throw 'Copy integrity mismatch.' }
}
if (!(Test-Path -LiteralPath (Join-Path $staging "install-status-$($status.version).json"))) { throw 'Version-specific status missing.' }
Write-Output 'Installer verified: queued lock, preserved old file, one installed JAR, backup and hashes.'
