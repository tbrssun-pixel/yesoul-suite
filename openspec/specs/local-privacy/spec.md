# Local Privacy Specification

## Purpose

Define current v1 privacy and local-only behavior.

## Requirements

### Requirement: No Internet Permission
The app SHALL NOT request Android internet permission.

#### Scenario: Manifest is reviewed
- GIVEN the app manifest is inspected
- WHEN permissions are checked
- THEN `android.permission.INTERNET` is absent

### Requirement: Local Data Storage
The app SHALL keep workouts and diagnostic snapshots local by default.

#### Scenario: Workout is recorded
- GIVEN the user records a workout
- WHEN the workout is saved
- THEN session and sample data remain in the local app database

### Requirement: Explicit Sharing Only
The app SHALL share workout exports and diagnostics only after explicit user action.

#### Scenario: User shares diagnostics
- GIVEN diagnostics exist
- WHEN the user taps copy or share diagnostics
- THEN sanitized diagnostic content may leave the app through the selected Android action

#### Scenario: User shares workout export
- GIVEN a saved workout exists
- WHEN the user taps CSV or TCX export
- THEN the export may leave the app through the Android share sheet

### Requirement: Android Backup Disabled For App Database
The app SHALL exclude app database data from Android backup and device-transfer extraction rules.

#### Scenario: Backup rules are inspected
- GIVEN backup and data extraction rules are reviewed
- WHEN database rules are checked
- THEN database content is excluded from cloud backup and device transfer

### Requirement: No Third Party Tracking
The app SHALL NOT include accounts, analytics, ads, crash reporting, cloud sync, payments, or subscriptions in v1.

#### Scenario: Privacy copy is reviewed
- GIVEN user-facing privacy and Play compliance docs are reviewed
- WHEN current v1 scope is checked
- THEN those third-party and monetization features remain out of scope
