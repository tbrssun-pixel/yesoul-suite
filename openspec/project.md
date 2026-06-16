# Owl Bike V1 Tracker OpenSpec

## Purpose

Owl Bike V1 Tracker is a local-first native Android app for YESOUL Bike V1 / YS-003 testing. It scans for a compatible BLE/FTMS bike, shows workout telemetry, records local workouts, exports saved rides, provides sanitized diagnostics, and prepares for a controlled Google Play beta.

## Current Constraints

- Release package id is `com.owlbike.v1tracker`; debug package id is `com.owlbike.v1tracker.debug`.
- Data stays local unless the user explicitly shares diagnostics or a workout export.
- The app does not request internet access and does not include analytics, ads, crash reporting, accounts, cloud sync, payments, or subscriptions.
- Release signing uses only `OWL_BIKE_RELEASE_*` environment variables and requires a separate explicit release decision before upload.
- `output/playwright/`, build outputs, APK/AAB files, SDK/tool caches, keystores, and secrets are local-only.

## Domains

- `ble-connection-diagnostics`
- `workout-recording-history`
- `workout-export`
- `local-privacy`
- `release-controlled-beta`
- `race-goal-baseline`

## Explicit Non-Goals

Current v1 does not include automatic resistance control, cloud sync, accounts, Health Connect, Strava, payments, or subscriptions.
