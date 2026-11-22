package app.launcher4;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;

import static com.android.launcher3.icons.IconNormalizer.ICON_VISIBLE_AREA_FACTOR;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Insettable;
import com.android.launcher3.R;
import com.android.launcher3.allapps.ActivityAllAppsContainerView;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.qsb.QsbContainerView;
import com.android.launcher3.views.ActivityContext;

/**
 * The search bar at the top of the app drawer. It hosts the search widget of the installed Google
 * app, so the bar looks and works like Google's own: the Google "G", microphone and Lens icons
 * are drawn by the Google app itself.
 */
public class AllAppsQsbContainer extends QsbContainerView implements SearchUiManager, Insettable {

    private final ActivityContext mActivityContext;
    private final int mContentOverlap;
    private ActivityAllAppsContainerView<?> mAppsView;

    public AllAppsQsbContainer(Context context) {
        this(context, null);
    }

    public AllAppsQsbContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsQsbContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActivityContext = ActivityContext.lookupContext(context);
        mContentOverlap =
                getResources().getDimensionPixelSize(R.dimen.all_apps_search_bar_content_overlap);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAppsView == null || mAppsView.getActiveRecyclerView() == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Match the width of the app grid, like the stock search box.
        DeviceProfile dp = mActivityContext.getDeviceProfile();
        int requestedWidth = getSize(widthMeasureSpec);
        int rowWidth = requestedWidth - mAppsView.getActiveRecyclerView().getPaddingLeft()
                - mAppsView.getActiveRecyclerView().getPaddingRight();
        int cellWidth = DeviceProfile.calculateCellWidth(rowWidth,
                dp.cellLayoutBorderSpacePx.x, dp.numShownHotseatIcons);
        int iconVisibleSize = Math.round(ICON_VISIBLE_AREA_FACTOR * dp.iconSizePx);
        int iconPadding = cellWidth - iconVisibleSize;
        int width = rowWidth - iconPadding + getPaddingLeft() + getPaddingRight();
        super.onMeasure(makeMeasureSpec(width, EXACTLY), heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // Centre horizontally in the parent.
        View parent = (View) getParent();
        int availableWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        int width = right - left;
        int expectedLeft = parent.getPaddingLeft() + (availableWidth - width) / 2;
        setTranslationX(expectedLeft - left);
        offsetTopAndBottom(mContentOverlap);
    }

    @Override
    public void setInsets(Rect insets) {
        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        params.topMargin = insets.top;
        requestLayout();
    }

    @Override
    public void initializeSearch(ActivityAllAppsContainerView<?> containerView) {
        mAppsView = containerView;
    }

    @Override
    public void resetSearch() { }

    @Override
    public ExtendedEditText getEditText() {
        return null;
    }
}
