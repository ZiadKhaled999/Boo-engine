# Plugin Philosophy

## Purpose
Define small-core, plugin-heavy system design.

## Responsibilities
- Core manages orchestration
- Plugins own feature logic
- Capability by explicit opt-in

## Lifecycle / Flow
Plugin declared -> validated -> loaded -> invoked -> audited

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
- Each plugin has explicit permissions
- Input validation per method

## Future Improvements
- Remote/plugin registry trust and signatures

## Related Specs
- `specs/architecture/system-overview.md`
