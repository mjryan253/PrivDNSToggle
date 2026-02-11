# Debugging with On-Device Logs

The app includes a built-in debug logging system that captures detailed information about DNS operations, validation, connection tests, and any errors that occur. This guide explains how to use the debug logs to diagnose issues.

---

## Accessing the Debug Menu

1. **Open the app** on your device
2. **Scroll to the bottom** of the main screen
3. **Tap "Debug Logs"** to expand the debug menu
4. The debug menu appears as a collapsible card below the "Setup Instructions" section

---

## What the Logs Show

The debug logs capture:

- **DNS operations:** When DNS settings are being read or written
- **Hostname validation:** Syntax validation attempts and results
- **Connection tests:** TLS connection attempts to DNS servers
- **Save operations:** When hostnames are saved to preferences
- **Exceptions:** Detailed error messages and stack traces when things go wrong
- **Function calls:** Which functions are being called and with what parameters

### Log Entry Format

Each log entry includes:
- **Timestamp:** `[HH:mm:ss.SSS]` format (e.g., `[14:23:45.123]`)
- **Tag:** Component name (`DnsManager` or `MainActivity`)
- **Message:** Description of what's happening
- **Stack traces:** Full exception details when errors occur

Example log entries:
```
[14:23:45.123] MainActivity: Save button clicked with hostname: 'custom.dns.com'
[14:23:45.125] DnsManager: validateHostnameSyntax: Validating 'custom.dns.com'
[14:23:45.130] DnsManager: validateHostnameSyntax: Valid hostname
[14:23:45.200] MainActivity: Starting connection test for 'custom.dns.com'
[14:23:45.500] DnsManager: testConnection: Starting connection test to 'custom.dns.com'
[14:23:46.000] DnsManager: testConnection: Connection successful to 'custom.dns.com'
[14:23:46.100] MainActivity: Connection test successful, saving hostname
[14:23:46.150] DnsManager: saveHostname: Saving hostname 'custom.dns.com'
[14:23:46.200] DnsManager: saveHostname: Saved successfully
[14:23:46.250] MainActivity: Calling enableDns() with 'custom.dns.com'
[14:23:46.300] DnsManager: enableDns: Setting mode to hostname
[14:23:46.350] DnsManager: enableDns: Setting specifier to 'custom.dns.com'
[14:23:46.400] DnsManager: enableDns: Exception - IllegalArgumentException: Invalid DNS hostname
```

---

## Using Logs to Diagnose Crashes

### Step 1: Prepare for Logging

1. **Open the app** and navigate to the main screen
2. **Expand the Debug Logs menu** before attempting the operation that causes the crash
3. **Clear existing logs** by tapping "Clear Logs" (optional, but helps focus on the current issue)

### Step 2: Reproduce the Issue

1. **Enter a DNS hostname** in the input field (or use the one that causes crashes)
2. **Tap "Save"** button
3. **Watch the logs** as they appear in real-time
4. The logs update automatically every 500ms when the debug menu is expanded

### Step 3: Analyze the Logs

Look for these key indicators:

**Before the crash:**
- What hostname was entered?
- Did validation pass or fail?
- Did the connection test succeed?
- Which function was called last?

**At the point of crash:**
- What exception type was thrown? (e.g., `IllegalArgumentException`, `SecurityException`, `NullPointerException`)
- What was the error message?
- Which function threw the exception? (usually `enableDns()` for DNS setting issues)

**Example crash log:**
```
[14:23:46.250] MainActivity: Calling enableDns() with 'custom.dns.com'
[14:23:46.300] DnsManager: enableDns: Setting mode to hostname
[14:23:46.350] DnsManager: enableDns: Setting specifier to 'custom.dns.com'
[14:23:46.400] DnsManager: enableDns: Exception - IllegalArgumentException: Invalid DNS hostname
android.content.ContentResolver.putString(ContentResolver.java:1234)
com.privdnstoggle.app.DnsManager.enableDns(DnsManager.kt:150)
```

---

## Common Issues and What to Look For

### Issue: App Crashes When Saving Custom DNS

**What to check:**
- Look for the last log entry before the crash
- Check if `enableDns()` was called
- Note the exception type and message
- See if the exception occurs on the first or second `Settings.Global.putString()` call

**What the logs tell you:**
- If the exception is `SecurityException`: Permission issue
- If the exception is `IllegalArgumentException`: Samsung may be rejecting the hostname format
- If the exception is `NullPointerException`: Possible null value being passed

### Issue: "Permission Denied" Error

