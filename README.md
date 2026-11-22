# Launcher4

An Android launcher built from the AOSP Launcher3 source (Android 16, `android-16.0.0_r3`), with:

- a Google search bar on the home screen, **below the dock icons**;
- a Google search bar at the top of the **app drawer**.

Both bars host the search widget of the installed Google app, so the Google "G", microphone and
Lens icons are drawn by the Google app itself. Without the Google app they fall back to a plain
"Search" bar.

## Install

1. Open the latest build on the [Releases page](../../releases) on your phone and download `Launcher4.apk`.
2. Open the file and allow installs from your browser if Android asks.
3. Tap **Open** and choose **Choose default home app**, then pick Launcher4 (or use Settings → Apps → Default apps → Home app).
4. First run: tap the small gear on a search bar and allow Launcher4 to add the widget.

Builds are debug-signed with a fresh key each time, so uninstall the old build before installing a newer one.

## If it closes unexpectedly

Open the **Launcher4** app from your app list. It shows the last crash report with Copy and Share buttons.

## How it is built

GitHub Actions (`.github/workflows/build.yml`) downloads the Launcher3 source, assembles a Gradle
project around it (`scripts/assemble.sh`), applies the Launcher4 changes (`scripts/patch_tree.py`,
`scaffold/`), builds the APK and publishes it as a release.
