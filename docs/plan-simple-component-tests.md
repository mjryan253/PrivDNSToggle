# Plan: Simple component tests

## Unit tests vs instrumented tests

- **Unit tests** run on the JVM (no device/emulator). They exercise a single unit of logic in isolation. In this project, the only code that fits is **pure Kotlin with no Android APIs** — i.e. `DnsManager.validateHostnameSyntax()`. These are what people usually mean by "unit tests" and they run quickly (e.g. from Android Studio or `./gradlew test`).
- **Instrumented tests** run on a device or emulator. They use real `Context`, `ContentResolver`, etc. Anything that touches `Settings.Global`, `SharedPreferences`, or the UI lives here. They are slower and require a running Android target.

So: **yes, the validation tests are unit tests.** Any tests that involve saving/loading hostname or reading/writing Private DNS settings would be **instrumented tests** (or manual testing).

---

## What to test

| Component | Test type | Rationale |
|-----------|-----------|-----------|
| `DnsManager.validateHostnameSyntax()` | **Unit (JVM)** | Pure function, no Android; many edge cases; high regression risk when changing regex/rules. |
| `DnsManager.saveHostname` / `getSavedHostname` | Instrumented (optional) | Needs real or test `Context`; can assert round-trip in test app storage. |
| `DnsManager.enableDns` / `disableDns` / `toggle` | Manual or instrumented on real device | Depend on `Settings.Global` and `WRITE_SECURE_SETTINGS`; instrumented tests would mutate real system settings on the test device. |

Recommendation: **Start with unit tests for `validateHostnameSyntax` only.** That gives the most value with no device and no mocking. Add instrumented tests later if you want to lock in save/load behavior.

---

## To-do tasks (output and changes)

Each task below states **what will be produced or changed** and **where**.

### To-do 1: Add unit test dependencies in app/build.gradle.kts

- **Output:** New dependency lines in app/build.gradle.kts.
- **Changes:** testImplementation for junit:junit:4.13.2 and org.jetbrains.kotlin:kotlin-test-junit.
- **Result:** The test source set compiles and is runnable from Gradle / Android Studio.

### To-do 2: Create unit test source directory and DnsManagerTest.kt

- **Output:** New file app/src/test/kotlin/com/privdnstoggle/app/DnsManagerTest.kt.
- **Changes:** Create directory and test class with @Test methods calling DnsManager.validateHostnameSyntax().
- **Result:** A test class ready for the validation test cases.

### To-do 3: Add test cases for validateHostnameSyntax in DnsManagerTest.kt

- **Output:** Multiple @Test methods in DnsManagerTest.kt.
- **Changes:** Valid hostnames, IPv4, IPv6, empty/blank, scheme, path/slash, port, invalid format — assert null for valid and non-null error for invalid.
- **Result:** Running testDebugUnitTest passes when validation logic matches expectations; failures indicate regressions.

### To-do 4: Document how to run unit tests

- **Output:** Short documentation in README or docs.
- **Changes:** State that unit tests exist for hostname validation and how to run (Gradle task or Android Studio).
- **Result:** Future contributors know tests exist and how to run them.

### Optional: Instrumented test for save/load

- **Output:** app/src/androidTest/.../DnsManagerInstrumentedTest.kt and androidTestImplementation dependencies.
- **Changes:** Get Context via ApplicationProvider, call saveHostname then getSavedHostname, assert round-trip.
- **Result:** Save/load covered on device/emulator; no change to production code.

---

## Summary

- **Unit tests** = JVM-only, fast, for `validateHostnameSyntax()` — these are unit tests.
- **Instrumented tests** = on device, for code that uses Context/Android APIs; optional for save/load.
- Execute To-dos 1–4 to get unit tests and basic documentation; optional to-do adds instrumented coverage for persistence.
