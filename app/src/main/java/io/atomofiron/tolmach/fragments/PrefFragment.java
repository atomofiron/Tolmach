package io.atomofiron.tolmach.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.Menu;
import android.view.MenuInflater;

import io.atomofiron.tolmach.I;
import io.atomofiron.tolmach.R;

public class PrefFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
	private SharedPreferences sp;

	public PrefFragment() {}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setHasOptionsMenu(true);

		sp = I.sp(getActivity());

		setListeners(getPreferenceScreen());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

	}

	private void setListeners(android.support.v7.preference.PreferenceGroup screen) {
		for (int i = 0; i < screen.getPreferenceCount(); i++) {
			Preference preference = screen.getPreference(i);
			preference.setOnPreferenceChangeListener(this);
			updateSummary(preference, null);
		}
	}

	private void updateSummary(Preference preference, Object value) {
		if (preference instanceof EditTextPreference)
			preference.setSummary(value == null ? sp.getString(preference.getKey(), "") : (String) value );
        else if (preference instanceof ListPreference)
			// preference.entry from entries, but newValue from entryValues
			preference.setSummary(
					value == null ? ((ListPreference) preference).getEntry() :
					((ListPreference) preference).getEntries()[Integer.parseInt((String) value)]);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		updateSummary(preference, newValue);

		if (preference.getKey().equals(I.PREF_RECOGNIZER)) {
			boolean google = newValue.equals(I.PREF_RECOGNIZER_GOOGLE);
			CheckBoxPreference pref = (CheckBoxPreference) findPreference(I.PREF_OFFLINE_RECOGNITION);
			pref.setEnabled(google);
			if (!google)
				pref.setChecked(false);
		}
		return true;
	}
}
