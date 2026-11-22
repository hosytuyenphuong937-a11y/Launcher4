package app.launcher4;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The "Open" entry for Launcher4: lets the user open the launcher, choose it as the home app and
 * read the last crash report. AOSP Launcher3 has no regular launcher activity, so without this the
 * installer only offers "Done".
 */
public class StartActivity extends Activity {

    private ScrollView mScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScroll = new ScrollView(this);
        mScroll.setOnApplyWindowInsetsListener((v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsets.CONSUMED;
        });
        setContentView(mScroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(24);
        column.setPadding(pad, dp(16), pad, pad);

        TextView title = text("Launcher4", 30, true);
        column.addView(title);

        boolean isDefault = isDefaultHome();
        column.addView(text(isDefault
                ? "Launcher4 is your home app."
                : "Launcher4 is not your home app yet. Choose it below, then press Home.", 16, false));

        column.addView(button("Open Launcher4", v -> openLauncher()), buttonParams());
        column.addView(button("Choose default home app", v -> chooseHome()), buttonParams());

        String crash = CrashReporter.read(this);
        if (crash == null) {
            column.addView(text("\nNo crash recorded.", 15, false));
        } else {
            column.addView(text("\nLast crash", 20, true));
            column.addView(text("If Launcher4 closed on you, copy this report and send it to the "
                    + "person fixing it.", 15, false));
            TextView report = text(crash, 11, false);
            report.setTypeface(Typeface.MONOSPACE);
            report.setTextIsSelectable(true);
            report.setPadding(0, dp(8), 0, dp(8));
            column.addView(report);
            column.addView(button("Copy report", v -> copy(crash)), buttonParams());
            column.addView(button("Share report", v -> share(crash)), buttonParams());
            column.addView(button("Clear report", v -> {
                CrashReporter.clear(this);
                render();
            }), buttonParams());
        }
        column.addView(text("\nHidden API unlock: " + HiddenApi.status(), 12, false));

        mScroll.removeAllViews();
        mScroll.addView(column);
    }

    private boolean isDefaultHome() {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo info = getPackageManager().resolveActivity(home,
                PackageManager.MATCH_DEFAULT_ONLY);
        return info != null && info.activityInfo != null
                && getPackageName().equals(info.activityInfo.packageName);
    }

    private void openLauncher() {
        try {
            startActivity(new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .setClassName(getPackageName(), "com.android.launcher3.Launcher")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Throwable t) {
            Toast.makeText(this, "Could not open: " + t, Toast.LENGTH_LONG).show();
        }
    }

    private void chooseHome() {
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (Throwable t) {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
            } catch (Throwable t2) {
                Toast.makeText(this, "Open Settings > Apps > Default apps > Home app",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void copy(String report) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Launcher4 crash report", report));
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }

    private void share(String report) {
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "Launcher4 crash report")
                .putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(send, "Share crash report"));
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }
        view.setPadding(0, dp(6), 0, dp(6));
        return view;
    }

    private Button button(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        return params;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()));
    }
}
