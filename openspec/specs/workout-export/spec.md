# Workout Export Specification

## Purpose

Define current v1 behavior for user-initiated CSV and TCX exports from saved workouts.

## Requirements

### Requirement: CSV Export
The app SHALL export saved workout samples as CSV after explicit user action.

#### Scenario: User shares CSV
- GIVEN a saved workout session has samples
- WHEN the user taps CSV export
- THEN the app creates a CSV file with timestamp, elapsed time, speed, cadence, power, heart rate, resistance, distance, and calories columns
- AND the user chooses the destination through the Android share sheet

### Requirement: TCX Export
The app SHALL export saved workout data as a TCX biking activity after explicit user action.

#### Scenario: User shares TCX
- GIVEN a saved workout session has samples
- WHEN the user taps TCX export
- THEN the app creates a TCX file with activity, lap, trackpoint, heart rate, cadence, speed, and power data when available
- AND the user chooses the destination through the Android share sheet

### Requirement: FileProvider Sharing
The app SHALL share export files through the configured Android FileProvider.

#### Scenario: Export file is shared
- GIVEN an export file has been created in the app cache export path
- WHEN the share intent is launched
- THEN the receiving app receives a content URI with temporary read permission

### Requirement: No Automatic Upload
The app SHALL NOT upload workout exports automatically.

#### Scenario: User does not export
- GIVEN a saved workout exists
- WHEN the user takes no export action
- THEN no CSV or TCX file is shared outside the app
