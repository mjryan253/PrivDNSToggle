# Troubleshooting PrivDNS Toggle

If the app crashes on launch or doesn’t apply Private DNS even after granting permission, use these steps to narrow it down.

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

Sharing the **full stack trace** (from “FATAL EXCEPTION” down to the last “Caused by”) is the fastest way to get a precise fix.

**Known crash (v0.3 release):** If you see `NoSuchMethodError: ... KeyframesSpec$KeyframeEntity; ... at(Ljava/lang/Object;I)...`, the app was built with Compose BOM 2024.01.00, which has a Material3 vs animation-core mismatch. Rebuild the app from source with the current repo (BOM set to 2023.10.01) and install that build, or wait for a release that includes the fix.

---

## 2. Confirm WRITE_SECURE_SETTINGS is granted

The app needs this permission to change Private DNS. Grant it once via ADB:

```bash
adb shell pm grant com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS
```

**Check that it’s actually granted:**

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
   Or in Android Studio: **Run** → run the “app” configuration.
3. Reproduce the crash and capture logcat as in step 1. The stack trace will be much easier to read.

---

## 4. Device- and OEM-specific behavior

- **Samsung (One UI):** Some versions restrict `Settings.Global` more. If the app no longer crashes but “Permission denied” appears after granting via ADB, try:
  - Rebooting the device after granting.
  - Ensuring you’re not in a restricted profile or “Secure Folder” install.
- **Multiple users / work profile:** Install and grant permission in the same user/profile where you use the app. If `dumpsys package` shows `WRITE_SECURE_SETTINGS: granted=false, userId=150` (or another id), run: `adb shell pm grant --user 150 com.privdnstoggle.app android.permission.WRITE_SECURE_SETTINGS` (use the userId that was false).
- **Android 9–10:** Private DNS exists; if you’re on an older OS, confirm the device actually has the Private DNS setting (Settings → Network & internet → Private DNS).

---

## 5. If the app opens but changes don’t apply

- Confirm the grant (step 2). The in-app “Setup Instructions” card should not show “Required” after a successful grant; if it does, the permission isn’t active.
- After changing the DNS hostname, tap **Save** and wait for “Testing connection…” to finish. If the test fails, the hostname is not saved; fix the hostname or network and try again.
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

If you open an issue on GitHub, please include: device model, Android version, whether you’re on the release APK or a debug build, and the relevant logcat output (especially the fatal exception and “Caused by” lines).