**What to check:**
- Look for `enableDns()` calls
- Check if `SecurityException` appears in the logs
- Verify the hostname value being passed

**What the logs tell you:**
- If `enableDns()` returns false with `SecurityException`: WRITE_SECURE_SETTINGS not granted
- If it fails silently: Check for other exception types

### Issue: Connection Test Fails

**What to check:**
- Look for `testConnection()` entries
- Check the error message in the connection test logs
- See if it's a timeout, DNS resolution failure, or TLS handshake issue

**What the logs tell you:**
- `Connection timed out`: DNS server not responding on port 853
- `Could not resolve hostname`: DNS name doesn't exist
- `TLS handshake failed`: Server doesn't support DNS-over-TLS

---

## Copying Logs for Sharing

When reporting issues or seeking help:

1. **Reproduce the issue** with the debug menu expanded
2. **Wait for the crash** or error to occur
3. **Tap "Copy Logs"** button
4. **Paste the logs** into your bug report, email, or support request

The copied logs include all entries from the current session, formatted with timestamps and stack traces.

---

## Tips for Effective Debugging

1. **Clear logs before testing:** Start with a clean slate for each test
2. **Keep debug menu expanded:** Logs update automatically when expanded
3. **Test one thing at a time:** Change one variable (hostname, network, etc.) per test
4. **Note the exact hostname:** Some hostnames work while others don't - the logs show exactly what was tried
5. **Compare working vs. failing:** Compare logs from a working DNS (like "dns.adguard.com") with a failing one

---

## Limitations

- **Logs are in-memory only:** Logs are cleared when the app is closed
- **Maximum 100 entries:** Older entries are automatically removed
- **No persistence:** Logs don't survive app restarts
- **Real-time only:** You must have the debug menu open to see logs as they happen

---

## Combining with logcat

For complete debugging, combine on-device logs with `adb logcat`:

1. **Start logcat** before reproducing the issue:
   ```bash
   adb logcat -c
   adb logcat > logcat.txt
   ```

2. **Use the debug menu** to see app-specific logs in real-time

3. **After the crash:**
   - Copy logs from the debug menu (app-specific details)
   - Stop logcat (Ctrl+C) and check `logcat.txt` (system-level details)

The debug menu logs provide app-level context, while logcat provides system-level details including Android framework messages.

---

## Example Debugging Session

**Scenario:** App crashes when saving "custom.dns.example.com"

1. **Open app, expand Debug Logs, clear logs**
2. **Enter "custom.dns.example.com" and tap Save**
3. **Observe logs:**
   ```
   [14:23:45.123] MainActivity: Save button clicked with hostname: 'custom.dns.example.com'
   [14:23:45.200] MainActivity: Starting connection test for 'custom.dns.example.com'
   [14:23:45.500] DnsManager: testConnection: Starting connection test to 'custom.dns.example.com'
   [14:23:46.000] DnsManager: testConnection: Connection successful to 'custom.dns.example.com'
   [14:23:46.100] MainActivity: Connection test successful, saving hostname
   [14:23:46.150] DnsManager: saveHostname: Saving hostname 'custom.dns.example.com'
   [14:23:46.200] DnsManager: saveHostname: Saved successfully
   [14:23:46.250] MainActivity: Calling enableDns() with 'custom.dns.example.com'
   [14:23:46.300] DnsManager: enableDns: Setting mode to hostname
   [14:23:46.350] DnsManager: enableDns: Setting specifier to 'custom.dns.example.com'
   [14:23:46.400] DnsManager: enableDns: Exception - IllegalArgumentException: Invalid DNS hostname
   ```

4. **Analysis:** The crash occurs in `enableDns()` when setting the specifier. The exception is `IllegalArgumentException`, suggesting Samsung is rejecting the hostname format even though it passed validation and connection test.

5. **Next steps:** Try a different hostname format, check Samsung-specific restrictions, or report the issue with these logs.

---

## Troubleshooting the Debug Menu

**Debug menu not showing logs:**
- Ensure the menu is expanded (tap "Debug Logs")
- Try clearing and reproducing the issue
- Check if the app has crashed (logs may not update after crash)

**Logs not updating:**
- Ensure the debug menu is expanded (auto-update only works when expanded)
- Wait a moment (updates every 500ms)
- Try scrolling to refresh the view

**Can't copy logs:**
- Ensure there are log entries (check if the log area shows "No log entries yet")
- Try clearing and reproducing to generate new logs
- Check clipboard permissions if copy fails
