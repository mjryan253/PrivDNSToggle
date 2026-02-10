# Upload v0.3 release to GitHub (beta/pre-release).
# Prereqs: GitHub CLI installed (winget install GitHub.cli) and logged in (gh auth login).
# Run from repo root.

$ErrorActionPreference = "Stop"
$apk = "builds\app\outputs\apk\release\app-release.apk"
$apkName = "PrivDNSToggle-v0.3.apk"

if (-not (Test-Path $apk)) {
    Write-Error "APK not found. Build first: .\gradlew.bat assembleRelease"
}

# Push tag if not already pushed
git push origin v0.3 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Push tag first: git push origin v0.3"
    exit 1
}

# Create release and upload APK (--prerelease = beta)
gh release create v0.3 $apk --title "v0.3" --notes "Signed release APK. Fixes install on Samsung and other devices (package appears to be invalid)." --prerelease

if ($LASTEXITCODE -eq 0) {
    Write-Host "Release v0.3 created and APK uploaded."
} else {
    # If release already exists, upload asset only
    gh release upload v0.3 $apk --clobber
}
