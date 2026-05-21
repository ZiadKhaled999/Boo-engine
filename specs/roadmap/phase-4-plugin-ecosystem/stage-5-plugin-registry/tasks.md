# Tasks (Expanded Implementation Spec)

> Source stage: `stage-5-plugin-registry`

### Discover plugins

**What it is**
This implements `Discover plugins` in `stage-5-plugin-registry` as a shippable vertical slice, so contributors can deliver one deterministic behavior before moving to the next part.

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
1. `Discover plugins success` — setup valid config + payload; invoke method; expect `ok=true` and expected side-effect.
2. `Discover plugins validation failure` — send malformed payload; expect `VALIDATION_ERROR` and no side-effect.
3. `Discover plugins permission denied` — remove permission or deny OS prompt; expect `PERMISSION_DENIED`.

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

### Install plugins

**What it is**
This implements `Install plugins` in `stage-5-plugin-registry` as a shippable vertical slice, so contributors can deliver one deterministic behavior before moving to the next part.

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
1. `Install plugins success` — setup valid config + payload; invoke method; expect `ok=true` and expected side-effect.
2. `Install plugins validation failure` — send malformed payload; expect `VALIDATION_ERROR` and no side-effect.
3. `Install plugins permission denied` — remove permission or deny OS prompt; expect `PERMISSION_DENIED`.

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

### Enable/disable plugins

**What it is**
This implements `Enable/disable plugins` in `stage-5-plugin-registry` as a shippable vertical slice, so contributors can deliver one deterministic behavior before moving to the next part.

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
1. `Enable/disable plugins success` — setup valid config + payload; invoke method; expect `ok=true` and expected side-effect.
2. `Enable/disable plugins validation failure` — send malformed payload; expect `VALIDATION_ERROR` and no side-effect.
3. `Enable/disable plugins permission denied` — remove permission or deny OS prompt; expect `PERMISSION_DENIED`.

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

### Validate plugin signatures later

**What it is**
This implements `Validate plugin signatures later` in `stage-5-plugin-registry` as a shippable vertical slice, so contributors can deliver one deterministic behavior before moving to the next part.

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
1. `Validate plugin signatures later success` — setup valid config + payload; invoke method; expect `ok=true` and expected side-effect.
2. `Validate plugin signatures later validation failure` — send malformed payload; expect `VALIDATION_ERROR` and no side-effect.
3. `Validate plugin signatures later permission denied` — remove permission or deny OS prompt; expect `PERMISSION_DENIED`.

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
