package io.atomofiron.tolmach.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;

import java.util.ArrayList;

import io.atomofiron.tolmach.R;
import io.atomofiron.tolmach.utils.Lang;

public class ButtonList extends android.support.v7.widget.AppCompatButton {
	private OnItemSelectedListener onItemSelectedListener;
	private final ArrayList<Lang> list = new ArrayList<>();
	private Lang current;

	public ButtonList(Context context) {
		super(context);
	}

	public ButtonList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ButtonList(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setAlpha(enabled ? 1f : 0.3f);
	}

	@Override
	public boolean performClick() {
		if (isEnabled())
			showDialog();

		return super.performClick();
	}

	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		onItemSelectedListener = listener;
	}

	public void setList(ArrayList<Lang> l) {
		list.clear();
		if (l != null)
			list.addAll(l);

		current = list.contains(current) ? list.get(list.indexOf(current)) :
				list.size() > 0 ? list.get(0) : null;
		updateText();
	}

	public ArrayList<Lang> getList() {
		return list;
	}

	public Lang getCurrent() {
		return current;
	}

	public void setCurrent(Lang current) {
		this.current = current;
		updateText();

		if (onItemSelectedListener != null)
			onItemSelectedListener.onSelected(getId(), current);
	}

	private void updateText() {
		setText(current == null ? "null" : current.name);
	}

	private void showDialog() {
		new AlertDialog.Builder(getContext())
				.setItems(Lang.getArryString(list), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						current = list.get(which);
						updateText();

						if (onItemSelectedListener != null)
							onItemSelectedListener.onSelected(getId(), list.get(which));

					}
				}).setNegativeButton(R.string.cancel, null)
				.create().show();
	}

	public interface OnItemSelectedListener {
		void onSelected(int buttonId, Lang lang);
	}
}
