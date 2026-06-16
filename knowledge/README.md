---
title: "Project Knowledge"
generated_by: "start-new-project"
refined_by: "codex"
generated_at: "2026-06-11T19:23:20.447Z"
---

# yesoul-suite Knowledge

Open this directory as a repo-local Obsidian vault. It is a working context layer for agents and humans, not a replacement for source files.

## Entry Points

- [[modules/root|Root project map]]
- [[modules/app|Android app module]]
- [[modules/tools|Build tools]]
- [[modules/docs|Project docs and specs]]
- [[modules/openspec|OpenSpec behavior specs]]
- [[terminology/project-terms|Project terminology]]
- [[docs/documentation-index|Documentation index]]
- [[private/README|Private/local context]]
- [[rag/extractive/index.html|Extractive RAG UI]]

## Rules

- Keep source facts traceable to repo paths through `rag/source-map.json`.
- Keep public-safe material in `docs/public-context.md`.
- Keep private assumptions and non-public operational notes in `docs/private-context.md` or `knowledge/private/`.
- Do not paste secrets, tokens, passwords, private keys, signing keys, keystore passwords, or live credentials into this vault.
- Do not index ignored local caches such as `.android-sdk/`, `.build-tools/`, `.gradle/`, build outputs, APKs, AABs, keystores, or `output/playwright/` review screenshots.
- Re-run `start-new-project --mode write --approve-write` after major project structure changes, then review generated output for ignored-cache pollution before accepting it.

## Current Context Health

- Understand Anything: missing (reduced)
- Modules mapped: 5 active project modules plus local cache boundary notes
- Documentation sources: README, LICENSE, project docs, Play/compliance docs, design handoff/prototype, and OpenSpec
- Secret-risk markers: 0
- Deploy relevance: Android debug APK/manual install plus release AAB helper for controlled Google Play beta
