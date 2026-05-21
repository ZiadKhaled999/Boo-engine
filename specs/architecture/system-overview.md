# System Overview

## Purpose
Define Boo Engine as an Android-first runtime for local-first web apps with optional native plugins.

## Responsibilities
- Keep core small and stable
- Separate runtime, bridge, plugins, SDK, and CLI
- Enforce phase-based delivery with vertical slices

## Lifecycle / Flow
1. Web assets load in runtime
2. JS uses SDK to call bridge
3. Bridge routes to plugin
4. Plugin calls Android APIs
5. Result returns as typed response

## Inputs
- `boo.config.json`
- Runtime state
- Plugin declarations (if applicable)

## Outputs
- Deterministic behavior and logs
- Structured success/error responses

## Edge Cases
- Missing configuration
- Permission denied
- Plugin/runtime version mismatch
- Process death or app backgrounding

## Security Notes
- No implicit privileges
- Capabilities exposed only through permissioned plugins
- Default deny on unknown API calls

## Future Improvements
- Add diagrams once core code exists
- Add compatibility matrix per Android API level

## Related Specs
- `specs/architecture/runtime-layers.md`
- `specs/bridge/bridge-protocol.md`
