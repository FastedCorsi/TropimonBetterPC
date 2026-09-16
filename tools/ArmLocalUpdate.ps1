param([Parameter(Mandatory = $true)][string]$Source)

# By FastedCorsi. Arme l'installation externe ; le launcher peut rester ouvert.
$ErrorActionPreference = 'Stop'
$sourceFile = (Resolve-Path -LiteralPath $Source).Path
$installer = Join-Path (Split-Path -Parent $sourceFile) 'InstallWhenClosed.ps1'
if (-not (Test-Path -LiteralPath $installer -PathType Leaf)) { throw 'Installateur differe introuvable.' }
$stream = [IO.File]::OpenRead($sourceFile)
try {
    $sha = [Security.Cryptography.SHA256]::Create()
    try { $hash = ([BitConverter]::ToString($sha.ComputeHash($stream))).Replace('-', '') }
    finally { $sha.Dispose() }
} finally { $stream.Dispose() }
$escapedInstaller = $installer.Replace("'", "''")
$escapedSource = $sourceFile.Replace("'", "''")
$command = "& '$escapedInstaller' -Source '$escapedSource' -ExpectedHash '$hash'"
$encoded = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($command))
Start-Process -FilePath 'powershell' -WindowStyle Hidden -ArgumentList @(
    '-NoProfile', '-ExecutionPolicy', 'Bypass', '-EncodedCommand', $encoded
) | Out-Null
Write-Output 'Mise a jour locale armee ; elle attendra la fermeture de Minecraft.'
