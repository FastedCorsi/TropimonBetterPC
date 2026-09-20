param([Parameter(Mandatory = $true)][string]$Source)

# By FastedCorsi. Arme l'installation externe ; le launcher peut rester ouvert.
$ErrorActionPreference = 'Stop'
$sourceFile = (Resolve-Path -LiteralPath $Source).Path
$installer = Join-Path (Split-Path -Parent $sourceFile) 'InstallManagedLocalMod.ps1'
if (-not (Test-Path -LiteralPath $installer -PathType Leaf)) { throw 'Installateur differe introuvable.' }
$logFile = $sourceFile + '.install.log'
$escapedInstaller = $installer.Replace("'", "''")
$escapedSource = $sourceFile.Replace("'", "''")
$escapedLog = $logFile.Replace("'", "''")
$command = "& '$escapedInstaller' -SourceJar '$escapedSource' -ExpectedModId 'tropimon_better_pc' *> '$escapedLog'"
$encoded = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($command))
Start-Process -FilePath 'powershell' -WindowStyle Hidden -ArgumentList @(
    '-NoProfile', '-ExecutionPolicy', 'Bypass', '-EncodedCommand', $encoded
) | Out-Null
Write-Output 'Mise a jour locale armee ; elle attendra la fermeture de Minecraft.'
