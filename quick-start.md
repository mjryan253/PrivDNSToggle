# Quick Start Guide

Get PrivDNS Toggle built and running on your physical Android device in minutes.

## Prerequisites

Before you begin, ensure you have:

- **Android Studio** (latest stable version) - [Download here](https://developer.android.com/studio)
- **Android SDK Platform Tools** (for ADB) - Usually included with Android Studio, or [download separately](https://developer.android.com/tools/releases/platform-tools)
- **Physical Android device** running Android 9+ (API 28 or higher)
- **USB cable** to connect your device to your computer

## Step 1: Initial Setup

### 1.1 Get the Project

If you haven't already, clone or download this repository:

```bash
git clone https://github.com/mjryan253/PrivDNSToggle
cd PrivDNSToggle
```

### 1.2 Open in Android Studio

1. Launch **Android Studio**
2. Select **File > Open** (or **Open** from the welcome screen)
3. Navigate to and select the `PrivDNSToggle` folder
4. Click **OK**

### 1.3 Wait for Gradle Sync

Android Studio will automatically start syncing Gradle dependencies. Wait for the sync to complete (you'll see "Gradle sync finished" in the status bar at the bottom).

**First time setup?** Android Studio may download Gradle 8.4 and required dependencies. This can take several minutes depending on your internet connection.

## Step 2: Prepare Your Android Device

### 2.1 Enable Developer Options

1. Open **Settings** on your Android device
2. Navigate to **About phone** (or **About device**)
3. Find **Build number** and tap it **7 times**
4. You'll see a message: "You are now a developer!"

### 2.2 Enable USB Debugging

1. Go back to **Settings**
2. Navigate to **Developer options** (usually under **System**)
3. Toggle **USB debugging** to **ON**
4. When prompted, tap **OK** to confirm

### 2.3 Connect Your Device

1. Connect your Android device to your computer via USB cable
2. On your device, you may see a prompt: **"Allow USB debugging?"**
   - Check **"Always allow from this computer"** (optional but recommended)
   - Tap **Allow**

### 2.4 Verify Device Connection

Open a terminal/command prompt and run:

```bash
adb devices
```

**Windows users:** If `adb` is not recognized, you may need to:
- Use the full path: `C:\Users\<YourUsername>\AppData\Local\Android\Sdk\platform-tools\adb.exe devices`
- Or add the platform-tools folder to your PATH environment variable

**Expected output:**
```
List of devices attached
XXXXXXXX    device
```

If you see `unauthorized`, check your device screen for the USB debugging prompt and tap **Allow**.

## Step 3: Build & Install

### Option A: Run Directly from Android Studio (Recommended)

1. In Android Studio, ensure your device is selected in the device dropdown (top toolbar)
2. Click the **Run** button (green play icon) or press `Shift+F10`
3. Android Studio will build and install the app automatically
4. The app will launch on your device

### Option B: Build APK and Install Manually

1. In Android Studio, go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**
2. Wait for the build to complete
3. When finished, click **locate** in the notification, or navigate to:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
4. Transfer the APK to your device (via USB, email, or cloud storage)
5. On your device, enable **Install from unknown sources** if prompted
6. Open the APK file and tap **Install**

### Verify Installation

Check that **PrivDNS Toggle** appears in your app drawer. You can open it, but it won't work yet until you grant the required permission in the next step.

## Step 4: Grant Required Permission

This app needs the `WRITE_SECURE_SETTINGS` permission to toggle Private DNS. Android requires this to be granted via ADB (it cannot be granted through a normal permission dialog).

### 4.1 Grant Permission via ADB

With your device still connected via USB, run this command in your terminal:

```bash
adb shell pm grant com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS
```

**Windows users:** If `adb` is not in your PATH, use the full path:
```powershell
C:\Users\<YourUsername>\AppData\Local\Android\Sdk\platform-tools\adb.exe shell pm grant com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS
```

**Expected output:** No error message means success!

### 4.2 Verify Permission

You can verify the permission was granted by running:

```bash
adb shell dumpsys package com.privdnstoggle.app | findstr WRITE_SECURE_SETTINGS
```

**Windows users:** Use `findstr` instead of `grep`:
```powershell
adb shell dumpsys package com.privdnstoggle.app | findstr WRITE_SECURE_SETTINGS
```

You should see the permission listed.

**Important:** This permission **persists across app updates** — you only need to grant it once. Uninstalling the app will revoke it automatically.

## Step 5: Configure Quick Settings Tile

### 5.1 Add the Tile

1. Pull down the notification shade **fully** (swipe down twice)
2. Look for a **pencil/edit icon** (usually in the bottom right or top right)
3. Tap it to enter **Edit tiles** mode
4. Scroll through available tiles and find **Private DNS**
5. **Drag** the Private DNS tile into your active tiles area
6. Tap **Done** or the back arrow to save

### 5.2 Configure DNS (First Time)

1. Open the **PrivDNS Toggle** app from your app drawer
2. You'll see a large toggle switch at the top (currently OFF)
3. Enter your desired DNS hostname or IP address in the input field below (e.g., `dns.nextdns.io`, `one.one.one.one`, `dns.cloudflare.com`)
4. Tap **Save** — the app will:
   - Validate the hostname/IP syntax
   - Test the connection to port 853 (DNS-over-TLS) with a 10-second timeout
   - Save and apply the DNS setting if successful
   - Show an error message if validation or connection fails
5. Once saved, you can use the large toggle switch to turn Private DNS on/off

## Step 6: You're Ready!

### Using the Quick Settings Tile

- Pull down your notification shade
- Tap the **Private DNS** tile to toggle between your saved DNS hostname and off
- The tile will light up when Private DNS is active, and dim when it's off

### Using the App

- Open **PrivDNS Toggle** to:
  - **Toggle Private DNS**: Use the large horizontal switch at the top to turn Private DNS on/off
  - **Change DNS Provider**: Enter a new hostname/IP in the input field and tap **Save**
    - The app validates syntax (hostname, IPv4, or IPv6 format)
    - Tests connectivity to the DNS provider (10-second timeout)
    - Shows inline error messages if validation or connection fails
  - **View Status**: See current Private DNS state and active hostname at the top

## Troubleshooting

### Device Not Detected by ADB

**Symptoms:** `adb devices` shows no devices or shows `unauthorized`

**Solutions:**
- Ensure USB debugging is enabled (Step 2.2)
- Check your USB cable (try a different cable)
- Try a different USB port
- On your device, revoke USB debugging authorizations in Developer Options, then reconnect
- Install device-specific USB drivers if needed (check your device manufacturer's website)

### Permission Denied Error

**Symptoms:** `adb shell pm grant` command fails with "Permission denied"

**Solutions:**
- Ensure USB debugging is enabled and authorized
- Try disconnecting and reconnecting your device
- Restart ADB server: `adb kill-server` then `adb start-server`
- Ensure you're using the correct package name: `com.privdnstoggle.app`

### App Installed But Toggle Doesn't Work

**Symptoms:** App opens but toggle has no effect

**Solutions:**
- Verify permission was granted (Step 4.2)
- Check that you're running Android 9+ (API 28+)
- Try uninstalling and reinstalling the app, then grant permission again
- Check app logs in Android Studio: **View > Tool Windows > Logcat**

### DNS Connection Test Fails

**Symptoms:** Save button shows "Connection failed" or timeout error

**Solutions:**
- Verify your internet connection is active
- Check that the DNS hostname/IP is correct (no `https://` prefix, no port numbers)
- Ensure the DNS provider supports DNS-over-TLS on port 853
- Try a different DNS provider (e.g., `one.one.one.one`, `dns.cloudflare.com`)
- Some networks may block port 853 — try on a different network or mobile data

### Gradle Sync Fails

**Symptoms:** Android Studio shows Gradle sync errors

**Solutions:**
- Check your internet connection
- Go to **File > Settings > Build, Execution, Deployment > Gradle** and ensure "Use Gradle from" is set to the wrapper
- Try **File > Invalidate Caches / Restart**
- Ensure you have Java 17 installed (required by this project)

### Build Fails

**Symptoms:** Build process fails with errors

**Solutions:**
- Ensure all prerequisites are installed (Android Studio, SDK)
- Check that your device meets minimum requirements (Android 9+)
- Try cleaning the project: **Build > Clean Project**, then **Build > Rebuild Project**
- Check the **Build** output window in Android Studio for specific error messages

## Need More Help?

- **Repository:** [https://github.com/mjryan253/PrivDNSToggle](https://github.com/mjryan253/PrivDNSToggle)
- See the main [README.md](README.md) for technical details and how the app works
- Check [docs/original-plan-idea.md](docs/original-plan-idea.md) for the original design concept
