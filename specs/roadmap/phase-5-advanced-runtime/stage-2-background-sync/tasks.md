# Tasks (Expanded Implementation Spec)

> Source stage: `stage-2-background-sync`

### Scheduled sync

**What it is**
This implements `Scheduled sync` in `stage-2-background-sync` as a shippable vertical slice, so contributors can deliver one deterministic behavior before moving to the next part.

**Exact file(s) to create or modify**
- `runtime-android/app/src/main/kotlin/io/booengine/core/BooShell.kt`

**Language & dependencies**
- Kotlin + Android SDK for runtime/plugin behavior (WebView, Activity, NotificationManager, Storage Access Framework where relevant).
- TypeScript + Node.js for SDK/CLI contract surfaces.
- Phase 1 uses Android System WebView only.

**Interfaces & contracts**
```kotlin
@JavascriptInterface
fun call(requestJson: String): String
```
```ts
export interface BooBridge {
  call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes>
}
```

**Implementation steps**
1. Define or update the interface signature first so JS and Kotlin remain in contract sync.
2. Implement config-gated permission checks from `boo.config.json` before any side-effect call.
3. Add the Android/TypeScript logic for this part and route through `boo.[namespace].[method]()` bridge surface.
4. Normalize success and failure into structured bridge responses with stable error codes.
5. Add logs (request id + namespace + method + duration + outcome).
6. Write tests for happy path, validation error, and permission denial.

**Inputs**
- `boo.config.json` settings for permissions/plugins/theme/start URL.
- Bridge payload from web app (`namespace`, `method`, `payload`).
- Runtime state (activity lifecycle + plugin registry).

**Outputs**
- Success response: `{ ok: true, data }`.
- Failure response: `{ ok: false, error: { code, message, details } }`.
- Side-effect where relevant (file write, notification, widget update, package artifact).

**Error handling**
- Invalid payload => `VALIDATION_ERROR` and no side-effect.
- Permission missing/denied => `PERMISSION_DENIED`.
- Namespace or method unknown => `METHOD_NOT_FOUND`.
- Runtime not ready => `RUNTIME_NOT_READY`.
- Unexpected exception => `INTERNAL_ERROR` with sanitized details only.

**Edge cases**
- App is backgrounded during request: cancel or defer deterministically; never half-apply writes.
- Duplicate request id is retried: operation is idempotent or rejected as `DUPLICATE_REQUEST`.
- Payload exceeds max size: reject with `PAYLOAD_TOO_LARGE`.
- Plugin disabled in config while method exists in runtime: deny with `PERMISSION_DENIED`.

**Security notes**
- Enforce explicit allowlist of namespace/method per plugin; no dynamic reflection dispatch.
- Sanitize all file paths/URIs and reject traversal (`..`) and unsupported URI schemes.
- Never leak stack traces or tokens to JS responses/logs.
- Deny by default when config permissions and runtime permissions disagree.

**Tests**
1. `Scheduled sync success` — setup valid config + payload; invoke method; expect `ok=true` and expected side-effect.
2. `Scheduled sync validation failure` — send malformed payload; expect `VALIDATION_ERROR` and no side-effect.
3. `Scheduled sync permission denied` — remove permission or deny OS prompt; expect `PERMISSION_DENIED`.

**Acceptance criteria**
- [ ] Part compiles in affected modules.
- [ ] Behavior reachable via bridge call (`boo.[namespace].[method]()`).
- [ ] Validation and permission gates run before side-effects.
- [ ] Structured errors returned for all documented failures.
- [ ] Tests for success/validation/permission are documented and pass.

**Related specs**
- `specs/architecture/system-overview.md`
- `specs/bridge/bridge-protocol.md`
- `specs/security/permission-model.md`
- `specs/security/validation-rules.md`

---

### Retry handling

**What it is**
This implements `Retry handling` in `stage-2-background-sync` as a shippable vertical slice, so contributors can deliver one deterministic behavior before moving to the next part.

**Exact file(s) to create or modify**
- `runtime-android/app/src/main/kotlin/io/booengine/core/BooShell.kt`

**Language & dependencies**
- Kotlin + Android SDK for runtime/plugin behavior (WebView, Activity, NotificationManager, Storage Access Framework where relevant).
- TypeScript + Node.js for SDK/CLI contract surfaces.
- Phase 1 uses Android System WebView only.

**Interfaces & contracts**
```kotlin
@JavascriptInterface
fun call(requestJson: String): String
```
```ts
export interface BooBridge {
  call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes>
}
```

