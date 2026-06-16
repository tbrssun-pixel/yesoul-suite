<!-- generated_by: start-new-project; refined_by: codex -->

# Deploy Readiness

Status: Android debug APK plus release AAB helper for future Google Play controlled beta.

This document is assessment and runbook material only. It does not authorize live deployment or external distribution.

## Policy

Live Play Store publication, external APK distribution, production credentials, signing key changes, CI environment changes, remote infrastructure actions, and certificate changes require a separate explicit user request.

## Detected Evidence

- `README.md` documents building and manually installing a debug APK.
- `tools/build-debug-apk.ps1` installs local Android tooling, runs tests, and builds the debug APK.
- `tools/build-release-aab.ps1` runs tests and builds the release AAB.
- `.gitignore` excludes local SDK/tool caches, build outputs, keystores, APKs, AABs, and `output/playwright/` review screenshots.

## Environments

| Environment | Target | Notes |
| --- | --- | --- |
| Local build | `app\build\outputs\apk\debug\app-debug.apk` | Debug package id is `com.owlbike.v1tracker.debug`. |
| Manual device test | Xiaomi Android phone + YESOUL BIKE V1 / YS-003 | Follow the README first-test checklist. |
| Release AAB validation | `app\build\outputs\bundle\release\app-release.aab` | Use `-UnsignedCheckOnly` only to validate the Gradle bundle task. |
| Google Play controlled beta | Signed release AAB | Requires verified Play Console account, Play declarations, tester countries, and signed upload artifact. |

## Required Secrets

List secret names only, never values.

- Release keystore path/name: `OWL_BIKE_RELEASE_STORE_FILE`
- Keystore alias name: `OWL_BIKE_RELEASE_KEY_ALIAS`
- Keystore password variable name: `OWL_BIKE_RELEASE_STORE_PASSWORD`
- Key password variable name: `OWL_BIKE_RELEASE_KEY_PASSWORD`
- Store/distribution credential names: TBD, only if external distribution is added

## Safe Checks

```powershell
.\tools\build-debug-apk.ps1 -SkipSdkInstall
```

Signed release bundle:

```powershell
.\tools\build-release-aab.ps1 -SkipSdkInstall
```

Unsigned local bundle task validation:

```powershell
.\tools\build-release-aab.ps1 -SkipSdkInstall -UnsignedCheckOnly
```

For a fresh local setup:

```powershell
.\tools\build-debug-apk.ps1
```

## Health Check

Manual smoke test from `README.md`:

1. Install the debug APK on the Xiaomi phone.
2. Grant Bluetooth permissions.
3. Scan and connect to the YESOUL device.
4. Open Diag and confirm services are detected.
5. Start a workout, pedal for 30-60 seconds, finish it, and confirm History contains saved samples.

## Google Play Gate

- Use app name `Owl Bike V1 Tracker` and package id `com.owlbike.v1tracker`.
- Complete `docs/play-compliance.md`.
- Host `docs/privacy-policy.md` at a public, non-PDF URL and add the public privacy contact.
- Use `docs/store-listing.md` for RU/EN listing copy and screenshot scope.
- Run internal testing before closed testing.
- For personal/new accounts, plan for at least 12 opted-in closed testers for 14 days before production access.
- Production request waits for at least 3 real YESOUL Bike V1 / YS-003 owners to submit a successful workout export or sanitized diagnostics.
- Keep production publication blocked until a separate explicit release decision is made.

## Rollback

- For local/manual debug testing, reinstall the previous known-good APK if available.
- For future Play distribution, use staged rollout controls and halt rollout if Play vitals, tester feedback, BLE connection, export, or diagnostics issues are blocking.
