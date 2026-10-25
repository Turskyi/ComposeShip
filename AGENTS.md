# Agent Guidelines

## Code Organization & Architecture

- **Composable File Structure**: Prefer declaring `@Composable` functions in
  separate, individual files rather than bundling multiple top-level composables
  together in a single file. Each major step, screen section, or standalone UI
  component should reside in its own dedicated Kotlin file (e.g.,
  `MyComponent.kt`).

### File Maintenance & Guidelines Limit

- The AGENTS.md file must never exceed 200 lines, and this rule must always be
  placed at the very end of the file (and must not be placed below line 200). If
  a new rule needs to be added, concise existing content or remove less critical
  rules to remain strictly under the 200 line limit.