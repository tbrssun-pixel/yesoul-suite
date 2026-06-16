---
title: "tools"
generated_by: "start-new-project"
refined_by: "codex"
generated_at: "2026-06-11T19:23:20.447Z"
surface: "repo-private, runtime/deploy"
---

# tools

## Purpose

Local build automation for installing project-scoped Android tooling, running tests, producing a debug APK, and validating/building release AAB artifacts.

## Boundary

- Surfaces: repo-private, runtime/deploy
- Public sharing: safe to mention command names and output paths; do not include local machine paths, keystores, or credentials.

## Representative Files

- `tools/build-debug-apk.ps1`
- `tools/build-release-aab.ps1`

## Terms

- debug APK
- release AAB
- SkipSdkInstall
- UnsignedCheckOnly
- local Android SDK
- local Gradle distribution
- unit test gate
- `OWL_BIKE_RELEASE_*` signing environment variables

## Follow-Up

- Keep build outputs ignored.
- Use `-UnsignedCheckOnly` only for local release task validation.
- Require a separate explicit release decision before signed upload or Play publication.
