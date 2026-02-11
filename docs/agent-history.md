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

### 2026-02-09 — v0.2-beta release prep

- **What:** Bumped `versionCode` to 2 and `versionName` to `"0.2-beta"` in `app/build.gradle.kts`. Replaced individual `.idea/*` exclusions in `.gitignore` with a blanket `.idea/` rule and removed tracked `.idea/` files from git. Created `CHANGELOG.md` with v0.2-beta release notes.
- **Why:** Preparing the main branch for the v0.2-beta GitHub Release.
- **Notes:** Tagged as `v0.2-beta` pre-release on GitHub with unsigned APK attached.

### 2026-02-09 — v0.3 build, tag, and release upload

- **What:** Bumped version to v0.3 (versionCode 3, versionName "0.3") in app/build.gradle.kts. Added CHANGELOG entry for 0.3 (signed APK fix). Created git tag v0.3. Added scripts/upload-release-v0.3.ps1 to push tag and create/upload release via GitHub CLI.
- **Why:** User requested assembleRelease and upload to beta release using v0.3.
- **Notes:** Build succeeded; APK is at builds/app/outputs/apk/release/app-release.apk. Tag v0.3 is local; push and release must be run by user (git push origin v0.3; then run script or use gh release create manually). GitHub CLI (gh) was not installed in the environment.

### 2026-02-09 — Fix Unresolved reference 'util' in build.gradle.kts

- **What:** Added `import java.util.Properties` at top of `app/build.gradle.kts` and changed `java.util.Properties()` to `Properties()` in the signing config.
- **Why:** In the app module script, `java` is the Java plugin extension, so `java.util` was unresolved; using the import avoids the name clash.

### 2026-02-09 — Fix unsigned release APK (install failure on Samsung S21+)

- **What:** Added `signingConfigs` block to `app/build.gradle.kts` with support for `keystore.properties` (for proper release signing) and automatic fallback to the debug keystore. Applied signing config to the `release` build type. Updated `.gitignore` to exclude `*.jks`, `*.keystore`, and `keystore.properties`.
- **Why:** Release APK was unsigned, causing "App not installed as package appears to be invalid" on Samsung Galaxy S21+ (and likely all devices).
- **Notes:** For a production release, create a dedicated release keystore and `keystore.properties` file. The debug keystore fallback ensures APKs are always installable during development and for personal distribution.

### 2026-02-09 — Replace app icons with filter-icon.png

