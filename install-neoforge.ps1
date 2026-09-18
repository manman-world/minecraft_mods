$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$jdk = Get-ChildItem "$PSScriptRoot/.tools/java" -Directory | Select-Object -First 1
if (-not $jdk) { throw 'Java 25 is missing from .tools/java.' }
& "$($jdk.FullName)/bin/java.exe" -jar "$PSScriptRoot/installers/neoforge-26.2.0.82-installer.jar"
exit $LASTEXITCODE
