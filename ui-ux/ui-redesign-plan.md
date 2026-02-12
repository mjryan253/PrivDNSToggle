# PrivDNS Toggle UI Redesign Plan (Updated)

## Summary

Implement the "Secure Core" dashboard from [ui-ux-redeisgn.md](ui-ux-redeisgn.md) using **funnel-crop-OLED.png** as the hero and as the source for app icons. Save button uses **Save only** (no auto-enable). App icons are updated from **icon-gen/output** artifacts after preflight verification.

---

## 1. Design and Layout Alignment

| Doc element | Implementation |
|-------------|----------------|
| **Header** | `CenterAlignedTopAppBar` with title "PrivDNS Toggle", bold, letterSpacing; container and title colors from new palette. |
| **Hero** | Funnel image (**funnel-crop-OLED.png** as `ic_funnel`) centered; **active** = alpha 1f, **inactive** = alpha 0.4f via `animateFloatAsState`. |
| **Status** | "Filtering Active" (green) / "Protection Disabled" (grey); optional subtitle "Connected to $currentHost" when active. |
| **Toggle** | Existing `LargeToggleSwitch` with optional size parameters; use **200×60 dp** on this screen. Same enable/disable logic via ViewModel. |
| **Configuration card** | Single Card (surface grey) with "Configuration" label, DNS hostname `OutlinedTextField`, error/success text, and "Save Configuration" button. |
| **Setup card** | Keep existing collapsible Setup Instructions (ADB command, copy); restyle container to surface grey and text to white/grey. |
| **Debug panel** | Keep DEBUG-only block; restyle to match (surface, inverseSurface for log area). |

**Color palette** (from doc):

- Background: `#1C1C1E`
- Accent/Active: `#34C759`
- Chaos/Error: `#FF3B30` (error states)
- Surface (cards, input area): `#2C2C2E`

---

## 2. Logic Reconciliation

### 2.1 Keep the ViewModel

Keep [DnsSettingsViewModel](app/src/main/kotlin/com/privdnstoggle/app/DnsSettingsViewModel.kt) and observe `viewModel.isActive`, `viewModel.currentHost`, `viewModel.hasPermission`, `viewModel.savedHostname`. Use `viewModel.enableDns` / `viewModel.disableDns` / `viewModel.saveHostname` so toggling from Quick Settings or returning to the app updates the screen correctly.

### 2.2 Save button: Save only (documentation match)

**Decided: Save only.** On Save:

1. Validate hostname syntax (re-enable `DnsManager.validateHostnameSyntax`).
2. Run connection test (`DnsManager.testConnection`).
3. Call `viewModel.saveHostname(host)` only — **do not** call `viewModel.enableDns(host)`.

Show success message such as "Configuration saved" or "✓ Configuration saved". User turns protection on via the toggle or Quick Settings tile. This matches the doc and separates "Configuration" (setup) from "Daily use" (toggle).

### 2.3 Syntax validation

Re-enable `DnsManager.validateHostnameSyntax(host)` before the connection test and show errors in the Configuration card. If a device-specific issue reappears, document in [docs/troubleshooting.md](../docs/troubleshooting.md).

### 2.4 Copy and messaging

- Status: "Filtering Active" / "Protection Disabled" and "Connected to $currentHost" when active.
- Success: "Configuration saved" or "✓ Configuration saved" in accent green.
- Button: "Save Configuration", with "Testing..." during validation.

---

## 3. Hero and app assets: one source (funnel-crop-OLED.png), deterministic path

