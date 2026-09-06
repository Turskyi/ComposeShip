This feature automates building a Flutter Wear OS AAB, generating release notes from git commits, and helping open the Play Console with release notes copied to the clipboard.

Requirements:
- A local Flutter SDK on PATH.
- The ComposeShip desktop app (this project) runs on the same machine and can access the Flutter project folder.
- The feature is intentionally conservative: it builds the AAB and generates release notes, then opens Play Console and copies notes to clipboard so you can paste them into the release editor. Full Play API uploads can be added later (depends on gcloud/fastlane or Google's Java client).

Quick flow:
1. Pick your Flutter project folder (any project with a wear flavor).
2. Click "Build Wear AAB" — the tool runs: flutter build appbundle --flavor wear --release
3. Click "Generate Release Notes" — the tool collects git commits between last tag and HEAD.
4. Click "Open Play Console" — Play Console is opened in the browser and the release notes are copied to clipboard for pasting into the release form.

Notes on future improvements:
- Automated upload via gcloud / Google Play API (requires selecting an approach and adding credentials handling).
- Allow choosing track (internal/beta/production) and automatic uploads.

