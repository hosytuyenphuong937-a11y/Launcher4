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
