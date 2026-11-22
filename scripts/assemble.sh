#!/usr/bin/env bash
# Assembles the standalone Launcher4 Gradle project into $OUT.
#
#   AOSP_SRC   checkout of platform/packages/apps/Launcher3 at android-16.0.0_r3
#   LC_SRC     checkout of LawnchairLauncher/lawnchair (pinned): source of the Gradle wrapper,
#              version catalog, prebuilt Android 16 jars, flag classes and small System UI modules
#   SYSUI_SRC  checkout of LawnchairLauncher/platform_frameworks_libs_systemui (pinned):
#              Google's frameworks/libs/systemui with Gradle files added
#   OUT        output directory (replaced)
set -euo pipefail
: "${AOSP_SRC:?}" "${LC_SRC:?}" "${SYSUI_SRC:?}" "${OUT:?}"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

rm -rf "$OUT"
mkdir -p "$OUT"
cp -a "$AOSP_SRC"/. "$OUT"/
rm -rf "$OUT/.git"

# Gradle wrapper and version catalog.
mkdir -p "$OUT/gradle/wrapper"
cp "$LC_SRC/gradle/libs.versions.toml" "$OUT/gradle/"
cp "$LC_SRC/gradle/wrapper/gradle-wrapper.jar" "$LC_SRC/gradle/wrapper/gradle-wrapper.properties" "$OUT/gradle/wrapper/"
cp "$LC_SRC/gradlew" "$OUT/gradlew"
chmod +x "$OUT/gradlew"

# Prebuilt Android 16 jars.
mkdir -p "$OUT/prebuilts/libs"
for jar in framework-16.jar SystemUI-core-16.jar SystemUI-statsd-16.jar WindowManager-Shell-16.jar; do
  cp "$LC_SRC/prebuilts/libs/$jar" "$OUT/prebuilts/libs/$jar"
done

# Library modules.
cp -a "$SYSUI_SRC" "$OUT/platform_frameworks_libs_systemui"
rm -rf "$OUT/platform_frameworks_libs_systemui/.git"
cp -a "$LC_SRC/flags" "$OUT/flags"
mkdir -p "$OUT/systemUI"
for module in plugin_core common log utils; do
  cp -a "$LC_SRC/systemUI/$module" "$OUT/systemUI/$module"
done

# Small stand-in sources (see scaffold/extra_src).
cp -a "$HERE/scaffold/extra_src" "$OUT/extra_src"

# Launcher4 resource overrides (search bar layouts).
cp -a "$HERE/scaffold/overrides/." "$OUT/"

# Our Gradle files.
cp "$HERE/scaffold/settings.gradle" "$HERE/scaffold/build.gradle" "$HERE/scaffold/gradle.properties" "$OUT/"

# Source-tree fixes (manifest cleanup, app name).
python3 -I "$HERE/scripts/patch_tree.py" "$OUT"

echo "Assembled Launcher4 project in $OUT"
