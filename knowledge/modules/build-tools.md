---
title: ".build-tools"
generated_by: "start-new-project"
refined_by: "codex"
generated_at: "2026-06-11T19:23:20.447Z"
surface: "local-only"
---

# .build-tools

## Purpose

Ignored local Gradle and Android command-line tool cache created by the build helper. This is not a product module.

## Boundary

- Surface: local-only
- Public sharing: do not index downloaded tool contents or include tool distribution internals in public context.

## Representative Files

- `.build-tools/`

## Follow-Up

- Keep this path ignored.
- Regenerate/install through `tools/build-debug-apk.ps1` when needed.
