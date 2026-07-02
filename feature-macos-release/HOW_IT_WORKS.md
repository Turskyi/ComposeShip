# How It Works: macOS App Store Release Tool

This tool automates the tedious and error-prone process of packaging, signing, notarizing, and uploading a Compose Multiplatform desktop application to the Mac App Store.

## Automation Workflow

1.  **Project Validation**: 
    - Verifies the selected directory is a valid Gradle project.
    - Checks for CMP-specific configurations (Compose desktop plugin).
    
2.  **Environment Preparation**:
    - Cleans the `com.apple.quarantine` extended attribute from all files in the project to prevent Apple's rejection.
    
3.  **Build Execution**:
    - Runs the detected or selected Gradle packaging task (e.g., `packageReleasePkg`) using the local `./gradlew` wrapper.
    - Streams live output to the UI for real-time monitoring.

4.  **Signing Identity Management**:
    - Automatically discovers valid signing identities from the macOS Keychain.
    - Differentiates between "Application" identities (for the `.app` bundle) and "Installer" identities (for the `.pkg`).

5.  **Post-Build Fixes & Alignment**:
    - Uses `PlistBuddy` to ensure `Info.plist` has correct metadata (`LSMinimumSystemVersion`, `ITSAppUsesNonExemptEncryption`, etc.).
    - Manually embeds the provisioning profile into `Contents/embedded.provisionprofile`.

6.  **Deep Signing**:
    - Recursively signs all nested `.dylib`, `.so`, and `.jnilib` files within the app bundle and JVM runtime.
    - Signs the `jspawnhelper` with specific child entitlements.
    - Signs the main executable and the app bundle itself using the provided entitlements.

7.  **Installer Creation**:
    - Wraps the signed `.app` into a `.pkg` using `productbuild`, signed with the "Installer" identity.

8.  **Notarization & Upload**:
    - Uses `xcrun notarytool` (modern) or `altool` (legacy) to submit the package to Apple.
    - Securely retrieves App Store Connect API credentials from the macOS Keychain.

## Key Technical Decisions

- **Process Isolation**: All external tools (`codesign`, `notarytool`, `gradlew`) are executed via `ProcessBuilder` with output redirected to the UI.
- **Security**: App Store Connect API keys and Issuer IDs are stored in the macOS Keychain, never in plaintext.
- **Dependency Inversion**: Platform-specific logic (Keychain, Shell, File System) is hidden behind interfaces in `commonMain`, with concrete implementations in `desktopMain`.
- **UI/UX**: A step-by-step guided flow ensures no steps are missed, and "Show Details" provides transparency for long-running commands.
