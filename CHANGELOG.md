# Changelog

All notable changes to PrivDNS Toggle will be documented in this file.

## [0.3.2] - YYYY-MM-DD

### Changed / Fixed / Added
- Version update to bring into alignment all version details.

## [0.3.1] - 2026-02-09

### Changed

- **Version source** — Version is now read from `version.properties` (single source of truth for release automation).
- **Release workflow** — Tag-based GitHub Action: `beta/v*` publishes a prerelease, `v*` from main publishes a full release.

### Fixed

- **Compose BOM** — Build uses Compose BOM 2023.10.01 to avoid KeyframesSpec NoSuchMethodError on device (fixes crash after first frame on some devices).

## [0.3] - 2026-02-09

### Fixed

- **Release APK signing** — release builds are now signed (keystore or debug fallback), fixing "App not installed as package appears to be invalid" on devices such as Samsung Galaxy S21+.

## [0.2-beta] - 2026-02-09

First public beta release.

### Added

- **Quick Settings Tile** — toggle Private DNS on/off directly from the notification shade.
- **Large Toggle Switch** — prominent horizontal switch in the app with animated state transitions.
- **DNS Validation** — syntax validation for hostnames, IPv4, and IPv6 addresses, plus a TLS connection test (port 853, 10s timeout) before saving.
- **Dark Mode UI** — Material 3 dark theme by default, with dynamic color support on Android 12+.
- **In-App Setup Instructions** — collapsible card with the ADB permission grant command and copy-to-clipboard.
- **Custom App Icons** — launcher icons, adaptive icons, and Quick Settings tile icon generated from project branding.
- **Unit Tests** — hostname validation covered by JUnit 4 tests.

### Technical Details

- Min SDK: 28 (Android 9)
- Target SDK: 34
- Requires one-time ADB grant of `WRITE_SECURE_SETTINGS`
- No root, no Device Admin, no background services, no analytics
