<!-- generated_by: start-new-project; refined_by: codex -->

# Public Context

This file is the starting point for material safe to share with public agents, contractors, PR descriptions, demos, websites, and support conversations. Review before publishing.

## Project

- Name: Owl Bike V1 Tracker
- Product: native Android tracker APK compatible with YESOUL BIKE V1 / YS-003 testing.
- Stack: Kotlin, Android Gradle Plugin, Jetpack Compose, AndroidX Lifecycle, Room, Kotlin coroutines, BLE/FTMS telemetry.
- Current distribution mode: debug APK for manual installation plus release AAB preparation for controlled Google Play beta.
- Package ids: `com.owlbike.v1tracker` for release, `com.owlbike.v1tracker.debug` for debug.

## Public-Safe Sources

- `README.md`
- `LICENSE`

## Public-Safe Summary

The project builds a local-first Android app that scans for and connects to a compatible BLE/FTMS indoor bike, reads fitness-machine telemetry, records workouts, stores samples/history locally, exports CSV/TCX, keeps the BLE connection alive through a foreground connected-device service, and exposes sanitized diagnostics for missing metric investigation. It is not an official YESOUL app and is not affiliated with YESOUL.

## Do Not Include

- Secrets, tokens, passwords, private keys, keystore files, signing credentials, or credential values.
- Private URLs with credentials.
- Customer data, payment data, private access notes, or raw device diagnostics unless explicitly sanitized.
- Ignored local tool/cache directories such as `.android-sdk/`, `.build-tools/`, `.gradle/`, or build outputs.
- Local review artifacts such as `output/playwright/` screenshots unless explicitly selected and reviewed.
- Local-only assumptions from `knowledge/private/` unless explicitly reviewed.
