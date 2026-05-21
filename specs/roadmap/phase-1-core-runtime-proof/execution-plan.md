# Phase 1 Execution Plan (Vertical Slice First)

## Purpose
Define the production-oriented delivery path for Phase 1 so Boo Engine reaches a real, installable APK milestone with one end-to-end JS→Bridge→Native call and offline startup.

## Scope
This plan coordinates Phase 1 stages only:
1. Android shell
2. Web app loading
3. Basic bridge
4. Config system
5. Packaging pipeline
6. Minimal SDK
7. Demo app

## Entry / Exit Criteria

### Phase 1 Entry Criteria
- Runtime Android module builds locally.
- A simple web app bundle can be produced.
- A clear bridge contract exists for one plugin API.

### Phase 1 Exit Criteria
- `boo build` produces an installable APK (and optional AAB).
- App cold-starts offline using packaged assets.
- Demo app performs one successful native call via Boo SDK.
- Phase documentation and tasks are up to date.

## Stage-by-Stage Build Order

### Stage 1: Android Shell
**Deliverable:** Stable `Activity` + `WebView` host shell with lifecycle-safe startup.

**Must be true before moving on:**
- App launches to hosting shell on real/emulated Android.
- WebView lifecycle is tied to activity lifecycle.
- Startup and crash logs are visible for diagnostics.

### Stage 2: Web App Loading
**Deliverable:** Deterministic loading of packaged web assets.

**Must be true before moving on:**
- Local asset path routing is stable.
- Boot fallback page is shown on loading failure.
- Offline cold start succeeds without network access.

### Stage 3: Basic Bridge
**Deliverable:** One secure request/response bridge path.

**Must be true before moving on:**
- Bridge validates payload shape.
- Permission checks run before plugin call.
- Structured error objects return to JS.
- Async calls resolve/reject predictably.

### Stage 4: Config System
**Deliverable:** Validated Boo config consumed by CLI and runtime.

**Must be true before moving on:**
- Config schema version is explicit.
- Invalid config fails with actionable diagnostics.
- Runtime-relevant options are serializable into Android assets.

### Stage 5: Packaging Pipeline
**Deliverable:** Repeatable build flow from web bundle to APK.

**Must be true before moving on:**
- Web assets are injected into Android project output.
- Debug APK generation is automated.
- Signing flow supports local development keys.

### Stage 6: Minimal SDK
**Deliverable:** Tiny JS SDK that wraps bridge transport.

**Must be true before moving on:**
- SDK exposes one typed API call.
- SDK contract matches bridge schema.
- SDK errors are typed and debuggable.

### Stage 7: Demo App
**Deliverable:** Reference app proving full vertical slice.

**Must be true to complete Phase 1:**
- Demo runs inside Boo Android shell.
- Demo performs one native call from JS.
- Demo install and test steps are documented and repeatable.

## Architecture Guardrails
- Keep Boo Core minimal: host runtime, lifecycle, bridge routing, permissions, plugin loading, app loading, packaging integration.
- Push feature logic into plugins; no feature creep in core.
- Treat bridge contracts as versioned interfaces, not ad hoc method names.
- Build one golden path before introducing alternatives.

## First Native Call for Validation
For Phase 1, use **one intentionally simple plugin method** (for example a lightweight device capability ping) to validate:
- JS SDK call shape
- Bridge serialization
- Permission gate integration
- Native method execution
- Response and error propagation

Do not expand plugin surface in Phase 1 beyond what is necessary for proof.

## Risks and Mitigations
- **Risk:** Core grows into feature bucket.  
  **Mitigation:** Enforce “core vs plugin” boundary during code review.
- **Risk:** Bridge contract drift between Kotlin and TypeScript.  
  **Mitigation:** Shared schemas + contract tests.
- **Risk:** Packaging instability across environments.  
  **Mitigation:** Single documented build path and deterministic output layout.
- **Risk:** “Works on maintainer machine” demo outcome.  
  **Mitigation:** Scripted install/test checklist and CI smoke build.

## Validation Matrix
- Runtime boot smoke test (Android emulator).
- Offline cold start test (airplane mode).
- Bridge success path test (typed request/response).
- Bridge failure path test (validation + permission denial).
- Packaging smoke test (`boo build` + APK install).

## Related Specs
- `specs/roadmap/phase-1-core-runtime-proof/stage-1-android-shell/README.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-2-web-app-loading/README.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-3-basic-bridge/README.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-4-config-system/README.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-5-packaging-pipeline/README.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-6-minimal-sdk/README.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-7-demo-app/README.md`
