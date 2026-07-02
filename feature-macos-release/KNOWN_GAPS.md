# Known Gaps in macOS Release Documentation

This file tracks gaps and ambiguities discovered in the reference docs (`AI_MACOS_RELEASE.md`) and how they were resolved.

### 1. Installer Identity Distinction
- **Gap**: The reference doc mentions `IDENTITY` and `IDENTITY_INSTALLER`. `security find-identity -p codesigning` might not clearly distinguish them to a user.
- **Resolution**: The tool should look for both `3rd Party Mac Developer Application` and `3rd Party Mac Developer Installer` prefixes specifically.

### 2. Notarization vs. Upload
- **Gap**: The reference doc uses `altool --upload-app` for the final step. For modern macOS apps, `notarytool` is required for notarization *before* or *during* upload, especially for Developer ID distribution. For Mac App Store, `altool` is still used for submission, but the credentials management is slightly different.
- **Resolution**: Provide clear fields for API Key Path, Key ID, and Issuer ID, which are the modern standard for both `notarytool` and `altool`.

### 3. Entitlements File Path
- **Gap**: The reference doc hardcodes entitlements paths (e.g., `composeApp/src/desktopMain/entitlements/entitlements.plist`). Different CMP projects might use different paths.
- **Resolution**: The tool should attempt to auto-detect these files in `src/desktopMain/entitlements` but allow the user to specify them if not found.

### 4. JVM Hardened Runtime & Sandbox
- **Gap**: The reference doc lists several mandatory entitlements. If a user misses one, the app crashes on launch.
- **Resolution**: The tool should provide a "Verify Entitlements" step that checks for the presence of mandatory JVM flags (`allow-jit`, `allow-unsigned-executable-memory`, etc.) before signing.
