package app.launcher4;

import com.android.launcher3.qsb.QsbContainerView;
import com.android.launcher3.qsb.QsbContainerView.QsbWidgetHost;
import com.android.launcher3.qsb.QsbWidgetHostView;

/**
 * Same search widget as the home screen bar, but with its own widget id and widget host so the
 * two bars do not take each other's updates.
 */
public class AllAppsQsbFragment extends QsbContainerView.QsbFragment {

    private static final int ALL_APPS_QSB_HOST_ID = QSB_WIDGET_HOST_ID + 1;

    public AllAppsQsbFragment() {
        mKeyWidgetId = "qsb_widget_id_all_apps";
    }

    @Override
    protected QsbWidgetHost createHost() {
        return new QsbWidgetHost(getContext(), ALL_APPS_QSB_HOST_ID, QsbWidgetHostView::new);
    }
}
