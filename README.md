[![Stand With Ukraine](https://raw.githubusercontent.com/vshymanskyy/StandWithUkraine/main/banner-direct-single.svg)](https://stand-with-ukraine.pp.ua)
[![Regression Unit Tests](https://github.com/Turskyi/ComposeShip/actions/workflows/regression_tests.yml/badge.svg)](https://github.com/Turskyi/ComposeShip/actions/workflows/regression_tests.yml)
[![Upload Android Build to App Tester](https://github.com/Turskyi/ComposeShip/actions/workflows/android_compose_ci.yml/badge.svg)](https://github.com/Turskyi/ComposeShip/actions/workflows/android_compose_ci.yml)
[![Deploy to Firebase Hosting on merge](https://github.com/Turskyi/ComposeShip/actions/workflows/firebase-hosting-merge.yml/badge.svg)](https://github.com/Turskyi/ComposeShip/actions/workflows/firebase-hosting-merge.yml)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/Turskyi/ComposeShip)
[![wakatime](https://wakatime.com/badge/user/f9df5074-b4ea-4c17-b001-fff428ab82aa/project/138062c7-c898-4532-b554-bf709d077627.svg)](https://wakatime.com/badge/user/f9df5074-b4ea-4c17-b001-fff428ab82aa/project/138062c7-c898-4532-b554-bf709d077627)
<img alt="GitHub commit activity" src="https://img.shields.io/github/commit-activity/m/Turskyi/ComposeShip">

# ComposeShip

This is a Compose Multiplatform project targeting Android, iOS, Web, Desktop (
JVM), Server. It is designed as a suite of developer utility tools.

### Testing the App:

Join our testing program and provide valuable feedback:

- [Android App Tester Invite](https://appdistribution.firebase.dev/i/c32c55af9803d74d)

## Project Structure

* [/app/iosApp](./app/iosApp/iosApp) contains an iOS application. Even if you’re
  sharing your UI with Compose Multiplatform, you need this entry point for your
  iOS app. This is also where you should add SwiftUI code for your project.

* [/app/shared](./app/shared/src) is for code that will be shared across your
  Compose Multiplatform applications. It contains several subfolders:
    - [commonMain](./app/shared/src/commonMain/kotlin) is for code that’s common
      for all targets.
    - Other folders are for Kotlin code that will be compiled for only the
      platform indicated in the folder name.

* [/core](./core/src) is for the code that will be shared between all targets in
  the project. The most important subfolder
  is [commonMain](./core/src/commonMain/kotlin).

* [/server](./server/src/main/kotlin) is for the Ktor server application.

* [/feature-macos-release](./feature-macos-release) is a specialized tool for
  macOS desktop apps. It automates the process of packaging, signing,
  notarizing, and uploading Compose Multiplatform apps to App Store Connect.

* [/feature-cloud-run-deploy](./feature-cloud-run-deploy) is a deployment utility
  for Google Cloud Run. It automates building Docker images via Cloud Build and
  deploying them to Cloud Run.

## macOS Release Tool

The **macOS Release Tool** is a guided, step-by-step utility built to bridge the
gap between Compose Multiplatform build outputs and the Mac App Store.

### Features

- **Project Validation**: Ensures the target project is a valid Compose
  Multiplatform project.
- **Security**: Securely stores App Store Connect API credentials in the macOS
  Keychain.
- **Automated Signing**: Handles deep signing of the app bundle, including JRE
  libraries and subcomponents.
- **Notarization & Upload**: Automates the use of `notarytool` and `altool` for
  submission.

### How to use

1. Run the Desktop application: `./gradlew :app:desktopApp:run`
2. Select the root directory of the CMP project you wish to release.
3. Follow the guided steps to select signing identities and provide App Store
   Connect API credentials.
4. Click **Start Build & Release** to begin the automated flow.

For more technical details,
see [HOW_IT_WORKS.md](./feature-macos-release/HOW_IT_WORKS.md)
and [KNOWN_GAPS.md](./feature-macos-release/KNOWN_GAPS.md) in the feature
module.

## Cloud Run Deployer

The **Cloud Run Deployer** is a simple one-click deployment tool for Ktor
servers or other containerized applications.

### Features

- **Automated Build**: Triggers `gcloud builds submit` to create a Docker image
  remotely.
- **Easy Deployment**: Deploys the image to Cloud Run with pre-configured
  regions and service names.
- **Live Logs**: Streams the build and deployment logs directly into the app
  UI.

### How to use

1. Run the Desktop application: `./gradlew :app:desktopApp:run`
2. Switch to the **Cloud Run** tab in the navigation rail.
3. Select the project root (must contain a `Dockerfile`).
4. Enter your GCP Project ID and service details.
5. Click **Deploy to Cloud Run**.

## Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You
can also use these commands and options:

- **Android app**: `./gradlew :app:androidApp:assembleDebug`
- **Desktop app**:
    - Hot reload: `./gradlew :app:desktopApp:hotRun --auto`
    - Standard run: `./gradlew :app:desktopApp:run`
- **Server**: `./gradlew :server:run`
- **Web app**:
    - Wasm target: `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
    - JS target: `./gradlew :app:webApp:jsBrowserDevelopmentRun`
- **iOS app**: open the [/app/iosApp](./app/iosApp) directory in Xcode and run
  it from there.

## Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- **Android tests**: `./gradlew :app:shared:testAndroidHostTest`
- **Desktop tests**: `./gradlew :app:shared:jvmTest`
- **Server tests**: `./gradlew :server:test`
- **Web tests**:
    - Wasm target: `./gradlew :app:shared:wasmJsTest`
    - JS target: `./gradlew :app:shared:jsTest`
- **iOS tests**: `./gradlew :app:shared:iosSimulatorArm64Test`

---

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…
