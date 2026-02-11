# Troubleshooting PrivDNS Toggle

If the app crashes on launch, won't deploy to your device, or doesn't apply Private DNS after granting permission, use these steps to narrow it down.

---

## 0. "Device not found" when running from the IDE

If the build succeeds but you see **Error running 'app' — device '…' not found** (e.g. `device 'RFCT710DNLX' not found`):

1. **Check the device dropdown** in Android Studio next to the Run (play) button. If it still shows an old device or "No devices", the IDE isn't seeing your current device.
2. **Verify ADB sees the device:** in a terminal run `adb devices`. You should see your device serial and `device` (not `unauthorized` or empty).
3. **If the device is missing:** reconnect the USB cable, ensure **USB debugging** is ON in Developer options, and when the phone asks "Allow USB debugging?" tap **Allow** (and optionally "Always allow from this computer").
4. **Refresh in Android Studio:** use **Run > Select Device** (or the device dropdown) and pick your device once it appears. Then run the app again.
5. **Install the APK manually:** the build already produced an APK. You can install it without the IDE:
   ```bash
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```
   (Use forward slashes on macOS/Linux.) Then open the app on the device. Grant WRITE_SECURE_SETTINGS via ADB as in section 2 below.

---

## 1. Capture the crash log (logcat)

With the device connected over USB and USB debugging enabled:

1. **Clear logcat** (optional, so you only see new lines):
   ```bash
   adb logcat -c
   ```

2. **Start the app** on the device (tap the icon or Quick Settings tile).

3. **Capture logs** right after the crash:
   ```bash
   adb logcat -d > logcat.txt
   ```
   Or stream and filter for the app and crashes:
   ```bash
   adb logcat -s "AndroidRuntime:E" "*:E" | findstr /i privdnstoggle
   ```
   On macOS/Linux use `grep` instead of `findstr`:
   ```bash
   adb logcat -d | grep -i -E "privdnstoggle|FATAL|AndroidRuntime"
   ```

4. Look for:
   - `FATAL EXCEPTION`
   - `Caused by:` (often the real cause)
   - Lines mentioning `com.privdnstoggle.app`

Sharing the **full stack trace** (from "FATAL EXCEPTION" down to the last "Caused by") is the fastest way to get a precise fix.

**Known crash (v0.3 release):** If you see `NoSuchMethodError: ... KeyframesSpec$KeyframeEntity; ... at(Ljava/lang/Object;I)...`, the app was built with Compose BOM 2024.01.00, which has a Material3 vs animation-core mismatch. Rebuild the app from source with the current repo (BOM set to 2023.10.01) and install that build, or wait for a release that includes the fix.

---

## 2. Confirm WRITE_SECURE_SETTINGS is granted

The app needs this permission to change Private DNS. Grant it once via ADB:

```bash
adb shell pm grant com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS
```

**Check that it's actually granted:**

```bash
adb shell dumpsys package com.privdnstoggle.app | findstr permission
```

You should see `android.permission.WRITE_SECURE_SETTINGS: granted=true` for the user where you use the app.  
If the app was **reinstalled** (uninstall + install), you must run the `pm grant` command again; the grant does not survive reinstall.

**Work profile or multiple users:** If the output shows `granted=true` for one line and `granted=false, userId=150` (or another userId), the app is installed in a **work profile** or **secondary user**. You must grant the permission for that user:

```bash
adb shell pm grant --user 150 com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS
```

Replace `150` with the userId that showed `granted=false`. To list users: `adb shell pm list users`. Common IDs: `0` = main profile, `10` = work profile on some devices, `150` = work profile on others.

---

## 3. Install a debug build for better stack traces

Release APKs (e.g. from GitHub) are minified; stack traces can be obfuscated. A debug build gives readable class and method names.

1. Clone the repo and open it in Android Studio (or use the command line).
2. Build and install the debug APK:
   ```bash
   ./gradlew installDebug
   ```
   Or in Android Studio: **Run** → run the "app" configuration.
3. Reproduce the crash and capture logcat as in step 1. The stack trace will be much easier to read.

---

## 4. Device- and OEM-specific behavior

- **Samsung (One UI):** Some versions restrict `Settings.Global` more. If the app no longer crashes but "Permission denied" appears after granting via ADB, try:
  - Rebooting the device after granting.
  - Ensuring you're not in a restricted profile or "Secure Folder" install.