- **What:** Created `icon-gen/` directory with `convert_icon.py` (Pillow-based) and `requirements.txt`. Script takes `filter-icon.png` from repo root and produces all Android icon assets into `icon-gen/output/res/`: legacy launcher PNGs (5 densities), round launcher PNGs, adaptive-icon foreground PNGs (108dp canvas with 72dp safe zone), monochrome QS tile PNGs (white-on-transparent silhouette), and adaptive-icon XML files. Updated `colors.xml` to change `ic_launcher_background` from blue (#1B6EF3) to dark gray (#3D3D3D). Added `android:roundIcon="@mipmap/ic_launcher_round"` to `AndroidManifest.xml`. Deleted old `drawable/ic_dns.xml` vector (shield/lock), to be replaced by density-specific `ic_dns.png` files from the script output.
- **Why:** User wants `filter-icon.png` used everywhere the app needs an icon or image.
- **Notes:** User must run `convert_icon.py` manually, then copy `icon-gen/output/res/*` into `app/src/main/res/`. The build will not succeed until those generated PNGs are in place (the old `ic_dns.xml` vector was deleted and `@drawable/ic_dns` / `@mipmap/ic_launcher_foreground` now expect the raster replacements).

### 2026-02-09 — Crash troubleshooting and defensive Settings reads

- **What:** Wrapped `DnsManager.getCurrentMode`, `getCurrentHostname`, and `hasPermission` in try/catch so `Settings.Global` read/write never throws and the app does not crash on startup on devices that restrict or throw on global settings. Created `docs/troubleshooting.md` with steps to capture logcat, verify WRITE_SECURE_SETTINGS grant, use a debug build for readable stack traces, and OEM/device notes.
- **Why:** User reported v0.3 APK crashing without applying changes despite ADB permission grant; needed a way to continue troubleshooting and to harden the app against Settings.Global failures.
- **Notes:** If crashes persist after this change, the logcat steps in troubleshooting.md will pinpoint the cause (e.g. theme/Compose, R8, or OEM-specific code paths).

### 2026-02-09 — Work profile / userId permission in troubleshooting

- **What:** Updated `docs/troubleshooting.md` to explain that `dumpsys package` can show WRITE_SECURE_SETTINGS granted for one user and false for another (e.g. `userId=150`). Added instructions to grant per-user with `adb shell pm grant --user <id> ...` and to use the userId that showed `granted=false`.
- **Why:** User's dumpsys output showed `granted=true` and `granted=false, userId=150`; permission was only granted for the main profile, not the work profile where the app runs.

### 2026-02-09 — Fix Compose BOM crash (NoSuchMethodError KeyframesSpec)

- **What:** Downgraded Compose BOM from `2024.01.00` to `2023.10.01` in `app/build.gradle.kts`. Added note in `docs/troubleshooting.md` describing the KeyframesSpec crash and that rebuilding from source (or a new release) fixes it.
- **Why:** User's logcat showed FATAL EXCEPTION: `NoSuchMethodError: No virtual method at(Ljava/lang/Object;I)Landroidx/compose/animation/core/KeyframesSpec$KeyframeEntity;` — a known incompatibility in BOM 2024.01.00 where Material3 still calls the old KeyframesSpec API while animation-core changed it.
- **Notes:** v0.3 GitHub release was built with 2024.01.00; users need to build from source with this change or install a future release (e.g. v0.4) that uses 2023.10.01 (or a BOM that includes the Material3 fix).

### 2026-02-10 — Version and release plan: beta/v* vs v* workflow

- **What:** Recreated `.cursor/plans/version_and_release_automation_b9adf66b.plan.md` so the release workflow is driven by tag patterns: **beta/v*** publishes beta/prerelease, **v*** publishes full release (single workflow, two tag triggers, prerelease set from tag prefix).
- **Why:** User requested a workflow where "beta/v*" branch/tag publishes beta or prerelease and "v*" branch/tag publishes a full release.
- **Notes:** Plan keeps version file, Gradle wiring, and signing notes; optional enforcement that full-release tags (v*) are only created from main is still documented.

### 2026-02-10 — Version file, release workflow, and generic upload script

- **What:** Added `version.properties` (VERSION_NAME=0.3.1, VERSION_CODE=4). Updated `app/build.gradle.kts` to read version from it with fallbacks. Added `.github/workflows/release.yml`: trigger on tags `beta/v*` and `v*`, build release APK, enforce full release only from main, set prerelease from tag prefix, create GitHub Release with APK. Added [0.3.1] to CHANGELOG. Added "Versioning and releases" section to `quick-start.md`. Added `scripts/upload-release.ps1` (version-agnostic, reads tag and infers prerelease from `beta/` prefix).
- **Why:** Implement the version-and-release-automation plan: single version source, tag-based beta vs production, and consistent manual release path.
- **Notes:** Production releases (tag `v*`) only succeed when the tagged commit is on `origin/main`. Use tag `beta/v0.3.1` to publish v0.3.1 as beta.

### 2026-02-11 — Debug menu and Samsung crash fixes

- **What:** Created `DebugLogger.kt` singleton for capturing log entries with timestamps. Added comprehensive logging throughout `DnsManager` (enableDns, saveHostname, validateHostnameSyntax, testConnection) and `MainActivity` Save button handler. Enhanced `DnsManager.enableDns()` to catch all exceptions (not just SecurityException) with detailed logging. Added collapsible "Debug Logs" menu in `MainActivity` with scrollable log display, Clear Logs, and Copy Logs buttons. Temporarily commented out DNS syntax validation in Save button to rule it out as crash cause. Added Samsung Galaxy S21+ specific troubleshooting section to `docs/troubleshooting.md` documenting known crashes with custom DNS hostnames, debugging steps, workarounds, and known limitations. Created `docs/debugging-with-logs.md` guide explaining how to use the debug menu, interpret log entries, diagnose crashes, and combine with logcat for complete debugging.
- **Why:** App crashes on Samsung Galaxy S21+ when saving custom DNS values; need visibility into crash cause and Samsung-specific issues documented. User requested documentation for debugging using on-device logs.
- **Notes:** Debug menu updates logs every 500ms when expanded. Validation is temporarily disabled - re-enable after identifying crash root cause. DebugLogger maintains circular buffer of last 100 entries.

### 2026-02-11 — Prominent debug button with log viewer

- **What:** Added `getRecentLogEntries(count: Int)` method to `DebugLogger.kt` to return the last N log entries (default 50). Replaced collapsible debug card in `MainActivity.kt` with a prominent "Show Debug" / "Hide Debug" button at the very bottom of the screen. Debug features (log viewer, Clear/Copy buttons) now appear below the button when enabled. Updated log viewer to display only the last 50 entries using `getRecentLogEntries(50)` instead of all entries. Changed state variable from `debugExpanded` to `debugModeEnabled` for clarity.
- **Why:** User reported debug menu not visible on main screen; requested a debug button at the bottom that enables/disables debug features, including a log viewer showing the last 30-50 lines for error hunting.
- **Notes:** Debug button is always visible at the bottom for easy access. Log viewer shows last 50 entries (configurable range 30-50). Log refresh still occurs every 500ms when debug mode is enabled.

### 2026-02-11 — Document expected log format and toggle logging limitation

- **What:** Updated `docs/debugging-with-logs.md` to include expected log format when saving an entry (example from emulator showing successful save flow). Documented that toggle on/off operations do not generate log entries. Updated all references from "debug menu" to "debug mode" to match new UI. Added note about comparing emulator logs vs device logs for debugging.
- **Why:** User requested documentation of expected log format from emulator for comparison with Samsung device logs. Also documented that toggle operations are not logged (may be added in future).
- **Notes:** Expected log sequence shows 9 entries from Save button click through successful DNS enable. Toggle operations intentionally not logged currently - may add in future for debugging toggle issues.

### 2026-02-11 — Add DebugLogger and validation edge case unit tests

- **What:** Created `DebugLoggerTest.kt` with comprehensive unit tests for DebugLogger: circular buffer behavior (max entries, order preservation), `getRecentLogEntries()` edge cases (less/more/exactly total, zero, one, empty buffer, default parameter), `getAllLogs()` formatting, `getLogEntries()` list copy behavior, `clear()` functionality, and logging methods (`d()`/`e()` with and without throwables). Added 20+ validation edge case tests to `DnsManagerTest.kt`: IPv6 edge cases (brackets, compressed format, full format), hostname length limits (63/64 character labels, multiple labels), trimming behavior (leading/trailing/both spaces, only spaces), special characters (underscores, mixed case, single labels, numeric-only labels), IPv4 boundary values and invalid octets, and port detection edge cases (IPv6 with port, hostname with port).
- **Why:** User requested unit tests for DebugLogger (new code that could regress) and additional validation edge cases to catch potential issues with IPv6 handling, length limits, and trimming. Tests provide confidence in log management and validation logic.
- **Notes:** DebugLogger tests verify circular buffer works correctly and log retrieval methods behave as expected. Validation edge cases test boundary conditions and error paths. All tests follow existing patterns and use JUnit 4. Tests can be run with `./gradlew :app:testDebugUnitTest`.

### 2026-02-11 — Version bump to 0.4

- **What:** Updated `version.properties` (VERSION_NAME=0.4, VERSION_CODE=7). Added [0.4] entry to CHANGELOG.md.
- **Why:** User requested version update to 0.4 in preparation for manual release after PR and merge to main.
- **Notes:** Version code incremented from 6 to 7. Release will be done manually after PR merge.
