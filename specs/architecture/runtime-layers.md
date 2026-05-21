# Runtime Layers

## Purpose
Describe layered boundaries to prevent core bloat.

## Responsibilities
- Android shell and lifecycle
- Bridge orchestration
- Plugin loading
- Permission enforcement

## Lifecycle / Flow
Android OS -> Boo core runtime -> Plugins -> Web app

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
- Only core owns permission gates
- Plugin code runs through validated contracts

## Future Improvements
- Fine-grained plugin process isolation in later phases

## Related Specs
- `specs/architecture/system-overview.md`
