# BLE Connection And Diagnostics Specification

## Purpose

Define current v1 behavior for BLE scan/connect, telemetry discovery, diagnostics, and foreground connection visibility.

## Requirements

### Requirement: BLE Device Discovery
The app SHALL scan for nearby BLE fitness devices only after the user grants required Bluetooth permissions.

#### Scenario: Bike appears during scan
- GIVEN Bluetooth is enabled and permissions are granted
- WHEN the user starts scanning near the bike
- THEN the app lists nearby devices with available name, address, RSSI, and advertised services

#### Scenario: No devices are found
- GIVEN scan permissions are granted
- WHEN no nearby compatible device is detected
- THEN the app remains usable and shows an empty nearby-device state

### Requirement: Bike Connection
The app SHALL connect to a selected BLE device and expose connection state to the UI.

#### Scenario: Connection succeeds
- GIVEN a user selects a listed bike
- WHEN the BLE connection and service discovery succeed
- THEN the app reports a connected state and displays available capabilities

#### Scenario: Connected bike is remembered
- GIVEN a BLE device reaches a successful connected state
- WHEN the app stores local connection history
- THEN the device is remembered locally with address, optional name, last connected time, optional RSSI, and advertised services

#### Scenario: User reconnects to a remembered bike
- GIVEN the app has a previously connected bike and Bluetooth permissions are granted
- WHEN the user selects that remembered bike
- THEN the app attempts to connect by stored address without requiring a new scan result

#### Scenario: Connection is stopped
- GIVEN the bike connection is active
- WHEN the user disconnects from the UI or notification action
- THEN the app disconnects and clears the active connected state

### Requirement: Telemetry Parsing
The app SHALL read FTMS, Cycling Speed and Cadence, and Heart Rate telemetry when those services and notifications are exposed by the device.

#### Scenario: FTMS bike data is available
- GIVEN the connected bike exposes Indoor Bike Data
- WHEN notification values arrive while the user pedals
- THEN the app updates speed, cadence, power, distance, calories, and reported load when present

#### Scenario: Heart rate is available
- GIVEN the connected device exposes Heart Rate Measurement
- WHEN heart-rate notifications arrive
- THEN the app updates heart rate in live metrics and recorded samples

### Requirement: Foreground Connection Visibility
The app SHALL use a foreground connected-device service for the active BLE connection state.

#### Scenario: Connection is kept active
- GIVEN keep-alive is active and the bike is connected or connecting
- WHEN Android requires foreground service visibility
- THEN the app shows a low-priority connection notification with a disconnect action

### Requirement: Diagnostics
The app SHALL provide sanitized diagnostics for compatibility testing.

#### Scenario: User shares diagnostics
- GIVEN diagnostic text has been captured
- WHEN the user copies or shares diagnostics
- THEN the diagnostics hide the Bluetooth device address before leaving the app
