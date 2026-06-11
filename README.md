# YESOUL Tracker APK

Native Kotlin Android tracker for YESOUL BIKE V1 / YS-003.

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
com.yesoulsuite.tracker.debug
```

Allow Bluetooth permissions on first launch. If Android blocks installation, enable installation from the app used to open the APK.

## First Test

1. Turn on Bluetooth and power the bike.
2. Open YESOUL Tracker.
3. Grant Bluetooth permissions.
4. Scan and connect to the YESOUL device.
5. Open Diag and check detected services.
6. Start a workout, pedal for 30-60 seconds, then finish it.
7. Check History for saved samples.
8. In Diag, tap `Copy diagnostics` and send the copied text if any metric is missing.

YESOUL BIKE V1 / YS-003 observed as `YSV100637` exposes FTMS telemetry, including current manual resistance. Physical resistance is adjusted manually with the bike's frame knob, so the app does not send resistance-control commands.

## Useful Follow-up Data

If speed/cadence/power/resistance does not appear, collect:

- copied text from the Diag tab;
- screenshot of the Diag tab if copying is unavailable;
- nRF Connect export or screenshots with services, characteristics, and properties;
- notification values while pedaling for 30-60 seconds;
- Xiaomi Android version.

## License

MIT. See [LICENSE](LICENSE).
