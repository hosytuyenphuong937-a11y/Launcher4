package app.launcher4;

import android.util.Log;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

/**
 * AOSP Launcher3 is built to run as a system app and calls many framework methods that Android
 * hides from normal apps. This lifts that restriction for this process; without it those calls
 * fail with NoSuchMethodError and the launcher closes.
 */
public final class HiddenApi {

    private static volatile String sStatus = "not attempted";

    private HiddenApi() { }

    public static void unlock() {
        try {
            boolean ok = HiddenApiBypass.addHiddenApiExemptions("L");
            sStatus = ok ? "ok" : "returned false";
        } catch (Throwable t) {
            sStatus = "failed: " + t;
            Log.w("Launcher4", "Could not unlock hidden APIs", t);
        }
    }

    public static String status() {
        return sStatus;
    }
}
