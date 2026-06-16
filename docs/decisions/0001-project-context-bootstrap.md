<!-- generated_by: start-new-project -->

# 0001. Project Context Bootstrap

Date: 2026-06-11

## Status

Proposed

## Context

The project needs a repeatable agent-ready context layer with explicit local/public boundaries, module terminology, documentation routing, local RAG, verification, and deploy-readiness notes.

## Decision

Use a repo-local `knowledge/` Obsidian/RAG vault plus generated docs under `docs/`. Keep public-safe context separate from private/local notes. Treat deploy as assessment until separately approved.

## Consequences

- Agents get a stable onboarding path.
- Sensitive material stays out of public context by default.
- Generated context must be refreshed when project structure changes.
- Live deploy remains gated by explicit approval.
