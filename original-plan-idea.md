Since you have an AI agent to generate the boilerplate, the **cleanest and most professional** route is to build a minimal **Native Android App (Kotlin)** that functions as a **Quick Settings Tile** or a **Home Screen Widget**.

Using a **Quick Settings Tile** (the toggles in your notification shade) is usually superior for this specific use case because it natively supports "active/inactive" states (lit up vs. dimmed) and is accessible from any screen.

Here is the blueprint you can feed your AI agent to get the exact code you need.

### The Architecture

* **Language:** Kotlin (Standard, concise).
* **Component:** `TileService` (for a Quick Settings toggle) OR `AppWidgetProvider` (for a Home Screen button).
* **Permission:** `android.permission.WRITE_SECURE_SETTINGS`.
* **Logic:** Reading/Writing to `Settings.Global`.

### Step 1: The "Prompt" for your AI Agent

Copy and paste this prompt to your agent. It contains the specific API references required to make this work (as some are hidden/system settings):

> "Create a minimal Android application in Kotlin.
> **Goal:** A Quick Settings Tile (TileService) that toggles the system's Private DNS setting.
> **Requirements:**
> 1. **Manifest:** specific permission `android.permission.WRITE_SECURE_SETTINGS` and declare the `TileService`.
> 2. **Logic:**
> * Read `Settings.Global` string `private_dns_mode`.
> * If mode is 'off', switch it to 'hostname' and set `private_dns_specifier` to 'dns.adguard.com' (or a placeholder I can change).
> * If mode is 'hostname', switch it to 'off'.
> 
> 
> 3. **UI State:** Update the Tile's state (STATE_ACTIVE for on, STATE_INACTIVE for off) and icon based on the current DNS setting.
> 4. **Error Handling:** If the app lacks the SECURE_SETTINGS permission, show a Toast telling the user to run the ADB command.
> 
> 
> Please provide the `AndroidManifest.xml` and the `MyDnsTileService.kt` file."

---

### Step 2: The Critical Manual Step (ADB)

Your app cannot request this permission via a popup; it implies system-level privileges. Once you build and install the APK your agent gives you, you **must** run this ADB command once:

```bash
adb shell pm grant <com.your.package.name> android.permission.WRITE_SECURE_SETTINGS

```

### Technical Details (For your review)

If you want to verify the code your agent generates, here is the logic it must use.

**1. The Settings Keys:**
Android stores Private DNS settings in the global table (not `System` or `Secure`).

* **Mode Key:** `"private_dns_mode"`
* Values: `"off"`, `"opportunistic"` (Automatic), `"hostname"` (Private DNS provider hostname).


* **Hostname Key:** `"private_dns_specifier"`
* Value: The actual URL (e.g., `one.one.one.one`).



**2. The Toggle Logic (Kotlin Snippet):**

```kotlin
val resolver = contentResolver
val currentMode = Settings.Global.getString(resolver, "private_dns_mode")

if (currentMode == "hostname") {
    // Turn OFF
    Settings.Global.putString(resolver, "private_dns_mode", "off")
    qsTile.state = Tile.STATE_INACTIVE
} else {
    // Turn ON
    Settings.Global.putString(resolver, "private_dns_mode", "hostname")
    Settings.Global.putString(resolver, "private_dns_specifier", "dns.google") // Your DNS here
    qsTile.state = Tile.STATE_ACTIVE
}
qsTile.updateTile()

```

### Why a TileService over a Widget?

* **Visual Feedback:** Tiles handle the "Lit Up = ON" state natively. Widgets require manual bitmap manipulation to change colors.
* **Access:** You can toggle DNS while in another app without going back to the home screen.
* **Code Size:** A `TileService` is often fewer lines of code than an `AppWidgetProvider` with `RemoteViews`.