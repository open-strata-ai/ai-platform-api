# ADR-0003: Schema-per-tenant vs RLS

- **Status**: Accepted — see R-002 in `openstrata-meta/contracts/adr-resolutions.md`
- **Date**: 2026-07-17
- **Suggested by**: OpenStrata Architecture Group
- **Repository**: ai-platform-api
- **Source**: `docs/DESIGN.md` §16 Open Issue
- **Association**: (within this repository)

##Context

Does the single-tenant default (starter) also build an independent schema? Prefer RLS unification to reduce operation and maintenance complexity.

## Decision Options (Options Considered)

1. **Maintain status quo / conservative default**: Maintain current behavior, controlled by configuration switches or explicit parameters, and do not introduce destructive changes.
2. **Unified implementation after cross-repository alignment**: Agree on a clear contract with the relevant service (`corresponding governance service`) before implementation.
3. **Phased introduction**: Leave a placeholder/default switch in the current stage, and solidify it in subsequent stages after the dependent capabilities are ready (see Related Architecture §).

## Recommended decision (Decision)

This ADR solidifies "Schema-per-tenant vs RLS" into an architectural decision record and incorporates it into `docs/adr/` for continuous tracking. This issue stems from the `docs/DESIGN.md` §16 open issue and is still open.

**Conservative Default Principle**: Before the final decision is made, the "minimum available + explicit configuration switch" shall prevail, maintain the current behavior, and not destroy the existing contract and cross-repository SPI interface; this ADR status will be written back after review by the relevant team.



## To be aligned / Follow-ups (Follow-ups)

- **Resolution (R-002)**: Accepted — RLS-primary isolation; schema-per-tenant is opt-in Enterprise only. The starter (single-tenant) deployment uses RLS on the shared schema and does NOT build an independent schema. See `openstrata-meta/contracts/adr-resolutions.md`.

## Traceback

- Upstream design: `docs/DESIGN.md` §16 Open issue
- Relevance index: see `docs/adr/README.md`
