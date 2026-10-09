# TADa Manager — Setup Guide

TADa Manager is a rebranded fork of [Morphe](https://github.com/MorpheApp/morphe-manager)
(GPLv3). Branding, package name, icons, colors, and user-facing strings were changed;
upstream copyright headers, LICENSE, and NOTICE were kept intact.

## What was rebranded

- App name: **TADa Manager** (`TADa Debug` for debug builds)
- Package: `app.morphe.manager` → **`app.tada.manager`**
- Brand colors: purple `#7C3AED` → amber `#F59E0B` (was blue → teal)
- Launcher icon + notification icons: original "T" artwork (the old logo files were
  marked as non-open-source Morphe branding and were **not** reused)
- Theme `Theme.MorpheManager` → `Theme.TADaManager`
- All UI strings (EN + every translated language): Morphe → TADa
- README / docs / CHANGELOG / issue templates / contributing guide
- Release artifacts renamed: `tada-manager-<version>.apk`
- Release workflow: removed Morphe website deploy + FCM push steps (no website/Firebase
  project for this fork yet)

## Before first build: replace placeholders

Search the repo for `nobianh78` (11 files) and replace with your GitHub
username. Files: `README.md`, `CONTRIBUTING.md`, issue/PR templates,
`app-release.json`, `app/app-release.json`, `Constants.kt`, `CreditsDialog.kt`.

## Publishing

1. Create a **new public repo** on GitHub named `tada-manager` (do NOT fork via the
   GitHub fork button if you want a clean issue tracker; either way works).
2. Push this folder's contents:
   ```
   cd tada-manager
   git init && git add -A && git commit -m "Initial TADa Manager release (fork of Morphe)"
   git branch -M main
   git remote add origin https://github.com/<you>/tada-manager.git
   git push -u origin main
   ```
3. The `Release` workflow builds the APK on every push to `main` (semantic-release).
   For a signed release build it needs these repo secrets (same as upstream):
   `KEYSTORE_B64`, `KEYSTORE_PASSWORD`, `KEYSTORE_ENTRY_ALIAS`,
   `KEYSTORE_ENTRY_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`, `GPG_FINGERPRINT`.
   Without them, use the `build_pull_request` workflow output (debug APK) to test.

## Notes

- Patch data still comes from Morphe's infrastructure (`api.morphe.software`,
  `morphe-patches.software`, `github.com/MorpheApp/morphe-patches`) — that is what makes
  the app actually work on day one. The in-app update check now points at YOUR repo.
- Push notifications (FCM) are disabled in this fork: the client code is still there,
  but there is no Firebase project or send script wired up. Set up your own Firebase
  project later if you want them.
- `google-services.json` has the new package name; FCM will not deliver until you add
  your own Firebase project config.
- Deep links via `morphe.software/add-source` were removed (no website for this fork);
  patch sources can still be added manually by URL inside the app.
- Screenshots in `docs/` still show the old UI; replace them when you have new ones.
- GPLv3 obligations: keep LICENSE/NOTICE, keep upstream copyright headers, publish
  this source, and never brand it as "Morphe".
