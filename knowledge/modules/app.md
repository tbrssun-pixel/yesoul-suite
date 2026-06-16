---
title: "app"
generated_by: "start-new-project"
refined_by: "codex"
generated_at: "2026-06-11T19:23:20.447Z"
surface: "repo-private"
---

# app

## Purpose

Native Android application module for connecting to a YESOUL bike over BLE, parsing FTMS/CSC/Heart Rate telemetry, keeping the active bike connection visible through a foreground connected-device service, recording workout samples, exporting workouts, saving diagnostics, calculating race/goal/baseline state, and presenting the Compose UI.

## Boundary

- Surfaces: repo-private
- Public sharing: summarize behavior from `README.md` unless source-level details have been reviewed.

## Representative Files

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/owlbike/v1tracker/MainActivity.kt`
- `app/src/main/java/com/owlbike/v1tracker/TrackerViewModel.kt`
- `app/src/main/java/com/owlbike/v1tracker/ble/BleConnectionService.kt`
- `app/src/main/java/com/owlbike/v1tracker/ble/BleSessionManager.kt`
- `app/src/main/java/com/owlbike/v1tracker/ble/YesoulBleClient.kt`
- `app/src/main/java/com/owlbike/v1tracker/ble/BleParsers.kt`
- `app/src/main/java/com/owlbike/v1tracker/ble/BleModels.kt`
- `app/src/main/java/com/owlbike/v1tracker/ble/BleUuids.kt`
- `app/src/main/java/com/owlbike/v1tracker/data/WorkoutDatabase.kt`
- `app/src/main/java/com/owlbike/v1tracker/data/WorkoutExporters.kt`
- `app/src/main/java/com/owlbike/v1tracker/data/WorkoutRepository.kt`
- `app/src/main/java/com/owlbike/v1tracker/data/WorkoutDao.kt`
- `app/src/main/java/com/owlbike/v1tracker/race/GoalInputParser.kt`
- `app/src/main/java/com/owlbike/v1tracker/race/RaceModels.kt`
- `app/src/main/res/xml/export_file_paths.xml`
- `app/src/test/java/com/owlbike/v1tracker/ble/BleParsersTest.kt`
- `app/src/test/java/com/owlbike/v1tracker/data/WorkoutExportersTest.kt`
- `app/src/test/java/com/owlbike/v1tracker/race/GoalInputParserTest.kt`
- `app/src/test/java/com/owlbike/v1tracker/race/RaceModelsTest.kt`

## Terms

- BLE
- FTMS
- BikeMetrics
- foreground BLE service
- diagnostics
- workout session
- workout sample
- CSV/TCX export
- FileProvider export
- Room database v3
- race with shadow
- workout goal
- personal baseline
- POST_NOTIFICATIONS
- Jetpack Compose screen tabs

## Follow-Up

- Expand tests around BLE parser edge cases as new diagnostics arrive.
- Keep device-specific diagnostic captures out of public docs unless sanitized.
- Review runtime permission behavior when changing Bluetooth flows.
- Preserve explicit user action for CSV/TCX export and diagnostics sharing.
- Do not add automatic resistance control, cloud sync, accounts, Health Connect, Strava, payments, or subscriptions without a separate spec change.
