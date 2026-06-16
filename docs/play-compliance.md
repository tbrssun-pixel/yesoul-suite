# Play Compliance Pack

Status: draft for controlled beta. Production upload still requires a verified Play Console account and current-console review.

## App Identity

- Public app name: Owl Bike V1 Tracker
- Package id: `com.owlbike.v1tracker`
- App category: Health & Fitness
- Pricing: free
- Ads: no
- In-app purchases: no
- Accounts: no
- Cloud sync: no
- Analytics/crash SDKs: no
- Internet permission: not requested. Confirmed by `AndroidManifest.xml`: no `android.permission.INTERNET` entry.
- Brand disclosure: compatible with YESOUL Bike V1 / YS-003; not an official YESOUL app and not affiliated with YESOUL.

## Data Safety Draft

Developer collection:

- Collected by developer: no
- Shared by developer: no
- Data processed off-device by the app: no

Local-only data:

- Workout sessions and samples stay in the app database on the device.
- BLE diagnostics stay in the app database on the device.
- Android backup is disabled for app data.

User-initiated sharing:

- CSV/TCX workout export is created only after the user taps export/share.
- Diagnostics text is shared only after the user taps copy/share diagnostics.
- Diagnostics text hides the Bluetooth device address.

Security notes:

- No account credentials.
- No server transport.
- No advertising ID.
- No third-party telemetry SDKs.

## Health Apps Declaration Draft

Declare the app as a health/fitness app because it records cycling workout metrics such as speed, cadence, power, heart rate, distance, calories, and resistance/load when exposed by the bike.

Recommended declaration language:

```text
Owl Bike V1 Tracker is a local-first cycling workout tracker and diagnostics helper for compatible BLE/FTMS exercise bikes. It displays and records workout telemetry locally on the user's device. It is not a medical device, does not diagnose, treat, cure, or prevent any medical condition, and does not provide medical advice.
```

Do not declare:

- Health Connect access
- medical device functionality
- human-subject research
- disease prevention or public health
- background health data collection

## Permissions Declaration

Requested permissions:

- `BLUETOOTH_SCAN`
- `BLUETOOTH_CONNECT`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_CONNECTED_DEVICE`
- `POST_NOTIFICATIONS`
- legacy `BLUETOOTH`, `BLUETOOTH_ADMIN`, and `ACCESS_FINE_LOCATION` with max SDK 30

Reasoning:

- Bluetooth permissions are required to scan for and connect to the bike.
- Android 11 and below require foreground location permission for BLE scanning.
- The app does not request background location and does not record coordinates.
- Foreground service permissions are required to keep the active bike connection visible as a connected-device foreground service.
- Notification permission is required on Android 13+ so the foreground connection notification can be shown.
- Internet permission is not requested; the app has no server transport, cloud sync, ads, analytics, or crash-reporting SDK.

## Target Audience And Content

- Target audience: adults and older teens using indoor exercise bikes.
- Do not target children.
- No ads.
- No user-generated content.
- No social features.
- No purchases or subscriptions.

## Release Gate

Before production request:

- Signed AAB built by `tools/build-release-aab.ps1`.
- Internal test uploaded and reviewed.
- Closed test completed if Play Console requires it.
- At least 12 opted-in testers for 14 days when required by account type.
- At least 3 real YESOUL Bike V1 / YS-003 owners record workouts and submit CSV/TCX or diagnostics.
- No Play policy warnings, blocking crashes, misleading listing copy, or unsupported feature claims.
