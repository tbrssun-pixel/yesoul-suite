<!-- generated_by: start-new-project; refined_by: codex -->

# Private Context

This file is for repo-private assumptions, internal notes, and local project-start findings. It is not public-safe by default.

## Private/Repo-Internal Sources

- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/**`
- `app/src/test/**`
- `tools/build-debug-apk.ps1`
- `tools/build-release-aab.ps1`
- `docs/design/**`
- `openspec/**`
- generated context under `docs/` and `knowledge/`

## Local-Only Markers

The following are ignored local build environment or output paths. Do not index their contents into public context and do not commit generated outputs:

- `.android-sdk/`
- `.build-tools/`
- `.gradle/`
- `.kotlin/`
- `build/`
- `app/build/`
- `local.properties`
- `keystore.properties`
- `*.jks`
- `*.keystore`
- `*.p12`
- `*.apk`
- `*.aab`
- `output/playwright/`

## Policy

Do not copy this content into public docs, demos, PR text, websites, or external tools without review.

Release signing variables are documented by name only in repo docs. Do not store keystore paths, aliases, passwords, Play credentials, or signed artifacts in the repository.
