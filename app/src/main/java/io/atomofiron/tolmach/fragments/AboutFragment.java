package io.atomofiron.tolmach.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.atomofiron.tolmach.R;

public class AboutFragment extends Fragment {

	public AboutFragment() {}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().setTitle(R.string.about);

		View view = inflater.inflate(R.layout.fragment_about, container, false);

		TextView yandex = (TextView) view.findViewById(R.id.link);
		yandex.setText(Html.fromHtml(getString(R.string.use_service)));
		yandex.setMovementMethod(LinkMovementMethod.getInstance());

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
	}
}
