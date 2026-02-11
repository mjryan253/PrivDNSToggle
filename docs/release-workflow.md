# Release workflow (tag and branch convention)

This doc defines how tag-based workflows treat **beta** vs **production** releases.

**Note:** GitHub Actions workflows are planned but not yet implemented. Currently, releases must be created manually using the scripts in `scripts/` or GitHub CLI. This document describes the intended workflow behavior.

## Branch and tag convention

- **Beta releases:** Use the **beta** branch and tag with pattern **`beta/v*`** (e.g. `beta/v0.3.1`).
  - When workflows are implemented: Pushing a tag like `beta/v0.3.1` (from the `beta` branch or any branch) → build and create a **GitHub Release with prerelease = true** (beta).
  - Currently: Create releases manually using `scripts/upload-release.ps1` or GitHub CLI.
- **Production releases:** Use the **main** branch and tag with pattern **`v*`** (e.g. `v0.4`).
  - When workflows are implemented: Pushing a tag like `v0.4` **from `main`** → build and create a **GitHub Release with prerelease = false** (production).
  - Currently: Create releases manually using `scripts/upload-release.ps1` or GitHub CLI.

## Workflow trigger and logic

**Trigger (GitHub Actions):**

```yaml
on:
  push:
    tags:
      - 'v*'
      - 'beta/v*'
```

**Decide beta vs production:**

1. If the tag **matches `beta/v*`** (e.g. `beta/v0.3.1`) → **beta** → `prerelease: true`.
2. Else if the tag matches **`v*`** and the tagged commit is on **`origin/main`** → **production** → `prerelease: false`.
3. Else (e.g. tag `v*` not on main) → treat as **beta** → `prerelease: true`, or **fail** the job to enforce “production only from main”.

Recommended: for tags `v*` that are **not** on `origin/main`, either mark as prerelease (beta) or fail with a clear message so production is only ever from main.

## Summary

| Tag pattern | Branch (tagged commit) | Result        |
|-------------|------------------------|---------------|
| `beta/v*`   | any                    | Beta (prerelease) |
| `v*`        | main                   | Production (full release) |
| `v*`        | not main               | Beta or fail (configurable) |
