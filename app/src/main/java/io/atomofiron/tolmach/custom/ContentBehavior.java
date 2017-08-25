package io.atomofiron.tolmach.custom;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

/* используется в activity_main.xml@FrameLayout */
public class ContentBehavior extends android.support.design.widget.AppBarLayout.ScrollingViewBehavior {

	/* этот конструктор здесь необходим */
	public ContentBehavior(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
		return super.layoutDependsOn(parent, child, dependency) || dependency instanceof Snackbar.SnackbarLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
		if (dependency instanceof Snackbar.SnackbarLayout) {
			child.setTranslationY(Math.min(0, dependency.getTranslationY() - dependency.getHeight()));
			return true;
		} else
			return super.onDependentViewChanged(parent, child, dependency);
	}
}
