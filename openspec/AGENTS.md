# OpenSpec Instructions

This directory contains the current behavior contract for Owl Bike V1 Tracker.

## Rules

- Keep `openspec/specs/` aligned with behavior that exists in the Android app and reviewed docs.
- Do not document future scope as v1 behavior.
- Use change folders under `openspec/changes/` only for proposed behavior changes.
- Do not include secrets, keystore values, Play credentials, raw diagnostics, APK/AAB outputs, or `output/playwright/` screenshots.
- Validate specs with:

```powershell
openspec validate --specs --strict --no-interactive
```

## Out Of Scope For Current v1

- automatic resistance control
- cloud sync
- accounts
- Health Connect
- Strava
- payments
- subscriptions
