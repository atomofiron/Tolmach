package io.atomofiron.tolmach.custom;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import io.atomofiron.tolmach.R;

/* костыль... или инновационное решение*/
public class VoicePowerIndicator extends View implements Animation.AnimationListener, ValueAnimator.AnimatorUpdateListener {
	private Paint paint;
	private FloatingActionButton fab;
	private ValueAnimator animator = ValueAnimator.ofFloat();

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
		fabRadius = fab.getWidth() / 2;
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();

		// работает пока только, если FAB в такой конфигурации
		cx = -fabRadius / 2 - mlp.rightMargin;
		cy = -fabRadius / 2 - mlp.bottomMargin;
		maxRadius = fabRadius * 4;
	}

	private void startAnim(float nextScale) {
		animator.cancel();
		animator.setFloatValues(scale, nextScale);
		animator.start();
	}

	public void setFAB(FloatingActionButton fab) {
		this.fab = fab;
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
