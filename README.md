# Owl Bike V1 Tracker

Native Kotlin Android tracker for YESOUL BIKE V1 / YS-003.

Owl Bike V1 Tracker is a local-first tracker and diagnostic helper: no account, no subscription, no ads, no analytics, and no cloud service. It is intended for first users who want to verify which metrics their bike actually exposes, record a workout, and export the data for analysis.

This is not an official YESOUL app and is not affiliated with YESOUL.

## Current Capabilities

- Scan for nearby BLE fitness devices and connect to the YESOUL bike.
- Reconnect to bikes that were successfully connected before.
- Read FTMS / Cycling Speed and Cadence / Heart Rate telemetry when exposed by the device.
- Show speed, cadence, power, heart rate, distance, calories, and current reported load.
- Record workout samples locally in the Android app database.
- Review history with session summaries, averages, recent samples, and a 7-day summary.
- Export a saved workout as CSV or TCX from the History detail screen.
- Copy or share a sanitized diagnostics bundle for compatibility testing.

## Compatibility

| Device | Status | Notes |
| --- | --- | --- |
| YESOUL BIKE V1 / YS-003 | Verified target | Observed as `YSV100637`; exposes FTMS telemetry, including current manual resistance. |
| Other YESOUL / FTMS bikes | Unconfirmed | Use the Diag screen and share sanitized diagnostics before claiming support. |

Physical resistance on the verified YS-003 is adjusted manually with the bike's frame knob. The app does not send resistance-control commands or claim automatic resistance.

## Build

```powershell
.\tools\build-debug-apk.ps1
```

The script installs local Android command-line tools into `.android-sdk`, local Gradle into `.build-tools`, runs unit tests, and builds:

```text
app\build\outputs\apk\debug\app-debug.apk
```

For repeat builds after SDK setup:

```powershell
.\tools\build-debug-apk.ps1 -SkipSdkInstall
```

## Install

Copy or send `app-debug.apk` to the Xiaomi phone and install it manually. Android will show it as a debug build with package id:

```text
com.owlbike.v1tracker.debug
```

Allow Bluetooth permissions on first launch. If Android blocks installation, enable installation from the app used to open the APK.

## First Test

1. Turn on Bluetooth and power the bike.
2. Open Owl Bike V1 Tracker.
3. Grant Bluetooth permissions.
4. Scan and connect to the YESOUL device.
5. Open Diag and check detected services.
6. Start a workout, pedal for 30-60 seconds, then finish it.
7. Check History for saved samples, session summary, and CSV/TCX export.
8. In Diag, check Compatibility and detected services.
9. If any metric is missing, tap `Share diagnostics` or `Copy diagnostics` and send the sanitized text.

YESOUL BIKE V1 / YS-003 observed as `YSV100637` exposes FTMS telemetry, including current manual resistance. Physical resistance is adjusted manually with the bike's frame knob, so the app does not send resistance-control commands.

## Useful Follow-up Data

If speed/cadence/power/resistance does not appear, collect:

- sanitized text from the Diag tab;
- screenshot of the Diag tab if copying is unavailable;
- nRF Connect export or screenshots with services, characteristics, and properties;
- notification values while pedaling for 30-60 seconds;
- Xiaomi Android version.

## Privacy

Workout history and diagnostic snapshots are stored locally on the device. Android backup for the app database is disabled. Export and diagnostics sharing are explicit user actions. The diagnostics text hides the Bluetooth device address before sharing.

The app does not request internet access and does not include analytics, ads, crash reporting, accounts, Health Connect, Strava integration, cloud sync, payments, or subscriptions.

## Release Bundle

Google Play releases use the neutral package id:

```text
com.owlbike.v1tracker
```

Release signing is configured from environment variables only. Do not commit keystores or passwords.

Required variables for a signed release bundle:

```powershell
$env:OWL_BIKE_RELEASE_STORE_FILE="E:\path\to\upload-key.jks"
$env:OWL_BIKE_RELEASE_STORE_PASSWORD="..."
$env:OWL_BIKE_RELEASE_KEY_ALIAS="..."
$env:OWL_BIKE_RELEASE_KEY_PASSWORD="..."
```

Build a signed release AAB:

```powershell
.\tools\build-release-aab.ps1 -SkipSdkInstall
```

Build an unsigned local bundle only to validate the release task:

```powershell
.\tools\build-release-aab.ps1 -SkipSdkInstall -UnsignedCheckOnly
```

Never upload an unsigned or debug-signed artifact to Google Play.

## First User Validation

The useful validation target is a controlled Google Play beta with at least 12 testers and at least 3 real YESOUL Bike V1 / YS-003 owners. Success for this phase means at least three bike owners can install the app, connect to their bike, record a short workout, and send either a CSV/TCX export or sanitized diagnostics.

Production publication waits for a verified Play Console account, completed Play declarations, closed-test evidence when required, and a separate explicit release decision.

## License

MIT. See [LICENSE](LICENSE).
