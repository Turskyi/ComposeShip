This feature automates building a Flutter Wear OS AAB, generating release notes from git commits, and uploading both to Google Play through Fastlane.

Requirements:
- A local Flutter SDK on PATH.
- The ComposeShip desktop app (this project) runs on the same machine and can access the Flutter project folder.
- Release notes from the text field are uploaded as Google Play's “What's new” text. Plain text is sent as `en-US`; optionally use `<locale>...</locale>` blocks (for example, `<en-CA>...</en-CA>`) for localized notes.

Quick flow:
1. Pick your Flutter project folder (any project with a wear flavor).
2. Click "Build Wear AAB" — the tool runs: flutter build appbundle --flavor wear --release
3. Click "Generate Release Notes" — the tool collects git commits between last tag and HEAD.
4. Review or edit the release notes, select a track, and click "Upload to Play". The AAB and release notes are sent together.

Notes on future improvements:
- Support release rollout percentage and draft releases.
- Show the resolved changelog locales before upload.
