package io.atomofiron.tolmach.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import io.atomofiron.tolmach.R;

public class FloatingRecordButton extends FloatingActionButton {
	public FloatingRecordButton(Context context) {
		super(context);
	}

	public FloatingRecordButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public FloatingRecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FloatingRecordButton, 0, 0);
		try {
			setEnabled(array.getBoolean(R.styleable.FloatingRecordButton_enabled, true));
		} finally {
			array.recycle();
		}
	}
}
