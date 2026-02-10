# Upload a release to GitHub (manual path alongside GitHub Actions).
# Prereqs: GitHub CLI installed (winget install GitHub.cli) and logged in (gh auth login).
# Run from repo root.
#
# Usage:
#   .\scripts\upload-release.ps1 -Tag beta/v0.3.1
#   .\scripts\upload-release.ps1 -Tag v0.4 -Prerelease:$false
# Prerelease defaults to $true when Tag starts with "beta/", else $false.

param(
    [Parameter(Mandatory = $true)]
    [string] $Tag,

    [Parameter(Mandatory = $false)]
    [switch] $Prerelease
)

$ErrorActionPreference = "Stop"
$apk = "builds\app\outputs\apk\release\app-release.apk"

# Default prerelease from tag: beta/v* -> prerelease
if (-not $PSBoundParameters.ContainsKey("Prerelease")) {
    $Prerelease = $Tag -match "^beta/"
}

if (-not (Test-Path $apk)) {
    Write-Host "APK not found. Building..."
    & .\gradlew.bat assembleRelease
    if (-not (Test-Path $apk)) {
        Write-Error "APK still not found at $apk"
    }
}

# Push tag if not already pushed
git push origin $Tag 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Push tag first: git push origin $Tag"
    exit 1
}

$releaseNotes = "See CHANGELOG.md for release notes."

if ($Prerelease) {
    $result = gh release create $Tag $apk --title $Tag --notes $releaseNotes --prerelease 2>&1
} else {
    $result = gh release create $Tag $apk --title $Tag --notes $releaseNotes 2>&1
}
if ($LASTEXITCODE -eq 0) {
    Write-Host "Release $Tag created and APK uploaded."
} else {
    # If release already exists, upload asset only
    gh release upload $Tag $apk --clobber
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Release $Tag already existed; APK uploaded (clobbered)."
    } else {
        Write-Error $result
    }
}
