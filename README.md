# PrivDNS Toggle

**Repository:** [https://github.com/mjryan253/PrivDNSToggle](https://github.com/mjryan253/PrivDNSToggle)

A minimal Android app to toggle Private DNS on and off — from the Quick Settings shade or the app itself.

## Features

- **Quick Settings Tile** — pull down notification shade, tap to toggle Private DNS on/off
- **Large Toggle Switch** — prominent horizontal switch in the app for easy on/off control
- **DNS Connection Testing** — connection testing (10s timeout) before saving (syntax validation temporarily disabled)
- **Dark Mode UI** — modern dark theme interface by default
- **Lightweight** — no background services, no analytics
- **Shizuku Support** - grant the required permission in-app via Shizuku, no computer needed

## Setup

### 1. Build & Install

Open the project in Android Studio, let Gradle sync, then **Run** on your device (or build the APK via `Build > Build Bundle(s) / APK(s) > Build APK(s)`).

### 2. Grant Permission (required, one-time)

This app writes to Android's Private DNS system setting, which requires the `WRITE_SECURE_SETTINGS` permission. Android does not allow this to be granted through a normal permission dialog — it must be granted via ADB, or via Shizuku (see below).

**Steps:**

1. Enable **USB Debugging** on your phone (Settings > Developer Options)
2. Connect your phone to your computer via USB
3. Run this command:

```
adb shell pm grant com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS
```

That's it. This permission **persists across app updates** — you only need to do it once. Uninstalling the app revokes it automatically.

> **Don't have ADB?** The primary intended option is **[Minimal ADB and Fastboot](https://minimaladbandfastboot.com/)** — a small Windows installer that gives you just the bare minimum needed to connect your phone via USB and run the permission command above. 


*Alternatively*, the full ADB suite is included when you install **[Android Studio](https://developer.android.com/studio)**; use the SDK’s `platform-tools` (or the terminal inside Android Studio) to run `adb`.

> **No computer?** If [Shizuku](https://shizuku.rikka.app) is installed and running on the phone, open the app, expand **Setup Instructions**, and tap **Grant with Shizuku**. This runs the same grant on-device. See [quick-start.md](quick-start.md#43-alternative-grant-via-shizuku-no-computer-needed) for the steps.

### 3. Add the Quick Settings Tile

1. Pull down the notification shade fully
2. Tap the pencil/edit icon to edit tiles
3. Find **Private DNS** and drag it into your active tiles

## Usage

- **Quick Settings Tile**: Tap to toggle between your saved DNS hostname and off
- **App**: 
  - Use the large toggle switch at the top to turn Private DNS on/off
  - Enter your DNS provider hostname or IP address (e.g., `dns.nextdns.io`, `one.one.one.one`)
  - Tap **Save** to test the connection (10s timeout), save the hostname, and enable Private DNS with that hostname
  - View current status and active DNS hostname

## How It Works

The app reads and writes two `Settings.Global` values:

| Key | Values |
|-----|--------|
| `private_dns_mode` | `"off"`, `"opportunistic"`, `"hostname"` |
| `private_dns_specifier` | The DNS hostname (e.g. `dns.adguard.com`) |

When toggled **on**, the mode is set to `"hostname"` with your saved specifier. When toggled **off**, the mode is set to `"off"`.

## Documentation

- **[quick-start.md](quick-start.md)** — Step-by-step build and run on a physical device.
- **docs/** — Additional docs:
  - [testing](docs/testing.md) — Unit tests and how to run them
  - [debugging-with-logs](docs/debugging-with-logs.md) — Using the built-in debug menu to diagnose issues
  - [troubleshooting](docs/troubleshooting.md) — Common issues and solutions
  - [original design concept](docs/original-plan-idea.md) — Initial design notes
  - [agent history](agent/agent-history.md) — Development history

## Tests

See **[docs/testing.md](docs/testing.md)** for what is tested, how to run unit tests (Android Studio and command line), and how to add or change tests.

**Test coverage:**
- Hostname/IP validation logic (`DnsManagerTest.kt`)
- Debug logging system (`DebugLoggerTest.kt`)

## Requirements

- Android 9+ (API 28)
- One-time permission grant via ADB or Shizuku
- Internet connection (for DNS connection testing during setup)
