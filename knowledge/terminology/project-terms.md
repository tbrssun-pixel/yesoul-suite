---
title: "Project Terms"
generated_by: "start-new-project"
refined_by: "codex"
generated_at: "2026-06-11T19:23:20.447Z"
---

# Project Terms

Treat this as a starting vocabulary, not a final glossary.

| Term | Meaning | Sources |
| --- | --- | --- |
| Owl Bike V1 Tracker | Native Android tracker app for YESOUL BIKE V1 / YS-003. | `README.md` |
| YESOUL BIKE V1 / YS-003 | Target exercise bike model observed as `YSV100637`. | `README.md` |
| FTMS | Bluetooth fitness-machine telemetry source used for bike metrics. | `README.md`, `app/src/main/java/com/owlbike/v1tracker/ble/**` |
| BLE | Bluetooth Low Energy connection layer for scanning, connecting, diagnostics, and notifications. | `app/src/main/java/com/owlbike/v1tracker/ble/**` |
| BikeMetrics | App-level metric model emitted from BLE parsing and consumed by workout recording/UI state. | `app/src/main/java/com/owlbike/v1tracker/ble/BleModels.kt`, `TrackerViewModel.kt` |
| Workout session | Persisted workout summary with start/end state and aggregate metrics. | `app/src/main/java/com/owlbike/v1tracker/data/**` |
| Workout sample | Persisted metric sample collected during an active workout. | `app/src/main/java/com/owlbike/v1tracker/data/**` |
| Diagnostics | Captured service/characteristic data and control messages used to debug missing metrics. | `README.md`, `TrackerViewModel.kt` |
| Jetpack Compose | UI framework used by `MainActivity.kt`. | `app/build.gradle.kts`, `MainActivity.kt` |
| Room schema v3 | Local persistence schema for workout sessions, samples, diagnostics, remembered BLE devices, and nullable goal/baseline snapshot fields. | `app/src/main/java/com/owlbike/v1tracker/data/WorkoutDatabase.kt`, `WorkoutEntities.kt` |
| CSV/TCX export | User-initiated export formats generated from saved workout samples and shared through Android. | `app/src/main/java/com/owlbike/v1tracker/data/WorkoutExporters.kt`, `MainActivity.kt` |
| FileProvider export | Android provider used to share temporary export files from the app cache without exposing raw file paths. | `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/export_file_paths.xml` |
| Foreground BLE service | Connected-device foreground service that keeps the active bike connection visible and provides a disconnect action. | `app/src/main/java/com/owlbike/v1tracker/ble/BleConnectionService.kt`, `AndroidManifest.xml` |
| POST_NOTIFICATIONS | Android 13+ runtime permission required for the foreground connection notification. | `app/src/main/AndroidManifest.xml`, `docs/play-compliance.md` |
| Race with shadow | Live ride model comparing current progress against a linear median pace when a valid goal and baseline exist. | `app/src/main/java/com/owlbike/v1tracker/race/RaceModels.kt`, `docs/design/owl-v1-dark-dashboard-handoff.md` |
| Workout goal | Distance, calorie, or no-goal target selected from personal median or manual input. | `app/src/main/java/com/owlbike/v1tracker/race/GoalInputParser.kt`, `RaceModels.kt` |
| Personal baseline | Median distance/calories/duration and streak data computed from finished local workout sessions. | `app/src/main/java/com/owlbike/v1tracker/race/RaceModels.kt` |
| build-debug-apk | PowerShell helper that installs local tooling if needed, runs tests, and builds a debug APK. | `README.md`, `tools/build-debug-apk.ps1` |
| build-release-aab | PowerShell helper that runs tests and builds the release AAB, with `-UnsignedCheckOnly` for local validation without signing secrets. | `README.md`, `tools/build-release-aab.ps1` |
| Controlled beta | Planned Google Play validation phase before production, gated by tester evidence and Play Console prerequisites. | `README.md`, `docs/deploy.md`, `docs/play-compliance.md` |
| Release AAB | Google Play bundle artifact for package id `com.owlbike.v1tracker`; signed builds require `OWL_BIKE_RELEASE_*` environment variables. | `app/build.gradle.kts`, `docs/deploy.md` |
| Design prototype | Repo-private HTML/handoff artifact for the dark dashboard UI direction, not a runtime web surface. | `docs/design/owl-v1-dark-dashboard-prototype.html`, `docs/design/owl-v1-dark-dashboard-handoff.md` |

## Unknowns

- Confirm Play Console account type, public privacy URL, tester countries, and closed-test roster.
- Add aliases used by real users, device diagnostics, and UI copy.
- Update terminology when new BLE services or metrics are supported.