**Implementation steps**
1. Define or update the interface signature first so JS and Kotlin remain in contract sync.
2. Implement config-gated permission checks from `boo.config.json` before any side-effect call.
3. Add the Android/TypeScript logic for this part and route through `boo.[namespace].[method]()` bridge surface.
4. Normalize success and failure into structured bridge responses with stable error codes.
5. Add logs (request id + namespace + method + duration + outcome).
6. Write tests for happy path, validation error, and permission denial.

**Inputs**
- `boo.config.json` settings for permissions/plugins/theme/start URL.
- Bridge payload from web app (`namespace`, `method`, `payload`).
- Runtime state (activity lifecycle + plugin registry).

**Outputs**
- Success response: `{ ok: true, data }`.
- Failure response: `{ ok: false, error: { code, message, details } }`.
- Side-effect where relevant (file write, notification, widget update, package artifact).

**Error handling**
- Invalid payload => `VALIDATION_ERROR` and no side-effect.
- Permission missing/denied => `PERMISSION_DENIED`.
- Namespace or method unknown => `METHOD_NOT_FOUND`.
- Runtime not ready => `RUNTIME_NOT_READY`.
- Unexpected exception => `INTERNAL_ERROR` with sanitized details only.

**Edge cases**
- App is backgrounded during request: cancel or defer deterministically; never half-apply writes.
- Duplicate request id is retried: operation is idempotent or rejected as `DUPLICATE_REQUEST`.
- Payload exceeds max size: reject with `PAYLOAD_TOO_LARGE`.
- Plugin disabled in config while method exists in runtime: deny with `PERMISSION_DENIED`.

**Security notes**
- Enforce explicit allowlist of namespace/method per plugin; no dynamic reflection dispatch.
- Sanitize all file paths/URIs and reject traversal (`..`) and unsupported URI schemes.
- Never leak stack traces or tokens to JS responses/logs.
- Deny by default when config permissions and runtime permissions disagree.

**Tests**
1. `Retry handling success` — setup valid config + payload; invoke method; expect `ok=true` and expected side-effect.
2. `Retry handling validation failure` — send malformed payload; expect `VALIDATION_ERROR` and no side-effect.
3. `Retry handling permission denied` — remove permission or deny OS prompt; expect `PERMISSION_DENIED`.

**Acceptance criteria**
- [ ] Part compiles in affected modules.
- [ ] Behavior reachable via bridge call (`boo.[namespace].[method]()`).
- [ ] Validation and permission gates run before side-effects.
- [ ] Structured errors returned for all documented failures.
- [ ] Tests for success/validation/permission are documented and pass.

**Related specs**
- `specs/architecture/system-overview.md`
- `specs/bridge/bridge-protocol.md`
- `specs/security/permission-model.md`
- `specs/security/validation-rules.md`

---

### Offline queue

**What it is**
This implements `Offline queue` in `stage-2-background-sync` as a shippable vertical slice, so contributors can deliver one deterministic behavior before moving to the next part.

**Exact file(s) to create or modify**
- `runtime-android/app/src/main/kotlin/io/booengine/core/BooShell.kt`

**Language & dependencies**
- Kotlin + Android SDK for runtime/plugin behavior (WebView, Activity, NotificationManager, Storage Access Framework where relevant).
- TypeScript + Node.js for SDK/CLI contract surfaces.
- Phase 1 uses Android System WebView only.

**Interfaces & contracts**
```kotlin
@JavascriptInterface
fun call(requestJson: String): String
```
```ts
export interface BooBridge {
  call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes>
}
```

**Implementation steps**
1. Define or update the interface signature first so JS and Kotlin remain in contract sync.
2. Implement config-gated permission checks from `boo.config.json` before any side-effect call.
3. Add the Android/TypeScript logic for this part and route through `boo.[namespace].[method]()` bridge surface.
4. Normalize success and failure into structured bridge responses with stable error codes.
5. Add logs (request id + namespace + method + duration + outcome).
6. Write tests for happy path, validation error, and permission denial.

**Inputs**
- `boo.config.json` settings for permissions/plugins/theme/start URL.
- Bridge payload from web app (`namespace`, `method`, `payload`).
- Runtime state (activity lifecycle + plugin registry).

**Outputs**
- Success response: `{ ok: true, data }`.
- Failure response: `{ ok: false, error: { code, message, details } }`.
- Side-effect where relevant (file write, notification, widget update, package artifact).