- **Source:** `funnel-crop-OLED.png` is the single source image for redesign visuals.
- **Launcher and tile:** Use generated files from **icon-gen/output/res/** and copy into **app/src/main/res/** after preflight checks pass.
- **Hero drawable (ic_funnel):** **Decided: Option B** for this redesign pass. Copy **funnel-crop-OLED.png** from repo root to `app/src/main/res/drawable-nodpi/ic_funnel.png` manually (no icon-gen script change in this pass).
- **In Compose:** Use `painterResource(R.drawable.ic_funnel)` for the hero with `animateFloatAsState` (active 1f, inactive 0.4f) for alpha.

---

## 4. App icons from icon-gen/output (with preflight gate)

Do not assume artifacts are complete. Validate required files before copying. If required files are missing, rerun `icon-gen/convert_icon.py` first.

**Implementation:**

- **Preflight checklist (must pass):**
  - `icon-gen/output/res/drawable-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_dns.png`
  - `icon-gen/output/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png`
  - `icon-gen/output/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_round.png`
  - `icon-gen/output/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher_foreground.png`
  - `icon-gen/output/res/mipmap-anydpi-v26/ic_launcher.xml`
  - `icon-gen/output/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- Copy (or sync) **icon-gen/output/res/** into **app/src/main/res/** so that:
  - **drawable-*** (hdpi, mdpi, xhdpi, xxhdpi, xxxhdpi): `ic_dns.png` — Quick Settings tile and `@drawable/ic_dns`.
  - **mipmap-anydpi-v26**: `ic_launcher.xml`, `ic_launcher_round.xml`.
  - **mipmap-*** (all densities): `ic_launcher.png`, `ic_launcher_round.png`, `ic_launcher_foreground.png`.
- Copy hero separately from Section 3 (manual `drawable-nodpi/ic_funnel.png`).

---

## 5. LargeToggleSwitch size

**Decided: Option A.** Add optional size parameters to `LargeToggleSwitch` (e.g. `trackWidth`, `trackHeight`; thumb size derived or parameterized so layout stays correct). Use **200×60 dp** on the redesigned screen so the switch fits under the hero image. Defaults can remain 280×72 for any other use.

---

## 6. Theme and theming

**Decided: Option B.** Define the palette in **res/values/colors.xml** and use it in the app theme for app-wide consistency. Compose screens reference these via `colorResource(R.color.*)` or via Material theme wired to the same colors.

**Execution requirement:** The current app theme uses `dynamicDarkColorScheme` on Android 12+. For this redesign, disable/replace dynamic colors for this screen/app theme path so the fixed palette (`#1C1C1E`, `#34C759`, `#2C2C2E`, `#FF3B30`) is deterministic across devices.

---

## 7. Implementation order

0. **Asset preflight gate:** Verify required icon-gen outputs from Section 4 exist. If not, rerun `icon-gen/convert_icon.py` and re-verify.
1. **Palette and theme:** Add palette to **res/values/colors.xml** (background, accent/active, surface, error) and wire **themes.xml** / Compose theme so the app uses these colors app-wide. Disable/replace dynamic colors so palette is deterministic.
2. **App icons and hero:** Copy **icon-gen/output/res/** into **app/src/main/res/** (drawable-*, mipmap-*), and copy **funnel-crop-OLED.png** to `app/src/main/res/drawable-nodpi/ic_funnel.png`.
3. **LargeToggleSwitch:** Add optional `trackWidth`/`trackHeight` (and proportional thumb/label math); use **200×60 dp** on the redesigned screen.
4. **Layout:** Restructure `DnsSettingsScreen` in MainActivity.kt: Scaffold (theme colors) → CenterAlignedTopAppBar → Hero (ic_funnel + alpha) → status → toggle → Configuration Card → Setup Card → Debug. Keep ViewModel and all flows; use `colorResource` or theme for palette.
5. **Save flow:** Save only (no enableDns after save); re-enable syntax validation; align messaging and acceptance criteria:
   - success text indicates save-only, not enabled
   - no toggle state change is implied by save success
6. **Validation regression check:** Run save/validation smoke checks on at least one Samsung/OEM device and one emulator before considering redesign pass complete.
7. **Setup and Debug:** Restyle to new surface/grey; keep behavior.
8. **Agent history:** Append entry to agent/agent-history.md per workspace rules.

---

## 8. Roadblocks and mitigations

| Issue | Mitigation |
|-------|-------------|
| Reactive state | Keep ViewModel and ContentObserver; do not switch to direct DnsManager + local state. |
| Syntax validation | Re-enable; document any device-specific issues in troubleshooting.md. |
| Hero asset | Use funnel-crop-OLED.png for ic_funnel; drawable-nodpi for density-independent scaling. |
| Icon sync | Enforce preflight checklist first; rerun icon-gen if outputs are missing; then copy into app/src/main/res/. |
| Dynamic colors override palette | Disable/replace dynamicDarkColorScheme path for redesign theme so colors remain consistent across devices. |
| Save-only UX ambiguity | Ensure save success copy explicitly says configuration is saved only and protection remains controlled by toggle/tile. |

---

## 9. Options still open

None. Save behavior (save only), hero/icon source path (manual hero copy + icon-gen for launcher/tile), LargeToggleSwitch (200×60), and theme behavior (fixed palette, no dynamic override for redesign path) are all decided.

---

## 10. Sanity and logic check vs ui-ux-redeisgn.md

Cross-check that this plan achieves the redesign doc’s goals and follows best practices:

| Goal (from ui-ux-redeisgn.md) | Plan alignment |
|-------------------------------|----------------|
| **Dashboard, not settings list** | Layout is Hero → Status → Toggle → Configuration card → Setup; clear “Secure Core” hierarchy. |
| **Funnel as hero; active = bright, inactive = dim** | Hero uses `ic_funnel` (from funnel-crop-OLED); `animateFloatAsState` 1f / 0.4f (doc suggests 0.5f or grayscale — 0.4f is within spec). |
| **Header: “PrivDNS Toggle”, non-negotiable branding** | CenterAlignedTopAppBar with that title; theme colors. |
| **Status: “Protection Active” (green) / “Protection Disabled” (grey)** | Plan uses “Filtering Active” / “Protection Disabled” and “Connected to $currentHost”; doc uses “Protection Active” in one place and “Filtering Active” in code — plan keeps “Filtering Active” for consistency with “filter” metaphor. Both are acceptable; ensure one consistent pair in implementation. |
| **Toggle under status; physical switch for the filter** | LargeToggleSwitch directly under status; same enable/disable logic via ViewModel; 200×60 to fit under hero. |
| **Configuration card: distinct section, “Daily Use” vs “Setup”** | Single Configuration card with DNS hostname, validation, Save; Setup Instructions in separate collapsible card. Save only (no auto-enable) enforces “config here, toggle for daily use”. |
| **Color palette: Background #1C1C1E, Accent #34C759, Error #FF3B30, Surface #2C2C2E** | Plan adopts these; Option B (colors.xml + theme) makes them app-wide. |
| **Save: validate syntax → test connection → save only, no auto-enable** | Re-enable `validateHostnameSyntax`; then `testConnection`; then `saveHostname` only. Matches doc’s “We only save, we don’t auto-enable”. |
| **Black/OLED theme so green pops** | Dark background and theme; funnel-crop-OLED (true black) for hero and icons. |

**Best-practice checks:**

- **State and reactivity:** ViewModel + StateFlow + ContentObserver retained so tile and onResume keep UI in sync; no direct DnsManager + local refresh in the screen.
- **Single source for assets:** One source image (funnel-crop-OLED), one icon-gen run, one copy from icon-gen/output; hero either from that output (if script extended) or one extra copy of same file.
- **Theme and DRY:** Palette in colors.xml and theme avoids scattered hardcoded colors and keeps future screens consistent.
- **Accessibility:** Hero has contentDescription (“Privacy Filter”); status and toggle are readable and focusable; error/success text in Configuration card.
- **Agent history:** Plan includes appending to agent-history.md per workspace rules.

**Minor doc vs plan variance:** Redesign doc says “Protection Active” in the hierarchy list and “Filtering Active” in the code snippet. Plan standardizes on “Filtering Active” / “Protection Disabled”. If you prefer exact doc wording, use “Protection Active” when active; otherwise “Filtering Active” is consistent with the filter metaphor and the doc’s own code.
