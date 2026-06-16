---
title: "(root)"
generated_by: "start-new-project"
refined_by: "codex"
generated_at: "2026-06-11T19:23:20.447Z"
surface: "public-safe, repo-private, runtime/deploy"
---

# (root)

## Purpose

Root project configuration, public README/license material, Gradle plugin versions, repository ignore rules, OpenSpec current-state specs, and build/release entrypoint documentation.

## Boundary

- Surfaces: public-safe, repo-private, runtime/deploy
- Public sharing: `README.md` and `LICENSE` are public-safe. Treat Gradle config and generated context as repo-private unless reviewed.

## Representative Files

- `.gitattributes`
- `.gitignore`
- `README.md`
- `LICENSE`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `openspec/project.md`
- `openspec/specs/**/spec.md`

## Terms

- Owl Bike V1 Tracker
- Android debug APK
- release AAB
- controlled beta
- Gradle Kotlin DSL
- Android Gradle Plugin
- Jetpack Compose
- local SDK/tool cache

## Follow-Up

- Add an Understand Anything graph and regenerate context.
- Keep release signing and external distribution behind a separate explicit request.
- Keep ignored local SDK/tool caches out of generated knowledge artifacts.
- Keep `output/playwright/` review screenshots local-only unless a specific screenshot is promoted into reviewed design evidence.