**Error handling**
- Invalid payload => `VALIDATION_ERROR` and no side-effect.
- Permission missing/denied => `PERMISSION_DENIED`.
- Namespace or method unknown => `METHOD_NOT_FOUND`.
- Runtime not ready => `RUNTIME_NOT_READY`.
- Unexpected exception => `INTERNAL_ERROR` with sanitized details only.

**Edge cases**
- App is backgrounded during request: cancel or defer deterministically; never half-apply writes.
- Duplicate request id is retried: operation is idempotent or rejected as `DUPLICATE_REQUEST`.
- Payload exceeds max size: reject with `PAYLOAD_TOO_LARGE`.
- Plugin disabled in config while method exists in runtime: deny with `PERMISSION_DENIED`.

**Security notes**
- Enforce explicit allowlist of namespace/method per plugin; no dynamic reflection dispatch.
- Sanitize all file paths/URIs and reject traversal (`..`) and unsupported URI schemes.
- Never leak stack traces or tokens to JS responses/logs.
- Deny by default when config permissions and runtime permissions disagree.

**Tests**
1. `Offline queue success` — setup valid config + payload; invoke method; expect `ok=true` and expected side-effect.
2. `Offline queue validation failure` — send malformed payload; expect `VALIDATION_ERROR` and no side-effect.
3. `Offline queue permission denied` — remove permission or deny OS prompt; expect `PERMISSION_DENIED`.

**Acceptance criteria**
- [ ] Part compiles in affected modules.
- [ ] Behavior reachable via bridge call (`boo.[namespace].[method]()`).
- [ ] Validation and permission gates run before side-effects.
- [ ] Structured errors returned for all documented failures.
- [ ] Tests for success/validation/permission are documented and pass.

**Related specs**
- `specs/architecture/system-overview.md`
- `specs/bridge/bridge-protocol.md`
- `specs/security/permission-model.md`
- `specs/security/validation-rules.md`

---

### Sync tracking

**What it is**
This implements `Sync tracking` in `stage-2-background-sync` as a shippable vertical slice, so contributors can deliver one deterministic behavior before moving to the next part.

**Exact file(s) to create or modify**
- `runtime-android/app/src/main/kotlin/io/booengine/core/BooShell.kt`

**Language & dependencies**
- Kotlin + Android SDK for runtime/plugin behavior (WebView, Activity, NotificationManager, Storage Access Framework where relevant).
- TypeScript + Node.js for SDK/CLI contract surfaces.
- Phase 1 uses Android System WebView only.

**Interfaces & contracts**
```kotlin
@JavascriptInterface
fun call(requestJson: String): String
```
```ts
export interface BooBridge {
  call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes>
}
```

**Implementation steps**
1. Define or update the interface signature first so JS and Kotlin remain in contract sync.
2. Implement config-gated permission checks from `boo.config.json` before any side-effect call.
3. Add the Android/TypeScript logic for this part and route through `boo.[namespace].[method]()` bridge surface.
4. Normalize success and failure into structured bridge responses with stable error codes.
5. Add logs (request id + namespace + method + duration + outcome).
6. Write tests for happy path, validation error, and permission denial.

**Inputs**
- `boo.config.json` settings for permissions/plugins/theme/start URL.
- Bridge payload from web app (`namespace`, `method`, `payload`).
- Runtime state (activity lifecycle + plugin registry).

**Outputs**
- Success response: `{ ok: true, data }`.
- Failure response: `{ ok: false, error: { code, message, details } }`.
- Side-effect where relevant (file write, notification, widget update, package artifact).

**Error handling**
- Invalid payload => `VALIDATION_ERROR` and no side-effect.
- Permission missing/denied => `PERMISSION_DENIED`.
- Namespace or method unknown => `METHOD_NOT_FOUND`.
- Runtime not ready => `RUNTIME_NOT_READY`.
- Unexpected exception => `INTERNAL_ERROR` with sanitized details only.

**Edge cases**
- App is backgrounded during request: cancel or defer deterministically; never half-apply writes.
- Duplicate request id is retried: operation is idempotent or rejected as `DUPLICATE_REQUEST`.
- Payload exceeds max size: reject with `PAYLOAD_TOO_LARGE`.
- Plugin disabled in config while method exists in runtime: deny with `PERMISSION_DENIED`.

**Security notes**
- Enforce explicit allowlist of namespace/method per plugin; no dynamic reflection dispatch.
- Sanitize all file paths/URIs and reject traversal (`..`) and unsupported URI schemes.
- Never leak stack traces or tokens to JS responses/logs.
- Deny by default when config permissions and runtime permissions disagree.

