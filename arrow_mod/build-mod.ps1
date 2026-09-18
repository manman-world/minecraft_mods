$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$jdk = Get-ChildItem "$PSScriptRoot/.tools/java" -Directory | Select-Object -First 1
if (-not $jdk) { throw 'Java 25 is missing from .tools/java.' }
$env:JAVA_HOME = $jdk.FullName
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
$env:GRADLE_USER_HOME = "$PSScriptRoot/.tools/gradle-cache"
& "$PSScriptRoot/gradlew.bat" build --no-daemon
exit $LASTEXITCODE
