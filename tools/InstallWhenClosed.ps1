param(
    [Parameter(Mandatory = $true)][string]$Source,
    [Parameter(Mandatory = $true)][string]$ExpectedHash,
    [string]$LauncherDirectory = $env:TROPIMON_HOME,
    [switch]$Once
)

# By FastedCorsi. Installateur local externe, absent du JAR partageable.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$sourceFile = (Resolve-Path -LiteralPath $Source).Path
$statusFile = Join-Path (Split-Path -Parent $sourceFile) 'install-status.json'
$version = $null

function Set-InstallStatus([string]$State) {
    $status = @{ state = $State; version = $version; updatedUtc = [DateTime]::UtcNow.ToString('o') } | ConvertTo-Json
    # The previous worker owns the shared status until its lock is released.
    if ($State -ne 'waiting-for-previous-update') {
        $status | Set-Content -LiteralPath $statusFile -Encoding UTF8
    }
    if ($version) {
        $status | Set-Content -LiteralPath (Join-Path (Split-Path -Parent $sourceFile) "install-status-$version.json") -Encoding UTF8
    }
}

function Read-Mod([string]$Path, [switch]$FullCheck) {
    $zip = [IO.Compression.ZipFile]::OpenRead($Path)
    try {
        if ($FullCheck) { foreach ($entry in $zip.Entries) {
            $stream = $entry.Open()
            try { $stream.CopyTo([IO.Stream]::Null) } finally { $stream.Dispose() }
        } }
        $entry = $zip.GetEntry('fabric.mod.json')
        if ($null -eq $entry) { throw 'Metadonnees absentes.' }
        $reader = [IO.StreamReader]::new($entry.Open())
        try { return $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
    } finally { $zip.Dispose() }
}

function Assert-Release([string]$Path) {
    $stream = [IO.File]::OpenRead($Path)
    try {
        $sha = [Security.Cryptography.SHA256]::Create()
        try { $actual = ([BitConverter]::ToString($sha.ComputeHash($stream))).Replace('-', '') }
        finally { $sha.Dispose() }
    } finally { $stream.Dispose() }
    if ($actual -ne $ExpectedHash) { throw 'Integrite inattendue.' }
    $metadata = Read-Mod $Path -FullCheck
    if ($metadata.id -ne 'tropimon_better_pc' -or @($metadata.authors).Count -ne 1 -or
            $metadata.authors[0] -cne 'By FastedCorsi' -or $metadata.version -notmatch '^[0-9A-Za-z.+_-]+$') {
        throw 'Identite du mod inattendue.'
    }
    return $metadata.version
}

function Test-GameBusy {
    foreach ($process in Get-CimInstance Win32_Process) {
        if ($process.Name -notmatch '^java(w)?\.exe$') { continue }
        if (-not $process.CommandLine) { return $true }
        if ($process.CommandLine -match '(?i)--gameDir(?:=|\s+)(?:"([^"]+)"|([^\s]+))') {
            $gameDirectory = if ($Matches[1]) { $Matches[1] } else { $Matches[2] }
            try {
                if (-not [IO.Path]::IsPathRooted($gameDirectory)) { return $true }
                $gameDirectory = [IO.Path]::GetFullPath($gameDirectory).TrimEnd('\', '/')
                if ($gameDirectory -eq $instance.TrimEnd('\', '/')) { return $true }
            } catch { return $true }
            continue
        }
        if ($process.CommandLine -match '(?i)KnotClient|net\.minecraft\.client\.main\.Main|@.*args') { return $true }
    }
    return $false
}

$lock = $null
$stage = $null
try {
    $version = Assert-Release $sourceFile
    if (-not $LauncherDirectory) { $LauncherDirectory = Join-Path $env:APPDATA '.tropimon' }
    $instance = (Resolve-Path -LiteralPath $LauncherDirectory).Path
    $mods = (Resolve-Path -LiteralPath (Join-Path $instance 'mods')).Path
    if ((Get-Item -LiteralPath $mods).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Dossier mods redirige.' }
    if ($sourceFile.StartsWith($mods + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Source dans les mods charges.' }
    $backupDirectory = Join-Path $instance 'mod-archive\TropimonBetterPC'
    New-Item -ItemType Directory -Force -Path $backupDirectory | Out-Null
    while ($null -eq $lock) {
        try {
            $lock = [IO.File]::Open((Join-Path $backupDirectory 'install.lock'),
                [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
        } catch [IO.IOException] {
            if (($_.Exception.HResult -band 0xFFFF) -notin @(32, 33)) { throw }
            Set-InstallStatus 'waiting-for-previous-update'
            if ($Once) { return }
            Start-Sleep -Seconds 5
        }
    }
    $target = Join-Path $mods "TropimonBetterPC-$version+1.21.1-LOCAL.jar"

    while (Test-GameBusy) {
        Set-InstallStatus 'waiting-for-game'
        if ($Once) { return }
        Start-Sleep -Seconds 5
    }

    $installed = @(Get-ChildItem -LiteralPath $mods -Filter '*.jar' -File | Where-Object {
        try { (Read-Mod $_.FullName).id -eq 'tropimon_better_pc' } catch { $false }
    })
    if ($installed.Count -gt 1) { throw 'Plusieurs JAR Tropimon Better PC sont charges.' }
    # A filesystem lock serializes workers but does not guarantee their order.
    # An older queued worker must never replace a newer release installed before it.
    if ($installed.Count -eq 1) {
        $currentMetadata = Read-Mod $installed[0].FullName
        $currentVersion = $null
        $requestedVersion = $null
        if (-not [Version]::TryParse(([string]$currentMetadata.version -split '\+', 2)[0], [ref]$currentVersion) -or
                -not [Version]::TryParse(($version -split '\+', 2)[0], [ref]$requestedVersion)) {
            throw 'Ordre des versions indeterminable ; installation existante conservee.'
        }
        if ($currentVersion -gt $requestedVersion) {
            Set-InstallStatus 'skipped-newer-installed'
            return
        }
    }
    if ((Test-Path -LiteralPath $target) -and
            ($installed.Count -ne 1 -or $installed[0].FullName -ne $target)) {
        throw 'Le nom cible est deja utilise par un autre fichier.'
    }
    if ($installed.Count -eq 1 -and $installed[0].FullName -eq $target -and
            (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash -eq $ExpectedHash) {
        Set-InstallStatus 'installed-verified'
        return
    }

    $stage = Join-Path $mods ('.tropimon-better-pc-' + [Guid]::NewGuid().ToString('N') + '.pending')
    Copy-Item -LiteralPath $sourceFile -Destination $stage
    $null = Assert-Release $stage
    if (Test-GameBusy) { throw 'Minecraft a redemarre pendant la preparation.' }
    $old = if ($installed.Count -eq 1) { $installed[0].FullName } else { $null }
    $backup = $null
    if ($old) {
        $probe = [IO.File]::Open($old, [IO.FileMode]::Open, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
        $probe.Dispose()
        $backup = Join-Path $backupDirectory ((Split-Path -Leaf $old) + '.' + [Guid]::NewGuid().ToString('N') + '.bak')
        Move-Item -LiteralPath $old -Destination $backup
    }
    try {
        Move-Item -LiteralPath $stage -Destination $target
        $stage = $null
        $null = Assert-Release $target
    } catch {
        if (Test-Path -LiteralPath $target) {
            Move-Item -LiteralPath $target -Destination (Join-Path $backupDirectory ([Guid]::NewGuid().ToString('N') + '.failed'))
        }
        if ($backup) { Move-Item -LiteralPath $backup -Destination $old }
        throw
    }
    Set-InstallStatus 'installed-verified'
} catch {
    Set-InstallStatus 'blocked-review-required'
    throw
} finally {
    if ($stage -and (Test-Path -LiteralPath $stage)) { Remove-Item -LiteralPath $stage -Force }
    if ($lock) { $lock.Dispose() }
}
