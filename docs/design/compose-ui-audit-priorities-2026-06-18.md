# Compose UI Audit Priorities

Last reviewed: 2026-06-18
Project: Owl Bike V1 Tracker / yesoul-suite
Primary product type: native Android mobile app
Status: needs implementation

## Scope

This note preserves the main issues from the Compose UI audit for the current information architecture: Home, Ride, History, Profile, with Connect as an internal flow from Home.

Evidence is based on source review and the current Compose implementation. Line links are code references from the audit moment and should be rechecked after edits.

## What Is Good

- The current IA is cleaner than the older prototype/navigation set: Home, Ride, History, Profile is easier to understand than Connect / Live Ride / History / Diag as top-level destinations.
- The app already separates owner-facing settings from diagnostics at the conceptual IA level, even if the Profile screen still needs better visual and interaction structure.
- The core empty and disconnected states are present, which is important for a real APK audit without fake BLE seed data.
- The design direction has useful state-color intent from the handoff: emerald for primary/ready, cyan for telemetry, amber for warning, and red for destructive or health-critical states.

## P1 Issues

### Finish is not visually dangerous enough and is not TalkBack release-ready

`Finish` and `Confirm finish` use green/accent styling even though the action is destructive for an active workout:

- [`MainActivity.kt:2414`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L2414)
- [`MainActivity.kt:2444`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L2444)

The inline confirmation appears without dialog semantics or an explicit focus/live announcement:

- [`MainActivity.kt:2423`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L2423)

Required direction:

- Change finish actions to danger/warning visual treatment.
- Use an `AlertDialog` for confirmation, or add explicit focus management and live announcement for the inline confirmation.
- Ensure TalkBack announces the confirmation state, the destructive nature of the action, and the available cancel/confirm controls.

### Active Ride controls are not sticky

The live ride screen shows context, race, metrics, and resistance before the pause/finish controls:

- [`MainActivity.kt:1780`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L1780)

On 360x800 this is risky during a workout because the user may need to scroll to reach critical actions.

Required direction:

- Add a compact sticky action panel for `Pause` / `Resume` / `Finish`.
- Keep critical ride controls reachable without scrolling in the active ride state.
- Verify at 360x800, 393x873, 412x915 with font scale 1.3.

### Bluetooth permissions and export/share need recovery feedback

The permission launcher currently refreshes state after the request:

- [`MainActivity.kt:367`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L367)

The UI gives a general CTA, but it does not clearly distinguish denied from permanently denied. Export/share flows also need success/error feedback for first beta testers.

Required direction:

- Distinguish permission states: not requested, denied, permanently denied, granted.
- For permanently denied, provide a clear route to Android app settings.
- Add success/error feedback for CSV/TCX export and share actions.
- Keep messages short and localizable in RU/EN.

## P2 Issues

### Custom controls have incomplete accessibility semantics

Settings segmented choices do not expose selected state:

- [`MainActivity.kt:3691`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L3691)

Week expand/collapse controls lack clear role/state semantics:

- [`MainActivity.kt:3332`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L3332)

The goal sheet handle is declared as a button, but closes only via drag gesture:

- [`MainActivity.kt:2804`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L2804)

Required direction:

- Add selected-state semantics to segmented controls.
- Add role and expanded/collapsed state to week headers.
- Either make the goal sheet handle perform a real button action or remove button semantics and provide an accessible close action elsewhere.

### Profile is overloaded with diagnostics

Settings, diagnostics, compatibility, raw BLE log, and about/privacy are currently one long scroll:

- [`MainActivity.kt:3625`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L3625)

This is useful for beta diagnostics, but noisy for a normal bike owner.

Required direction:

- Keep settings at the top.
- Move raw BLE log into a detail screen/sheet or constrain its height.
- Consider folding diagnostics and compatibility behind explicit sections, especially in non-debug-facing flows.

### Too much one-line truncation

Summary stats, week headers, chips, and goal sheet summary often rely on `maxLines = 1` plus ellipsis:

- [`MainActivity.kt:4223`](../../app/src/main/java/com/owlbike/v1tracker/MainActivity.kt#L4223)

This is risky for Russian localization, imperial units, and font scale 1.3-1.5.

Required direction:

- Allow important values and labels to wrap where the layout can support it.
- Avoid truncating destructive actions, permission explanations, selected options, workout summaries, and export/share results.
- Recheck screenshots in RU/EN, dark/light, at 360x800, 393x873, and 412x915 with font scale 1.3.

### UI architecture is too heavy

`MainActivity.kt` is around 4k lines and contains theme code, reusable components, screens, dialogs, and export/share helpers.

Required direction:

- Extract the UI in small, low-risk steps:
  - `ui/theme`
  - `ui/components`
  - `ui/screens/home`
  - `ui/screens/connect`
  - `ui/screens/ride`
  - `ui/screens/history`
  - `ui/screens/profile`
- Start with component and screen extraction only; keep behavior unchanged until the safety-critical UI issues above are handled.

## Suggested Implementation Order

1. Fix finish confirmation: danger styling, dialog or explicit focus/live announcement, TalkBack verification.
2. Add sticky active-ride controls for Pause/Resume/Finish.
3. Improve Bluetooth permission recovery and export/share success/error feedback.
4. Complete semantics for segmented controls, week expand/collapse, and goal sheet close behavior.
5. Reduce Profile diagnostic noise and constrain or hide raw BLE log behind a detail affordance.
6. Run text wrapping/font-scale pass for RU/EN at font scale 1.3-1.5.
7. Begin gradual UI file extraction after the P1 behavior is stable.

## Verification Targets

- Build: `.\tools\build-debug-apk.ps1 -SkipSdkInstall`
- Real APK package: `com.owlbike.v1tracker.debug`
- Emulator screenshot matrix: 360x800, 393x873, 412x915; RU/EN; dark/light; font scale 1.3.
- TalkBack pass:
  - Home primary action
  - Connect permission/scanning flow
  - Ride planning disabled start
  - Active ride pause/finish controls
  - History empty
  - Profile segmented controls

## Remaining Gaps

- Live Ride states that require BLE/device data still need real-device or seeded-data verification.
- History non-empty states need either prior real sessions or controlled seeded local data.
- The previous emulator screenshot attempt was interrupted before a clean full matrix was completed; do not treat older local PNGs as current IA evidence.
