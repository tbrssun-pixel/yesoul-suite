# Owl V1 Tracker Dark Dashboard Prototype Handoff

## Status

- Open Design release used: `open-design-v0.10.0`.
- Windows desktop setup asset downloaded from the official GitHub release and installed into:
  `C:\Users\TiberiusSunD17\AppData\Local\Programs\Open Design`.
- Portable asset was also unpacked into:
  `C:\Users\TiberiusSunD17\AppData\Local\OpenDesignPortable\open-design-0.10.0`.
- Desktop app launches, but the automated daemon/artifact loop could not be used from this session:
  - portable app failed with `Error loading V8 startup snapshot file`;
  - installed app launched, but no local daemon port was detected;
  - packaged `daemon-cli.mjs` requires Node ABI `145`, while the local Node is `22.18.0` and reports ABI `127`.
- Fallback artifact created:
  `docs/design/owl-v1-dark-dashboard-prototype.html`.

## Open Design Prompt

Use this prompt in Open Design Desktop once its daemon/agent runtime is available:

```text
Create a dark cockpit mobile prototype for Owl V1 Tracker, a native Android local-first tracker for YESOUL BIKE V1 / YS-003.

Use a ride-cockpit visual direction: carbon black background, high-contrast telemetry, emerald primary action, cyan secondary telemetry accents, amber warning states, red danger states, 8px panel radius, dense but readable dashboard layout.

Design four core screens:
1. Connect: Bluetooth permissions, scan/connect controls, nearby devices, RSSI, connection status.
2. Live Ride: Race With Shadow cockpit with two parallel progress tracks (`You today` and `Your median`), a moving time-based median ghost, a primary distance/calories goal card, HR safety card, power/cadence pace card, disabled remote load control, and protected finish controls.
3. Finish / History Detail: post-ride triumph state, median delta, streak, session summary, sparkline/sample preview, CSV and TCX share actions.
4. Diagnostics: compatibility status, detected FTMS/CSC services, sanitized diagnostics copy/share actions.

Do not invent cloud sync, accounts, analytics, subscriptions, automatic resistance control, Strava, Health Connect, internet features, or audio cues in the MVP.
Keep the UI native mobile, touch-first, accessible, and readable at 360x800, 393x873, and 412x915.
```

## Prototype Contract

- Prototype file: `docs/design/owl-v1-dark-dashboard-prototype.html`.
- Supported query params:
  - `screen=connect|ride|history|diagnostics`
  - `state=disconnected|scanning|connected|recording|paused|empty|behind|hralert|finishconfirm|goalreached|loadlocked`
- Review examples:
  - `docs/design/owl-v1-dark-dashboard-prototype.html?screen=ride&state=recording`
  - `docs/design/owl-v1-dark-dashboard-prototype.html?screen=ride&state=behind`
  - `docs/design/owl-v1-dark-dashboard-prototype.html?screen=ride&state=hralert`
  - `docs/design/owl-v1-dark-dashboard-prototype.html?screen=ride&state=finishconfirm`
  - `docs/design/owl-v1-dark-dashboard-prototype.html?screen=history&state=empty`
  - `docs/design/owl-v1-dark-dashboard-prototype.html?screen=diagnostics&state=connected`

## Compose Implementation Notes

- 2026-06-16 update: runtime navigation is now a fixed bottom Material 3 navigation bar with four tabs: Home, Ride, History, Profile.
- Home is the first destination and owns the last-trainer status, reconnect/connect-other actions, achievements, and history total link.
- Connect is an internal flow launched from Home, not a bottom navigation item.
- Ride is a lifecycle screen: Planning contains goals and medians only; Active contains live metrics and Race With Shadow; Results contains summary and CSV/TCX export.
- Profile owns app settings first, then diagnostics/compatibility, then about/privacy/permissions; Diagnostics and About are not top-level destinations.
- Keep the cockpit language for both dark and light modes: graphite panels, emerald primary state, cyan telemetry, amber warnings, red destructive/health states, 8dp radius, and 16dp screen gutters.
- Keep BLE, exporters, package IDs, permission model, and manual-only resistance behavior unchanged; do not add remote resistance writes for v1.
- Room schema is v3: sessions store nullable goal/baseline snapshot fields, and `remembered_devices` stores successful BLE connections for the Connect screen.
- `GhostRaceState` uses linear median pace for the MVP: `medianMetric / medianDuration * elapsedSeconds`, capped by median/target. The sampled true-ghost curve is phase 2.
- Convert current light tokens into a dark token set before changing screen composition:
  - background: near-black carbon;
  - surface: layered graphite panels;
  - primary: emerald;
  - secondary telemetry: cyan;
  - warning: amber;
  - danger: red;
  - borders: low-contrast graphite.
- Extract repeated UI pieces before applying the design broadly:
  - status indicator;
  - race progress line;
  - cockpit metric tile;
  - section panel;
  - primary/secondary/destructive action row;
  - protected finish confirmation;
  - empty state panel;
  - compatibility row.
- Keep the core screen set aligned with current IA:
  - Home dashboard;
  - Connect flow;
  - Ride planning / active / results;
  - History grouped by week and detail;
  - Profile settings / diagnostics / about.
- Use Material 3 semantics and Android touch sizing; do not add new product claims or features.

## Acceptance Checks

- At `360x800`, labels do not overlap, the bottom nav stays usable, and primary actions remain at least 44dp high.
- Live Ride makes Race With Shadow the first visual read: current progress, moving median ghost, delta, and remaining-to-goal are readable at a glance.
- Only three primary live metrics are prominent: active distance/calories goal, HR safety, and power/cadence pace.
- HR alert uses red and visually de-emphasizes race progress; red is not used for ordinary behind-median states.
- Behind/ahead color changes use hysteresis and do not flicker on the boundary.
- Manual load panel sits above workout controls, is disabled for YS-003 remote changes, and explains why.
- Finish requires a second confirmation before saving.
- Connect handles `disconnected`, `scanning`, `connected`, and `empty` states.
- History detail preserves CSV and TCX export actions.
- Diagnostics remains dense and utilitarian, with compatibility and sanitized sharing visible.
- Build verification after Compose implementation:

```powershell
.\tools\build-debug-apk.ps1 -SkipSdkInstall
```
