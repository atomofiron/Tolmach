package io.atomofiron.tolmach.custom;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ConfigurationHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import io.atomofiron.tolmach.R;

import static android.support.design.widget.FloatingActionButton.SIZE_AUTO;
import static android.support.design.widget.FloatingActionButton.SIZE_MINI;
import static android.support.design.widget.FloatingActionButton.SIZE_NORMAL;

/* костыль... или инновационное решение*/
public class VoicePowerIndicator extends View implements Animation.AnimationListener, ValueAnimator.AnimatorUpdateListener {
	private Paint paint;
	private FloatingActionButton fab;
	private ValueAnimator animator = ValueAnimator.ofFloat();

	private int realFabSize;
	private int fabRadius;
	private float scale = 0f;

	private float cx;
	private float cy;
	private float maxRadius;

	{
		animator.setDuration(200);
		animator.addUpdateListener(this);

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
	}

	public VoicePowerIndicator(Context context) {
		super(context);
	}

	public VoicePowerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		parseAttributes(context, attrs);
	}

	public VoicePowerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		parseAttributes(context, attrs);
	}

	private void parseAttributes(Context context, AttributeSet attrs) {
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VoicePowerIndicator, 0, 0);
		try {
			paint.setColor(array.getColor(R.styleable.VoicePowerIndicator_color, paint.getColor()));
		} finally {
			array.recycle();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawCircle(canvas.getWidth() + cx, canvas.getHeight() + cy, fabRadius + maxRadius * scale, paint);
	}

	public void resetScale() {
		startAnim(0);
	}

	public void setScale(float scale) {
		calculate();
		startAnim(scale);
	}

	private void calculate() {
		fabRadius = realFabSize / 2;
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();

		// работает пока только, если FAB в такой конфигурации
		cx = -fabRadius - mlp.rightMargin - (fab.getWidth() - realFabSize) / 2;
		cy = -fabRadius - mlp.bottomMargin - (fab.getHeight() - realFabSize) / 2;
		maxRadius = fabRadius * 5;
	}

	/** @see android.support.design.widget.FloatingActionButton#getSizeDimension(int) */
	private int getSizeDimension(@FloatingActionButton.Size final int size) {
		final Resources res = getResources();
		switch (size) {
			case SIZE_AUTO:
				// If we're set to auto, grab the size from resources and refresh
				final int width = ConfigurationHelper.getScreenWidthDp(res);
				final int height = ConfigurationHelper.getScreenHeightDp(res);
				return Math.max(width, height) < 470
						? getSizeDimension(SIZE_MINI)
						: getSizeDimension(SIZE_NORMAL);
			case SIZE_MINI:
				return res.getDimensionPixelSize(android.support.design.R.dimen.design_fab_size_mini);
			case SIZE_NORMAL:
			default:
				return res.getDimensionPixelSize(android.support.design.R.dimen.design_fab_size_normal);
		}
	}

	private void startAnim(float nextScale) {
		animator.cancel();
		animator.setFloatValues(scale, nextScale);
		animator.start();
	}

	public void setFAB(FloatingActionButton fab) {
		this.fab = fab;
		realFabSize = getSizeDimension(fab.getSize());
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {

	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		scale = (float) animation.getAnimatedValue();

		invalidate();
	}
}
