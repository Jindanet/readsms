$ErrorActionPreference = "Stop"

$androidDir = Join-Path $PSScriptRoot "android"
$gradlew = Join-Path $androidDir "gradlew.bat"
$androidStudioJbr = "C:\Program Files\Android\Android Studio\jbr"

if (-not (Test-Path -LiteralPath $gradlew)) {
    throw "Missing Android Gradle wrapper: $gradlew"
}

if (-not (Get-Command java -ErrorAction SilentlyContinue) -and (Test-Path -LiteralPath (Join-Path $androidStudioJbr "bin\java.exe"))) {
    $env:JAVA_HOME = $androidStudioJbr
    $env:Path = "$androidStudioJbr\bin;$env:Path"
}

Push-Location $androidDir
try {
    & $gradlew assembleDebug
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }
    $apk = Join-Path $androidDir "app\build\outputs\apk\debug\app-debug.apk"
    if (-not (Test-Path -LiteralPath $apk)) {
        throw "Build finished but APK was not found: $apk"
    }
    Write-Host ""
    Write-Host "APK built:"
    Write-Host $apk
} finally {
    Pop-Location
}
