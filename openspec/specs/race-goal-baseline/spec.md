# Race Goal And Baseline Specification

## Purpose

Define current v1 behavior for goals, personal baseline, and race-with-shadow state.

## Requirements

### Requirement: Goal Types
The app SHALL support distance goals, calorie goals, and riding without a goal.

#### Scenario: Manual distance goal is entered
- GIVEN the user enters a positive distance value
- WHEN the goal is confirmed
- THEN the ride uses a distance goal measured in meters

#### Scenario: Manual calorie goal is entered
- GIVEN the user enters a positive calorie value
- WHEN the goal is confirmed
- THEN the ride uses a calorie goal

#### Scenario: No goal is selected
- GIVEN the user chooses to ride without a goal
- WHEN recording starts
- THEN the workout is recorded without an active goal

### Requirement: Personal Baseline
The app SHALL compute personal baseline values from finished local workouts.

#### Scenario: Finished rides exist
- GIVEN finished sessions with positive distance or calories and duration exist
- WHEN baseline is computed
- THEN the app derives median metric values, median durations, streak data, best ride distance, and recent ride count

#### Scenario: No qualifying rides exist
- GIVEN no finished sessions with qualifying metric data exist
- WHEN baseline is computed
- THEN distance and calorie baseline values remain unavailable

### Requirement: Median Default Goal
The app SHALL offer a median-based default goal when a qualifying baseline exists.

#### Scenario: Distance baseline exists
- GIVEN a distance baseline is available
- WHEN the default goal is requested
- THEN the default goal uses median distance

### Requirement: Race With Shadow
The app SHALL activate the shadow race only when a valid goal, matching baseline metric, and baseline duration exist.

#### Scenario: Shadow race is active
- GIVEN a valid goal and matching baseline exist
- WHEN elapsed time and current metrics update
- THEN the app compares current progress against linear median pace capped by the goal and baseline metric

#### Scenario: Baseline is missing
- GIVEN a goal exists but no matching baseline exists
- WHEN race state is calculated
- THEN the shadow race remains inactive and the app can still track goal progress

### Requirement: Ahead Behind Zones
The app SHALL classify race state as ahead, behind, or neutral using hysteresis around the progress gap.

#### Scenario: User moves ahead
- GIVEN the previous zone is neutral
- WHEN the user exceeds the shadow pace threshold
- THEN the race zone becomes ahead

#### Scenario: User returns near pace
- GIVEN the previous zone is ahead or behind
- WHEN the progress gap returns within the exit threshold
- THEN the race zone returns toward neutral
