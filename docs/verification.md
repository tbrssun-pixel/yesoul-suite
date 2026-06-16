<!-- generated_by: start-new-project; refined_by: codex -->

# Verification

## Primary Command

Use this when the local Android SDK and Gradle cache already exist:

```powershell
.\tools\build-debug-apk.ps1 -SkipSdkInstall
```

## Full Setup And Build

Use this on a fresh machine or after clearing `.android-sdk/` or `.build-tools/`:

```powershell
.\tools\build-debug-apk.ps1
```

The helper installs local Android command-line tools and Gradle if needed, runs unit tests, and builds:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Release Bundle Validation

Use this to validate the release bundle task without upload signing secrets:

```powershell
.\tools\build-release-aab.ps1 -SkipSdkInstall -UnsignedCheckOnly
```

Use this only when the `OWL_BIKE_RELEASE_*` signing environment variables are present:

```powershell
.\tools\build-release-aab.ps1 -SkipSdkInstall
```

The signed release path is:

```text
app\build\outputs\bundle\release\app-release.aab
```

Never upload an unsigned or debug-signed artifact to Google Play.

## Focused Test Target

If Gradle is already available through the helper environment, the relevant unit test coverage is currently centered on:

```text
app\src\test\java\com\owlbike\v1tracker\ble\BleParsersTest.kt
app\src\test\java\com\owlbike\v1tracker\data\WorkoutExportersTest.kt
app\src\test\java\com\owlbike\v1tracker\race\GoalInputParserTest.kt
app\src\test\java\com\owlbike\v1tracker\race\RaceModelsTest.kt
```

## OpenSpec Validation

Validate current-state behavior specs:

```powershell
openspec validate --specs --strict --no-interactive
```

## Bootstrap Validation

Validate the generated context pack against the root `AGENTS.md`:

```powershell
node "$env:USERPROFILE\.codex\skills\start-new-project\scripts\start-new-project.mjs" --target . --mode validate --overwrite-agents
```

`--overwrite-agents` is intentional here: it tells the validator to treat the root `AGENTS.md` as the accepted project instruction file instead of expecting a separate `docs/start-new-project/AGENTS.proposed.md` artifact.

Expected result today: `warn`, because Understand Anything is missing; all file, secret-scan, RAG, deploy-readiness, and skeptical-review checks should pass.
