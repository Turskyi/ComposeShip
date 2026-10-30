# Agent Guidelines

## Project Context & Deployment

- **Live Web App**: The web version of ComposeShip is deployed live at `https://composeship.web.app` (Firebase Hosting). Use this URL when web links, privacy policy URLs, or live app references are required (e.g., App Store Connect, Google Play Console).

## Code Organization & Architecture

- **Composable File Structure**: Prefer declaring `@Composable` functions in
  separate, individual files rather than bundling multiple top-level composables
  together in a single file. Each major step, screen section, or standalone UI
  component should reside in its own dedicated Kotlin file (e.g.,
  `MyComponent.kt`).

- **Codebase Verification**: Always inspect existing source code (e.g., screens, scaffold, navigation, viewmodels) using project search tools before proposing UI button placements, navigation structures, or referring to existing components. Never assume non-existent UI elements (such as copyright footers or version details) exist.

- **Exhaustive Code Inspection**: Never ask the user questions about project configuration, dependencies, tracking, or app capabilities that can be discovered by inspecting project files. Always investigate build files (`build.gradle.kts`), manifests, and source code first before answering or proposing configurations.

### File Maintenance & Guidelines Limit

- The AGENTS.md file must never exceed 200 lines, and this rule must always be
  placed at the very end of the file (and must not be placed below line 200). If
  a new rule needs to be added, concise existing content or remove less critical
  rules to remain strictly under the 200 line limit.