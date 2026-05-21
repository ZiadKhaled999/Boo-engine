# Conflict Resolution Baseline for Roadmap Task Specs

This file records the **canonical baseline** used to resolve recurring merge conflicts in roadmap task specs.

## Scope
The following files are treated as conflict-prone and must prefer the latest canonical content from this branch when rebasing/merging:

- `specs/roadmap/phase-1-core-runtime-proof/stage-1-android-shell/tasks.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-2-web-app-loading/tasks.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-3-basic-bridge/tasks.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-4-config-system/tasks.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-5-packaging-pipeline/tasks.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-6-minimal-sdk/tasks.md`
- `specs/roadmap/phase-1-core-runtime-proof/stage-7-demo-app/tasks.md`
- `specs/roadmap/phase-2-native-capabilities/stage-1-filesystem-plugin/tasks.md`
- `specs/roadmap/phase-2-native-capabilities/stage-2-notifications-plugin/tasks.md`
- `specs/roadmap/phase-2-native-capabilities/stage-3-sharing-and-clipboard/tasks.md`
- `specs/roadmap/phase-2-native-capabilities/stage-4-haptics/tasks.md`
- `specs/roadmap/phase-2-native-capabilities/stage-5-plugin-permissions/tasks.md`
- `specs/roadmap/phase-2-native-capabilities/stage-6-plugin-isolation/tasks.md`
- `specs/roadmap/phase-3-developer-experience/stage-1-cli/tasks.md`
- `specs/roadmap/phase-3-developer-experience/stage-2-fast-iteration/tasks.md`
- `specs/roadmap/phase-3-developer-experience/stage-3-documentation/tasks.md`
- `specs/roadmap/phase-3-developer-experience/stage-4-example-templates/tasks.md`
- `specs/roadmap/phase-3-developer-experience/stage-5-debugging-tools/tasks.md`
- `specs/roadmap/phase-4-plugin-ecosystem/stage-1-plugin-contract/tasks.md`
- `specs/roadmap/phase-4-plugin-ecosystem/stage-2-plugin-packaging/tasks.md`
- `specs/roadmap/phase-4-plugin-ecosystem/stage-3-plugin-author-tools/tasks.md`
- `specs/roadmap/phase-4-plugin-ecosystem/stage-4-community-workflow/tasks.md`
- `specs/roadmap/phase-4-plugin-ecosystem/stage-5-plugin-registry/tasks.md`
- `specs/roadmap/phase-5-advanced-runtime/stage-1-widgets/tasks.md`
- `specs/roadmap/phase-5-advanced-runtime/stage-2-background-sync/tasks.md`

## Merge policy
1. Keep the full per-Part structure in each `tasks.md` section.
2. Keep bridge contract references (`@JavascriptInterface`, `boo.[namespace].[method]()`), structured errors, and security sections.
3. Keep `boo.config.json` as source of truth.
4. Keep line endings normalized (LF) and one trailing newline.

## Verification commands
```bash
git status --short
rg -n "^(<<<<<<<|=======|>>>>>>>)" specs/roadmap
```

If conflict markers are found, resolve and re-run verification before opening a PR.
