$ErrorActionPreference = "Stop"

$packageName = "com.readsms.app"
$adbCandidates = @(
    (Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"),
    "adb"
)

$adb = $null
foreach ($candidate in $adbCandidates) {
    if ($candidate -eq "adb") {
        $command = Get-Command adb -ErrorAction SilentlyContinue
        if ($command) {
            $adb = $command.Source
            break
        }
    } elseif (Test-Path -LiteralPath $candidate) {
        $adb = $candidate
        break
    }
}

if (-not $adb) {
    throw "adb not found. Install Android platform-tools or open Android Studio once."
}

Write-Host "Using adb: $adb"
& $adb devices

& $adb shell pm grant $packageName android.permission.READ_SMS
& $adb shell pm grant $packageName android.permission.RECEIVE_SMS

# Some Xiaomi/POCO builds keep an AppOps gate behind the runtime permission.
& $adb shell cmd appops set $packageName READ_SMS allow 2>$null
& $adb shell cmd appops set $packageName RECEIVE_SMS allow 2>$null

Write-Host ""
Write-Host "Done. Reopen ReadSMS and check Collector > SMS access."