- **Multiple users / work profile:** Install and grant permission in the same user/profile where you use the app. If `dumpsys package` shows `WRITE_SECURE_SETTINGS: granted=false, userId=150` (or another id), run: `adb shell pm grant --user 150 com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS` (use the userId that was false).
- **Android 9–10:** Private DNS exists; if you're on an older OS, confirm the device actually has the Private DNS setting (Settings → Network & internet → Private DNS).

---

## 4.1. Samsung Galaxy S21+ Specific Issues

### Known Crashes with Custom DNS Hostnames

Samsung Galaxy S21+ (and other Samsung devices) may crash when setting custom DNS hostnames via `Settings.Global.putString()`. This appears to be a Samsung One UI restriction or bug that affects the Android Private DNS API.

**Symptoms:**
- App works fine with default DNS values (e.g., "dns.adguard.com")
- App crashes when saving custom DNS hostnames
- Crash occurs specifically when calling `Settings.Global.putString()` with `private_dns_specifier`

**Possible Causes:**
- Samsung One UI may validate DNS hostnames more strictly than stock Android
- Samsung may throw exceptions beyond `SecurityException` (e.g., `IllegalArgumentException`, `NullPointerException`) when setting `private_dns_specifier`
- Samsung's implementation of `Settings.Global` may have additional restrictions or validation

**Debugging Steps:**
1. **Use the Debug Menu:** The app now includes a "Debug Logs" section at the bottom. Expand it before attempting to save a custom DNS value. The logs will show:
   - The exact hostname being saved
   - Which function is being called (`enableDns`, `saveHostname`, etc.)
   - The exact exception type and message if a crash occurs
   - Stack traces for any errors

2. **Check logcat:** Even if the app crashes, logcat may capture the exception:
   ```bash
   adb logcat -c
   # Then reproduce the crash
   adb logcat -d | grep -i -E "privdnstoggle|DnsManager|FATAL"
   ```

3. **Temporarily Disabled Validation:** DNS syntax validation has been temporarily commented out to rule it out as a cause. If crashes persist, the issue is likely in `enableDns()` when calling `Settings.Global.putString()`.

**Workarounds:**
- **Reboot after permission grant:** Some users report that rebooting the device after granting `WRITE_SECURE_SETTINGS` helps
- **Avoid Secure Folder:** Ensure the app is not installed in Samsung's Secure Folder
- **Check for Samsung-specific restrictions:** Some Samsung devices have additional security policies that may interfere
- **Use default DNS values:** If custom values consistently crash, use a well-known DNS provider's default hostname

**Known Limitations:**
- Samsung devices may reset Private DNS to "Automatic" unexpectedly
- Some Samsung devices experience DNS resolution failures when Private DNS is enabled with custom hostnames
- Samsung's Private DNS implementation may differ from stock Android, causing compatibility issues

**Reporting Issues:**
If you encounter crashes on Samsung devices, please include:
- Device model (e.g., "Samsung Galaxy S21+")
- Android version and One UI version
- The exact hostname that causes the crash
- Debug log output from the app's Debug Menu
- Full logcat output around the time of the crash

---

## 5. If the app opens but changes don't apply

- Confirm the grant (step 2). The in-app "Setup Instructions" card should not show "Required" after a successful grant; if it does, the permission isn't active.
- After changing the DNS hostname, tap **Save** and wait for "Testing connection…" to finish. If the test fails, the hostname is not saved; fix the hostname or network and try again.
- Reboot the device once after the first successful save; some stacks only pick up the new Private DNS setting after a reboot.

---

## 6. Quick checklist

| Step | Action |
|------|--------|
| 1 | Connect device via USB, enable USB debugging |
| 2 | Run: `adb shell pm grant com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS` |
| 3 | Verify: `adb shell dumpsys package com.privdnstoggle.app \| findstr permission` shows the grant |
| 4 | Open the app; if it crashes, run `adb logcat -d > logcat.txt` and inspect (or share) the stack trace |
| 5 | If needed, build and install a debug build and reproduce to get a clear stack trace |

If you open an issue on GitHub, please include: device model, Android version, whether you're on the release APK or a debug build, and the relevant logcat output (especially the fatal exception and "Caused by" lines).
