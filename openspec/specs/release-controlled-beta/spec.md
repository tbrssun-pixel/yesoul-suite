# Release And Controlled Beta Specification

## Purpose

Define current v1 build, release bundle, and controlled beta gates.

## Requirements

### Requirement: Debug APK Build
The project SHALL support a local debug APK build for manual device testing.

#### Scenario: Debug helper runs
- GIVEN local Android tooling exists
- WHEN `.\tools\build-debug-apk.ps1 -SkipSdkInstall` succeeds
- THEN the debug APK is produced under `app\build\outputs\apk\debug\app-debug.apk`

### Requirement: Release AAB Validation
The project SHALL support release AAB task validation without upload signing secrets.

#### Scenario: Unsigned release validation runs
- GIVEN local Android tooling exists
- WHEN `.\tools\build-release-aab.ps1 -SkipSdkInstall -UnsignedCheckOnly` succeeds
- THEN the release bundle task is validated
- AND the artifact is not considered upload-ready

### Requirement: Release Signing Inputs
Signed release builds SHALL require signing inputs from environment variables only.

#### Scenario: Signed release is requested without secrets
- GIVEN `-UnsignedCheckOnly` is not used
- WHEN any required `OWL_BIKE_RELEASE_*` environment variable is missing
- THEN the release helper fails before treating the artifact as signed

### Requirement: Controlled Beta Gate
Production publication SHALL remain blocked until controlled beta prerequisites are satisfied and a separate release decision is made.

#### Scenario: Beta evidence is incomplete
- GIVEN fewer than the required testers or real bike-owner results are available
- WHEN production release is considered
- THEN production publication remains out of scope

#### Scenario: Release decision is absent
- GIVEN an AAB can be built
- WHEN no separate explicit release request exists
- THEN no Play upload or production publication is performed
