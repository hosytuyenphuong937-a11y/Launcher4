/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.wm.shell.shared.bubbles;

/**
 * Stand-in for the WM Shell helper that is missing from the prebuilt Shell jar. The "bubble
 * anything" feature is disabled by default in Android 16, so every check returns false.
 */
public class BubbleAnythingFlagHelper {

    private BubbleAnythingFlagHelper() {}

    /** Whether creating any bubble or the overall bubble anything feature is enabled. */
    public static boolean enableCreateAnyBubble() {
        return false;
    }
}
