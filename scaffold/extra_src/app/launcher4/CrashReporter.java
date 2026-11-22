package app.launcher4;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Writes the stack trace of any uncaught exception to a file in the app's private storage, so the
 * Launcher4 entry (see {@link StartActivity}) can show it on the next start.
 */
public final class CrashReporter {

    private static final String FILE_NAME = "last_crash.txt";
    private static final int MAX_CHARS = 30_000;

    private CrashReporter() { }

    public static void install(Context context) {
        final File file = new File(context.getFilesDir(), FILE_NAME);
        final String processName = currentProcessName();
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                write(file, processName, thread, throwable);
            } catch (Throwable ignored) {
                // Nothing sensible can be done while crashing.
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            }
        });
    }

    /** Returns the saved crash report, or null if there is none. */
    public static String read(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.isFile()) {
            return null;
        }
        try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (Throwable t) {
            return null;
        }
    }

    public static void clear(Context context) {
        //noinspection ResultOfMethodCallIgnored
        new File(context.getFilesDir(), FILE_NAME).delete();
    }

    private static void write(File file, String processName, Thread thread, Throwable throwable)
            throws Exception {
        StringWriter trace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(trace));
        String stack = trace.toString();
        if (stack.length() > MAX_CHARS) {
            stack = stack.substring(0, MAX_CHARS) + "\n... (cut)";
        }
        String report = "Launcher4 crash report\n"
                + "Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(new Date()) + "\n"
                + "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n"
                + "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n"
                + "Process: " + processName + "\n"
                + "Thread: " + thread.getName() + "\n"
                + "Hidden API unlock: " + HiddenApi.status() + "\n\n"
                + stack;
        try (FileOutputStream out = new FileOutputStream(file, false)) {
            out.write(report.getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        }
    }

    private static String currentProcessName() {
        try {
            return android.app.Application.getProcessName();
        } catch (Throwable t) {
            return "unknown";
        }
    }
}
