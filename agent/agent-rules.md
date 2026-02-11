# Agent Rules for PrivDNSToggle

This document defines rules and guidelines for AI agents working on this Android project. Follow these patterns to maintain consistency and quality.

## Commit Messages

**When generating commit messages, keep them concise and focused:**

- **Short, descriptive subject line** (50-60 characters ideal, max 72)
- **Avoid verbose explanations** — the agent history file captures detailed context
- **Use imperative mood** (e.g., "Fix crash" not "Fixed crash" or "Fixes crash")
- **Focus on what changed**, not why (why goes in agent-history.md)

**Examples:**
- ✅ `Fix unsigned release APK signing`
- ✅ `Add unit tests for hostname validation`
- ✅ `Update version to 0.4`
- ❌ `This commit fixes the issue where the release APK was unsigned, which caused installation failures on Samsung devices...`

**Rationale:** Commit messages are for quick scanning; detailed context belongs in `agent/agent-history.md`.

---

## Version Management

**Always update version in `version.properties` (single source of truth):**

- Update both `VERSION_NAME` and `VERSION_CODE` together
- Increment `VERSION_CODE` by at least 1 for each release
- Follow semantic versioning for `VERSION_NAME` (e.g., `0.4`, `0.4.1`, `1.0.0`)
- Never hardcode versions in `app/build.gradle.kts` — it reads from `version.properties`

**When to bump version:**
- Before creating a release tag
- When preparing a PR that will be released
- After significant feature additions or bug fixes

---

## Agent History Documentation

**When making ANY code or configuration edits, append to `agent/agent-history.md`:**

- Use the entry format: **What**, **Why**, **Notes** (optional)
- Use dated heading: `### YYYY-MM-DD — Short title`
- Append at the end of the "History" section
- Be concise but include enough context for future agents

**This is mandatory** — see `.cursor/rules/agent-history.mdc` for details.

---

## Testing Requirements

**When adding or modifying validation logic, add unit tests:**

- `DnsManager.validateHostnameSyntax()` changes → update `DnsManagerTest.kt`
- `DebugLogger` changes → update `DebugLoggerTest.kt`
- Test edge cases: empty input, IPv6 formats, length limits, special characters
- Run tests before committing: `./gradlew :app:testDebugUnitTest`

**Test patterns:**
- Use JUnit 4 (`@Test`, `@RunWith(JUnit4::class)`)
- Test both valid and invalid inputs
- Test boundary conditions (e.g., 63 vs 64 character hostname labels)

---

## Android-Specific Patterns

**Error handling for Settings.Global access:**

- Wrap all `Settings.Global` reads/writes in try/catch
- Never let Settings operations crash the app
- Log errors via `DebugLogger.e()` with context
- Handle SecurityException gracefully (permission not granted)

**Example pattern:**
```kotlin
try {
    Settings.Global.putString(contentResolver, key, value)
} catch (e: Exception) {
    DebugLogger.e(TAG, "Failed to write setting: $key", e)
    // Handle gracefully, don't crash
}
```

**UI updates:**
- Use `withContext(Dispatchers.Main.immediate)` for UI state updates from coroutines
- Wrap Save button handlers in try/catch to prevent crashes

---

## Code Quality

**Follow existing patterns:**

- Use `DebugLogger.d()` and `DebugLogger.e()` for logging (not `Log.d()`)
- Keep functions focused and single-purpose
- Use descriptive variable names
- Add comments for complex logic (e.g., regex patterns, DNS-over-TLS port)

**Compose UI:**
- Use Material 3 components (`androidx.compose.material3`)
- Follow dark mode by default (dynamic color on Android 12+)
- Keep UI state management clear (use `remember`, `mutableStateOf` appropriately)

---

## Documentation Updates

**When adding features or changing behavior, update docs:**

- **README.md:** Update Features, Usage, or Requirements sections if behavior changes
- **docs/testing.md:** Update if test coverage changes
- **docs/troubleshooting.md:** Add device-specific issues or workarounds
- **quick-start.md:** Update if setup steps change

**Keep docs in sync with code** — outdated docs confuse users and future agents.

**Markdown file character encoding:**

- **All `*.md` files must use ASCII characters only** (no Unicode, emoji, or special characters)
- Use plain ASCII punctuation and symbols
- If non-ASCII characters are needed, request explicit approval first
- This ensures compatibility across all systems and tools

---

## Build Configuration

**Gradle dependencies:**

- Use Compose BOM `2023.10.01` (not `2024.01.00` — has KeyframesSpec API mismatch)
- Pin major versions for stability (e.g., `actions/checkout@v5`)
- Test build after dependency changes: `./gradlew :app:assembleDebug`

**Signing:**
- Release builds require signing config
- Supports `keystore.properties` for production keystore
- Falls back to debug keystore if `keystore.properties` missing
- Never commit keystore files or `keystore.properties` (already in `.gitignore`)

---

## Debug Features

**Debug logging system:**

- `DebugLogger` maintains circular buffer (last 100 entries)
- Use `DebugLogger.d(TAG, message)` for debug logs
- Use `DebugLogger.e(TAG, message, throwable)` for errors
- Debug menu in MainActivity shows last 50 entries
- Logs refresh every 500ms when debug mode enabled

**When debugging:**
- Check on-device debug logs first (via debug button)
- Combine with logcat for complete picture
- Document device-specific issues in `docs/troubleshooting.md`

---

## File Organization

**Project structure:**

- Kotlin source: `app/src/main/kotlin/com/privdnstoggle/app/`
- Tests: `app/src/test/kotlin/com/privdnstoggle/app/`
- Resources: `app/src/main/res/`
- Documentation: `docs/` directory
- Scripts: `scripts/` directory
- Version: `version.properties` at root
- Agent files: `agent/` directory

**Don't create:**
- Unnecessary subdirectories
- Duplicate documentation files
- Temporary or scratch files (use `.gitignore` patterns)

---

## Error Messages

**User-facing error messages:**

- Be clear and actionable
- Avoid technical jargon when possible
- For DNS validation errors, explain what's wrong (e.g., "Hostname cannot be empty", "Invalid hostname format")
- For permission errors, reference ADB command in troubleshooting docs

**Example:**
- ✅ "Hostname cannot be empty"
- ✅ "Connection test failed. Check your internet connection and DNS hostname."
- ❌ "Error code 42: Invalid input parameter"

---

## Summary Checklist

Before committing changes, verify:

- [ ] Entry added to `agent/agent-history.md`
- [ ] Tests added/updated (if validation logic changed)
- [ ] Documentation updated (if behavior changed)
- [ ] All `*.md` files use ASCII characters only
- [ ] Commit message is concise
- [ ] Code follows error handling patterns (try/catch for Settings)
- [ ] Debug logging added for new operations
- [ ] Build succeeds (`./gradlew :app:assembleDebug`)

---

**Remember:** This is a minimal Android app focused on reliability and simplicity. Keep changes focused, test thoroughly, and document clearly.
