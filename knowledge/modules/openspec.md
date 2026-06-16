---
title: "openspec"
generated_by: "start-new-project"
refined_by: "codex"
generated_at: "2026-06-12T00:00:00.000Z"
surface: "repo-private"
---

# openspec

## Purpose

Current-state behavior specifications for Owl Bike V1 Tracker. These specs document existing v1 behavior and release gates; they are not a future roadmap.

## Boundary

- Surface: repo-private
- Public sharing: summarize behavior from `README.md`, `docs/privacy-policy.md`, and reviewed Play/listing docs unless OpenSpec text has been explicitly reviewed.

## Representative Files

- `openspec/project.md`
- `openspec/AGENTS.md`
- `openspec/specs/ble-connection-diagnostics/spec.md`
- `openspec/specs/workout-recording-history/spec.md`
- `openspec/specs/workout-export/spec.md`
- `openspec/specs/local-privacy/spec.md`
- `openspec/specs/release-controlled-beta/spec.md`
- `openspec/specs/race-goal-baseline/spec.md`

## Terms

- current-state spec
- requirement
- scenario
- BLE connection diagnostics
- workout recording/history
- CSV/TCX export
- local privacy
- controlled beta gate
- race with shadow

## Follow-Up

- Run `openspec validate --specs --strict --no-interactive` after spec edits.
- Use `openspec/changes/` only for proposed behavior changes.
- Do not add automatic resistance control, cloud sync, accounts, Health Connect, Strava, payments, or subscriptions as v1 behavior.