**Tests**
1. `Sync tracking success` — setup valid config + payload; invoke method; expect `ok=true` and expected side-effect.
2. `Sync tracking validation failure` — send malformed payload; expect `VALIDATION_ERROR` and no side-effect.
3. `Sync tracking permission denied` — remove permission or deny OS prompt; expect `PERMISSION_DENIED`.

**Acceptance criteria**
- [ ] Part compiles in affected modules.
- [ ] Behavior reachable via bridge call (`boo.[namespace].[method]()`).
- [ ] Validation and permission gates run before side-effects.
- [ ] Structured errors returned for all documented failures.
- [ ] Tests for success/validation/permission are documented and pass.

**Related specs**
- `specs/architecture/system-overview.md`
- `specs/bridge/bridge-protocol.md`
- `specs/security/permission-model.md`
- `specs/security/validation-rules.md`

---

### Conflict handling basics

**What it is**
This implements `Conflict handling basics` in `stage-2-background-sync` as a shippable vertical slice, so contributors can deliver one deterministic behavior before moving to the next part.

**Exact file(s) to create or modify**
- `runtime-android/app/src/main/kotlin/io/booengine/core/BooShell.kt`

**Language & dependencies**
- Kotlin + Android SDK for runtime/plugin behavior (WebView, Activity, NotificationManager, Storage Access Framework where relevant).
- TypeScript + Node.js for SDK/CLI contract surfaces.
- Phase 1 uses Android System WebView only.

**Interfaces & contracts**
```kotlin
@JavascriptInterface
fun call(requestJson: String): String
```
```ts
export interface BooBridge {
  call<TReq, TRes>(namespace: string, method: string, payload: TReq): Promise<TRes>
}
```

**Implementation steps**
1. Define or update the interface signature first so JS and Kotlin remain in contract sync.
2. Implement config-gated permission checks from `boo.config.json` before any side-effect call.
3. Add the Android/TypeScript logic for this part and route through `boo.[namespace].[method]()` bridge surface.
4. Normalize success and failure into structured bridge responses with stable error codes.
5. Add logs (request id + namespace + method + duration + outcome).
6. Write tests for happy path, validation error, and permission denial.

**Inputs**
- `boo.config.json` settings for permissions/plugins/theme/start URL.
- Bridge payload from web app (`namespace`, `method`, `payload`).
- Runtime state (activity lifecycle + plugin registry).

**Outputs**
- Success response: `{ ok: true, data }`.
- Failure response: `{ ok: false, error: { code, message, details } }`.
- Side-effect where relevant (file write, notification, widget update, package artifact).

**Error handling**
- Invalid payload => `VALIDATION_ERROR` and no side-effect.
- Permission missing/denied => `PERMISSION_DENIED`.
- Namespace or method unknown => `METHOD_NOT_FOUND`.
- Runtime not ready => `RUNTIME_NOT_READY`.
- Unexpected exception => `INTERNAL_ERROR` with sanitized details only.

**Edge cases**
- App is backgrounded during request: cancel or defer deterministically; never half-apply writes.
- Duplicate request id is retried: operation is idempotent or rejected as `DUPLICATE_REQUEST`.
- Payload exceeds max size: reject with `PAYLOAD_TOO_LARGE`.
- Plugin disabled in config while method exists in runtime: deny with `PERMISSION_DENIED`.

**Security notes**
- Enforce explicit allowlist of namespace/method per plugin; no dynamic reflection dispatch.
- Sanitize all file paths/URIs and reject traversal (`..`) and unsupported URI schemes.
- Never leak stack traces or tokens to JS responses/logs.
- Deny by default when config permissions and runtime permissions disagree.

**Tests**
1. `Conflict handling basics success` — setup valid config + payload; invoke method; expect `ok=true` and expected side-effect.
2. `Conflict handling basics validation failure` — send malformed payload; expect `VALIDATION_ERROR` and no side-effect.
3. `Conflict handling basics permission denied` — remove permission or deny OS prompt; expect `PERMISSION_DENIED`.

**Acceptance criteria**
- [ ] Part compiles in affected modules.
- [ ] Behavior reachable via bridge call (`boo.[namespace].[method]()`).
- [ ] Validation and permission gates run before side-effects.
- [ ] Structured errors returned for all documented failures.
- [ ] Tests for success/validation/permission are documented and pass.

**Related specs**
- `specs/architecture/system-overview.md`
- `specs/bridge/bridge-protocol.md`
- `specs/security/permission-model.md`
- `specs/security/validation-rules.md`

---
