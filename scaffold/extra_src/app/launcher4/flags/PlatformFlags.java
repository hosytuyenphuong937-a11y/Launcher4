package app.launcher4.flags;

/**
 * Stand-ins for Android's own feature-flag classes (android.multiuser.Flags and
 * android.appwidget.flags.Flags). Those classes are not available to normal apps on every device
 * (the first build crashed on a Samsung tablet with a missing com.android.window.flags.Flags), so the
 * launcher reads these constants instead. The values are what shipping Android 16 uses.
 */
public final class PlatformFlags {

    private PlatformFlags() { }

    /** android.multiuser.Flags#enableMovingContentIntoPrivateSpace */
    public static boolean enableMovingContentIntoPrivateSpace() {
        return true;
    }

    /** android.appwidget.flags.Flags#generatedPreviews */
    public static boolean generatedPreviews() {
        return true;
    }
}
