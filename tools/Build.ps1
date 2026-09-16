param([switch]$Deliver, [switch]$Arm, [switch]$Smoke)
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$launcher = if ($env:TROPIMON_HOME) { $env:TROPIMON_HOME } else { Join-Path $env:APPDATA '.tropimon' }
$jdk = Join-Path $launcher 'runtime/x64/jdk-21.0.6+7'
if (Test-Path -LiteralPath (Join-Path $jdk 'bin/java.exe')) {
    $env:JAVA_HOME = $jdk
    $env:Path = (Join-Path $jdk 'bin') + [IO.Path]::PathSeparator + $env:Path
}
$tasks = @('build')
if ($Smoke) { $tasks = @('build', 'remapSmokeJar') }
if ($Deliver) { $tasks += 'prepareDelivery' }
if ($Arm) { $tasks += 'armBetterPcLocal' }
& (Join-Path $project 'gradlew.bat') -p $project @tasks --console=plain
if ($LASTEXITCODE -eq 0 -and $Smoke) { & (Join-Path $PSScriptRoot 'VerifyClient.ps1') }
exit $LASTEXITCODE
