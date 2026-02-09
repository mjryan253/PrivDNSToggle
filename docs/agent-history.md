# Agent history

**Mandate for AI agents:** When you make code or configuration edits in this repository, you **must** append a new dated entry to this file (`docs/agent-history.md`) summarizing what you changed and why. Keep entries concise but enough for future agents and maintainers to understand the evolution of the project.

---

## Entry format

Each new entry should follow this pattern:

```markdown
### YYYY-MM-DD — Short title
- **What:** Brief list of files/changes.
- **Why:** Reason or user request.
- **Notes:** Optional caveats, follow-ups, or context.
```

---

## History

### 2025-02-09 — Project setup and quick-start

- **What:** Created `quick-start.md` in repo root with steps to build and run the app on a physical Android device (prerequisites, Android Studio setup, device prep, build/install options, ADB permission grant, Quick Settings tile setup, troubleshooting). Repository URL was not yet set; placeholder used for clone command.
- **Why:** User requested a quick-start guide for getting the app built and running on a physical device.
- **Notes:** README already had high-level setup; quick-start expanded it into a full, step-by-step guide with Windows-specific notes where relevant.

### 2025-02-09 — Repository URL in docs

- **What:** Updated `quick-start.md` and `README.md` to use the public repo URL `https://github.com/mjryan253/PrivDNSToggle` (clone command and repository link). Added repository link to the "Need More Help?" section in quick-start.
- **Why:** User requested all documentation to reference the publicly available repository address.

### 2025-02-09 — .gitignore for native Android

- **What:** Added root `.gitignore` for a native Android app (build outputs, Gradle, IDE files, local.properties, keystores, OS cruft, etc.).
- **Why:** User requested a gitignore suitable for a native Android project.

### 2025-02-09 — App UI and validation overhaul (plan)

- **What:** Created a plan (no code changes) for: dark mode by default; large horizontal toggle switch; DNS hostname/IP field below with validation and 10s connection test; Save flow with syntax validation then TLS connection test; Quick Settings tile unchanged. Plan documented in a Cursor plan file.
- **Why:** User wanted to go from the then-current UI (small switch, simple save) to a design with a prominent toggle, validated/saved DNS field, connection test, and dark theme, with the tile mirroring the in-app toggle.

### 2025-02-09 — Implement UI and validation plan

- **What:**
  - **Dark mode:** `themes.xml` parent set to `Theme.Material.NoActionBar`; `MainActivity.kt` `dynamicColorScheme()` switched to `dynamicDarkColorScheme()` / `darkColorScheme()`.
  - **INTERNET permission:** Added to `AndroidManifest.xml` for connection test.
  - **DnsManager.kt:** Added `validateHostnameSyntax()` (hostname/IP regex, no scheme/port/path) and `suspend fun testConnection(hostname)` (TLS connect to port 853, 10s timeout, `Result`-based, never throw).
  - **MainActivity.kt:** Replaced main screen with large custom toggle (`LargeToggleSwitch`), DNS input field, Save button that runs syntax check then connection test with loading state and inline errors; kept collapsible setup instructions.
- **Why:** To implement the approved plan (dark theme, large toggle, validation, connection test, tile unchanged).
- **Notes:** Quick Settings tile required no code changes; it already used `DnsManager.toggle()` and saved hostname.

### 2025-02-09 — Documentation updates for new features

- **What:** Updated `README.md` and `quick-start.md` to describe: large toggle, DNS validation and connection test, dark mode, updated Save flow and troubleshooting (e.g. connection test failures).
- **Why:** Keep docs in sync with new behavior and UI.

### 2025-02-09 — Fix resource linking (ic_dns.xml)

- **What:** First attempt: added `colorControlNormal` to `themes.xml` for the drawable that referenced `?attr/colorControlNormal`. Build still failed (style attribute not found in merged resources). Second fix: removed `android:tint="?attr/colorControlNormal"` from `app/src/main/res/drawable/ic_dns.xml` and reverted `themes.xml` to a single-style declaration with no extra items.
- **Why:** Build failed with `attr/colorControlNormal not found` when linking resources; the platform theme does not declare that attribute, so the drawable was updated to not depend on it.
- **Notes:** Tile icon still uses path fillColor; system may still tint it at runtime.

### 2025-02-09 — Fix crash when saving custom DNS value

- **What:**
  - **DnsManager.kt:** `testConnection()`: Switched to `SSLSocketFactory.getDefault().createSocket(hostname, DOT_PORT)` instead of create-then-connect; added `Throwable` catch so the function never throws; trim hostname and handle empty; set socket soTimeout.
  - **DnsManager.kt:** `validateHostnameSyntax()`: Wrapped body in try/catch returning generic error on any throw.
  - **MainActivity.kt:** Save button handler: Wrapped post–connection-test state updates in `withContext(Dispatchers.Main.immediate)`; wrapped entire `scope.launch` body in try/catch and on exception set `isValidating = false` and a generic error message.
- **Why:** App crashed when user entered a custom DNS and tapped Save; fixes ensure connection test and validation never throw and that UI state is only updated on the main thread, with any unexpected error surfaced as a message instead of a crash.
- **Notes:** Defensive coding for emulator/device variance and to avoid main-thread assertion crashes.

### 2025-02-09 — Docs layout and agent-history mandate

- **What:**
  - Created `docs/` and `docs/agent-history.md` with full context/history above and a mandate that agents must append to this file when making code edits.
  - Moved `original-plan-idea.md` to `docs/original-plan-idea.md`.
  - Added Cursor rule in `.cursor/rules/agent-history.mdc` requiring agents to append to `docs/agent-history.md` when making edits.
- **Why:** User requested: dump all context and history into an agent-history file; require new agents to append to it when making edits; put documentation in `docs/` while keeping `README.md` and `quick-start.md` in the repo root.
