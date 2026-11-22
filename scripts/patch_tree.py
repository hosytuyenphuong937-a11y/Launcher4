#!/usr/bin/env python3
"""Source-tree fixes needed to build Launcher3 with Gradle instead of Soong.

Usage: patch_tree.py <assembled project dir>
"""
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])


def edit(relative_path, transform):
    path = root / relative_path
    original = path.read_text(encoding="utf-8")
    updated = transform(original)
    if updated == original:
        print(f"warning: {relative_path} was not changed")
    path.write_text(updated, encoding="utf-8")


def fix_manifest(text):
    # Gradle takes the namespace from build.gradle; a package attribute is not allowed.
    text = re.sub(r'(<manifest\b[^>]*?)\s+package="[^"]*"', r"\1", text, count=1)
    # SDK levels come from build.gradle.
    text = re.sub(r"\s*<uses-sdk[^>]*/>", "", text)
    # Never ship a debuggable launcher.
    text = re.sub(r'\s*android:debuggable="true"', "", text)
    return text


for manifest in ("AndroidManifest-common.xml", "AndroidManifest.xml"):
    edit(manifest, fix_manifest)

# App name shown under the launcher icon and in settings.
edit(
    "res/values/strings.xml",
    lambda text: text.replace(
        '<string name="app_name">Launcher3</string>',
        '<string name="app_name">Launcher4</string>',
    ),
)


# ---------------------------------------------------------------------------------------------
# Launcher4 changes
# ---------------------------------------------------------------------------------------------


def replace_once(relative_path, old, new):
    """Replace exactly one occurrence of `old`; fail the build if the source has moved on."""
    path = root / relative_path
    original = path.read_text(encoding="utf-8")
    if original.count(old) != 1:
        sys.exit(f"patch_tree: expected exactly one match in {relative_path} for: {old!r}"
                 f" (found {original.count(old)})")
    path.write_text(original.replace(old, new), encoding="utf-8")


# Entry point for the installer's "Open" button and for crash reports: a regular launcher activity.
replace_once(
    "AndroidManifest.xml",
    "</application>",
    """    <activity
            android:name="app.launcher4.StartActivity"
            android:label="@string/derived_app_name"
            android:theme="@android:style/Theme.DeviceDefault.DayNight"
            android:taskAffinity="app.launcher4.start"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>""",
)

# Crash reporter and hidden-API unlock as early as possible (before content providers start).
replace_once(
    "src/com/android/launcher3/LauncherApplication.java",
    "    public LauncherAppComponent getAppComponent() {",
    """    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(base);
        app.launcher4.CrashReporter.install(this);
        app.launcher4.HiddenApi.unlock();
    }

    public LauncherAppComponent getAppComponent() {""",
)

# The package name is not com.android.launcher3; the first-screen search bar moves below the dock.
replace_once(
    "src_build_config/com/android/launcher3/BuildConfig.java",
    'APPLICATION_ID = "com.android.launcher3"',
    'APPLICATION_ID = "app.launcher4"',
)
replace_once(
    "src_build_config/com/android/launcher3/BuildConfig.java",
    "QSB_ON_FIRST_SCREEN = true",
    "QSB_ON_FIRST_SCREEN = false",
)

# The search widget fragment is no longer gated on the first-screen flag.
replace_once(
    "src/com/android/launcher3/qsb/QsbContainerView.java",
    "return BuildConfig.QSB_ON_FIRST_SCREEN;",
    "return true;",
)

# Room for the search bar below the dock: bar height, gap above it, and less space under it
# (system insets are added automatically).
replace_once("res/values/dimens.xml",
             '<dimen name="qsb_widget_height">0dp</dimen>',
             '<dimen name="qsb_widget_height">56dp</dimen>')
replace_once("res/values/dimens.xml",
             '<dimen name="all_apps_search_bar_field_height">48dp</dimen>',
             '<dimen name="all_apps_search_bar_field_height">56dp</dimen>')
replace_once("res/values/dimens.xml",
             '<dimen name="all_apps_search_bar_content_overlap">24dp</dimen>',
             '<dimen name="all_apps_search_bar_content_overlap">28dp</dimen>')
replace_once("res/values/config.xml",
             '<dimen name="hotseat_qsb_space_default">0</dimen>',
             '<dimen name="hotseat_qsb_space_default">10</dimen>')
replace_once("res/values/config.xml",
             '<dimen name="hotseat_bar_bottom_space_default">48</dimen>',
             '<dimen name="hotseat_bar_bottom_space_default">12</dimen>')
