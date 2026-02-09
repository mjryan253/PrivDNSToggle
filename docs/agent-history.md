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

- **What:** Dark mode (themes.xml, dynamicColorScheme in MainActivity); INTERNET permission; DnsManager.validateHostnameSyntax() and testConnection(); MainActivity redesign with large toggle, DNS input, Save with validation/connection test and inline errors. Tile unchanged.
- **Why:** To implement the approved plan.
- **Notes:** Quick Settings tile required no code changes.

### 2025-02-09 — Documentation updates for new features

- **What:** Updated README.md and quick-start.md to describe large toggle, DNS validation and connection test, dark mode, updated Save flow and troubleshooting.
- **Why:** Keep docs in sync with new behavior and UI.

### 2025-02-09 — Fix resource linking (ic_dns.xml)

- **What:** Removed `android:tint="?attr/colorControlNormal"` from `app/src/main/res/drawable/ic_dns.xml`; reverted themes.xml to single-style declaration. (First attempt of adding colorControlNormal to theme had failed.)
- **Why:** Build failed with attr/colorControlNormal not found when linking resources; platform theme does not declare that attribute.
- **Notes:** Tile icon still uses path fillColor; system may still tint at runtime.

### 2025-02-09 — Fix crash when saving custom DNS value

- **What:** DnsManager.testConnection(): use createSocket(hostname, port), catch Throwable, trim hostname; validateHostnameSyntax() wrapped in try/catch. MainActivity Save: state updates in withContext(Dispatchers.Main.immediate), entire launch in try/catch.
- **Why:** App crashed when user entered custom DNS and tapped Save; ensure no throws and UI updates on main thread.
- **Notes:** Defensive coding for emulator/device variance.

### 2025-02-09 — Save hostname only; do not enable Private DNS on save

- **What:** MainActivity Save button: removed DnsManager.enableDns() after save; only saveHostname() and success toast ("Saved. Use the switch or Quick Settings tile to turn Private DNS on.").
- **Why:** User wanted Save to only persist the entry, not turn on Private DNS.
- **Notes:** Toggle and tile unchanged; they still enable/disable using saved hostname.

### 2025-02-09 — Unit tests for hostname validation

- **What:** testImplementation for JUnit 4.13.2 and kotlin-test-junit in app/build.gradle.kts. Created app/src/test/.../DnsManagerTest.kt with tests for valid hostnames, IPv4, IPv6, empty/blank, scheme, path/slash, port, invalid format. Documented how to run in README.
- **Why:** Plan to add simple component tests to reduce iterative manual testing.
- **Notes:** No production code changes. Instrumented tests optional and not implemented.

### 2025-02-09 — Testing documentation

- **What:** Created docs/testing.md (what is tested, how to run, what is not covered, dependencies, adding tests). README Tests section and Documentation list updated to link to docs/testing.md.
- **Why:** User requested testing and how-to-test in a separate new document.
- **Notes:** README points to docs/testing.md for all test instructions.

### 2025-02-09 — Android Studio run for unit tests

- **What:** app/build.gradle.kts: added testOptions { unitTests { isReturnDefaultValues = true } }. DnsManagerTest.kt: added @RunWith(JUnit4::class) and imports. docs/testing.md: added "If Android Studio doesn't show Run" troubleshooting (Sync Gradle, Mark as Test Sources Root, Rebuild, Invalidate Caches, Run via Gradle).
- **Why:** User reported Android Studio had no ability to run DnsManagerTest as a test and couldn't get details on lint/validity.
- **Notes:** @RunWith(JUnit4::class) helps IDE recognize JUnit 4 test; troubleshooting covers common causes.

### 2026-02-09 — Replace app icons with filter-icon.png

- **What:** Created `icon-gen/` directory with `convert_icon.py` (Pillow-based) and `requirements.txt`. Script takes `filter-icon.png` from repo root and produces all Android icon assets into `icon-gen/output/res/`: legacy launcher PNGs (5 densities), round launcher PNGs, adaptive-icon foreground PNGs (108dp canvas with 72dp safe zone), monochrome QS tile PNGs (white-on-transparent silhouette), and adaptive-icon XML files. Updated `colors.xml` to change `ic_launcher_background` from blue (#1B6EF3) to dark gray (#3D3D3D). Added `android:roundIcon="@mipmap/ic_launcher_round"` to `AndroidManifest.xml`. Deleted old `drawable/ic_dns.xml` vector (shield/lock), to be replaced by density-specific `ic_dns.png` files from the script output.
- **Why:** User wants `filter-icon.png` used everywhere the app needs an icon or image.
- **Notes:** User must run `convert_icon.py` manually, then copy `icon-gen/output/res/*` into `app/src/main/res/`. The build will not succeed until those generated PNGs are in place (the old `ic_dns.xml` vector was deleted and `@drawable/ic_dns` / `@mipmap/ic_launcher_foreground` now expect the raster replacements).
