# PrivDNS Toggle

A minimal Android app to toggle Private DNS on and off — from the Quick Settings shade or the app itself.

## Features

- **Quick Settings Tile** — pull down notification shade, tap to toggle Private DNS on/off
- **Simple UI** — enter your DNS hostname or IP, save, and apply in one tap
- **Lightweight** — no background services, no internet permission, no analytics

## Setup

### 1. Build & Install

Open the project in Android Studio, let Gradle sync, then **Run** on your device (or build the APK via `Build > Build Bundle(s) / APK(s) > Build APK(s)`).

### 2. Grant Permission (required, one-time)

This app writes to Android's Private DNS system setting, which requires the `WRITE_SECURE_SETTINGS` permission. Android does not allow this to be granted through a normal permission dialog — it must be granted via ADB.

**Steps:**

1. Enable **USB Debugging** on your phone (Settings > Developer Options)
2. Connect your phone to your computer via USB
3. Run this command:

```
adb shell pm grant com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS
```

That's it. This permission **persists across app updates** — you only need to do it once. Uninstalling the app revokes it automatically.

> **Don't have ADB?** Install it from [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools). On Windows, extract the zip and run the command from that folder.

### 3. Add the Quick Settings Tile

1. Pull down the notification shade fully
2. Tap the pencil/edit icon to edit tiles
3. Find **Private DNS** and drag it into your active tiles

## Usage

- **Quick Settings Tile**: Tap to toggle between your saved DNS hostname and off
- **App**: Open to change the DNS hostname/IP, view current status, or toggle with the switch

## How It Works

The app reads and writes two `Settings.Global` values:

| Key | Values |
|-----|--------|
| `private_dns_mode` | `"off"`, `"opportunistic"`, `"hostname"` |
| `private_dns_specifier` | The DNS hostname (e.g. `dns.adguard.com`) |

When toggled **on**, the mode is set to `"hostname"` with your saved specifier. When toggled **off**, the mode is set to `"off"`.

## Requirements

- Android 9+ (API 28)
- One-time ADB command for permission grant
