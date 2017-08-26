package io.atomofiron.tolmach.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.atomofiron.tolmach.App;
import io.atomofiron.tolmach.BuildConfig;
import io.atomofiron.tolmach.I;
import io.atomofiron.tolmach.R;
import io.atomofiron.tolmach.adapters.PhraseAdapter;
import io.atomofiron.tolmach.custom.ButtonList;
import io.atomofiron.tolmach.custom.VoicePowerIndicator;
import io.atomofiron.tolmach.utils.Phrase;
import io.atomofiron.tolmach.retrofit.Api;
import io.atomofiron.tolmach.retrofit.LangsResponse;
import io.atomofiron.tolmach.retrofit.TranslateResponse;
import io.atomofiron.tolmach.utils.Lang;
import io.atomofiron.tolmach.utils.recognition.GoogleRecognizer;
import io.atomofiron.tolmach.utils.recognition.VoiceRecognizer;
import io.atomofiron.tolmach.utils.recognition.YandexRecognizer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment implements VoiceRecognizer.VoiceListener, ButtonList.OnItemSelectedListener {
	private static String SRC_LANGS_ARG_KEY = "SRC_LANGS_ARG_KEY";
	private static String DST_LANGS_ARG_KEY = "DST_LANGS_ARG_KEY";
	private static String SRC_LANG_ARG_KEY = "SRC_LANG_ARG_KEY";
	private static String DST_LANG_ARG_KEY = "DST_LANG_ARG_KEY";
	private static String PHRASES_KEY = "PHRASES_KEY";
	private static String RECOGNIZER_STARTED_KEY = "RECOGNIZER_STARTED_KEY";
	private static String DST_LANGUAGES_ARE_LOADED_KEY = "DST_LANGUAGES_ARE_LOADED_KEY";
	private View fragmentView = null;
	private FloatingActionButton fab;
	private View anchor;
	private ButtonList buttonSrcList;
	private ButtonList buttonDstList;
	private VoicePowerIndicator indicator;

	private SharedPreferences sp;
	private Api retrofit;
	private VoiceRecognizer recognizer = null;
	private PhraseAdapter phraseAdapter;

	public MainFragment() {
		retrofit = App.getRetrofitApi();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		sp = I.sp(getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		phraseAdapter.shutdown();
		recognizer.destroy();
	}

	private void checkRecognizerSupplier() {
		String pref = sp.getString(I.PREF_RECOGNIZER, I.PREF_RECOGNIZER_YANDEX);

		if (pref.equals(I.PREF_RECOGNIZER_YANDEX) && (recognizer == null || recognizer instanceof GoogleRecognizer)) {
			if (recognizer != null)
				recognizer.destroy();

			recognizer = new YandexRecognizer(this);
			loadLangs();
		} else if (pref.equals(I.PREF_RECOGNIZER_GOOGLE) && (recognizer == null || recognizer instanceof YandexRecognizer)) {
			if (recognizer != null)
				recognizer.destroy();

			recognizer = new GoogleRecognizer(getActivity(), this);
			loadLangs();
		}
	}

	private void loadLangs() {
		recognizer.getLangs(getActivity(), new VoiceRecognizer.LanguagesReceiver() {
			public void onReceive(Lang srcLang, ArrayList<Lang> srcLangs) {
				buttonSrcList.setEnabled(true);
				buttonSrcList.setList(srcLangs);
				buttonSrcList.setCurrent(srcLang);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (fragmentView != null) {
			checkRecognizerSupplier();

			return fragmentView;
		}

		fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

		fab = (FloatingActionButton) fragmentView.findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!v.isActivated())
					checkPermissionAndStart();
				else
					reset();
			}
		});

		anchor = fragmentView.findViewById(R.id.language_bar);
		indicator = (VoicePowerIndicator) fragmentView.findViewById(R.id.indicator);
		indicator.setFAB(fab);

		buttonSrcList = (ButtonList) fragmentView.findViewById(R.id.src);
		buttonDstList = (ButtonList) fragmentView.findViewById(R.id.dst);
		if (savedInstanceState != null) {
			Lang lang;
			ArrayList<Lang> langs;
			// такая фигня это нормально? или я что-то упускаю?
			buttonSrcList.setList(langs = savedInstanceState.getParcelableArrayList(SRC_LANGS_ARG_KEY));
			buttonSrcList.setCurrent(lang = savedInstanceState.getParcelable(SRC_LANG_ARG_KEY));
			buttonDstList.setList(langs = savedInstanceState.getParcelableArrayList(DST_LANGS_ARG_KEY));
			buttonDstList.setCurrent(lang = savedInstanceState.getParcelable(DST_LANG_ARG_KEY));
			buttonSrcList.setEnabled(true);
			buttonDstList.setEnabled(true);
		}
		buttonSrcList.setOnItemSelectedListener(this);
		buttonDstList.setOnItemSelectedListener(this);

		RecyclerView recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		phraseAdapter = new PhraseAdapter(getActivity());
		recyclerView.setAdapter(phraseAdapter);
		phraseAdapter.setAutoVocalize(sp.getBoolean(I.PREF_AUTO_VOCALIZE, false));

		if (savedInstanceState != null) {
			ArrayList<Phrase> phrases;
			phraseAdapter.setPhrases(phrases = savedInstanceState.getParcelableArrayList(PHRASES_KEY));
		}

		checkRecognizerSupplier();
		return fragmentView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		updateMenuVocalizeIcon(menu.findItem(R.id.auto_vocalize), sp.getBoolean(I.PREF_AUTO_VOCALIZE, false));
	}

	private void updateMenuVocalizeIcon(MenuItem item, boolean on) {
		item.setIcon(on ? R.drawable.ic_volume_on : R.drawable.ic_volume_off);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.auto_vocalize:
				boolean vocalize = !sp.getBoolean(I.PREF_AUTO_VOCALIZE, false);
				sp.edit().putBoolean(I.PREF_AUTO_VOCALIZE, vocalize).apply();
				phraseAdapter.setAutoVocalize(vocalize);
				updateMenuVocalizeIcon(item, vocalize);
				break;
			case R.id.remove_all:
				phraseAdapter.clear();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		if (savedInstanceState == null)
			return;

		if (!savedInstanceState.getBoolean(DST_LANGUAGES_ARE_LOADED_KEY, false))
			onSrcLangChanged();
		else if (savedInstanceState.getBoolean(RECOGNIZER_STARTED_KEY, false))
			start();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(SRC_LANG_ARG_KEY, buttonSrcList.getCurrent());
		outState.putParcelable(DST_LANG_ARG_KEY, buttonDstList.getCurrent());
		outState.putParcelableArrayList(SRC_LANGS_ARG_KEY, buttonSrcList.getList());
		outState.putParcelableArrayList(DST_LANGS_ARG_KEY, buttonDstList.getList());
		outState.putParcelableArrayList(PHRASES_KEY, phraseAdapter.getPhrases());
		outState.putBoolean(RECOGNIZER_STARTED_KEY, fab.isActivated());
		outState.putBoolean(DST_LANGUAGES_ARE_LOADED_KEY, buttonDstList.isEnabled());
		super.onSaveInstanceState(outState);
	}

	private void onSrcLangChanged() {
		recognizer.cancel();
		reset();

		buttonSrcList.setEnabled(false);
		buttonDstList.setEnabled(false);

		fab.setEnabled(false);
		retrofit.getLangs(BuildConfig.API_KEY_TRANSLATE, buttonSrcList.getCurrent().code).enqueue(new Callback<LangsResponse>() {
			public void onResponse(Call<LangsResponse> call, Response<LangsResponse> response) {
				if (response.isSuccessful()) {
					buttonSrcList.setEnabled(true);
					buttonDstList.setList(response.body().getLangs(buttonSrcList.getCurrent().code));

					if (buttonDstList.getCurrent() != null) {
						buttonDstList.setEnabled(true);
						reset();
					}
				} else
					onFailure(call, new Throwable(response.message()));
			}
			public void onFailure(Call<LangsResponse> call, Throwable t) {
				buttonSrcList.setEnabled(true);
				Snackbar.make(anchor, t.getMessage(), Snackbar.LENGTH_INDEFINITE).setAction(R.string.retry, new View.OnClickListener() {
					public void onClick(View v) {
						onSrcLangChanged();
					}
				}).show();
			}
		});
	}

	private void checkPermissionAndStart() {
		if (!I.granted(getActivity(), Manifest.permission.RECORD_AUDIO)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				getActivity().requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, I.PERMISSION_REQUEST_CODE);
			else
				Snackbar.make(anchor, R.string.no_mic_permission, Snackbar.LENGTH_SHORT).show();
		} else
			start();
	}

	private void start() {
		fab.setActivated(true);

		if (!recognizer.start(buttonSrcList.getCurrent().getFullCode()))
			reset();
	}

	private void reset() {
		fab.setActivated(false);
		fab.setEnabled(true);

		recognizer.stop();
		indicator.resetScale();
	}

	@Override
	public void onPowerUpdated(float v) {
		indicator.setScale(v);
	}

	@Override
	public boolean onPartialResults(String text) {
		translate(text);

		return !sp.getBoolean(I.PREF_AUTO_VOCALIZE, false);
	}

	@Override
	public void onStopSelf() {
		reset();
	}

	@Override
	public void onError(String message) {
		reset();

		Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show();
	}

	private void translate(final String text) {
		I.log("translate: " + text);
		final String langCode = buttonDstList.getCurrent().code;
		retrofit.translate(BuildConfig.API_KEY_TRANSLATE, text, langCode, I.TEXT_FORMAT_PLAIN).enqueue(new Callback<TranslateResponse>() {
			public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
				if (response.isSuccessful()) {
					Phrase phrase = new Phrase(text, response.body().getText().get(0).replace("<censored>", "censored"), langCode);
					phraseAdapter.addPhrase(phrase);
					configureTranslateLabel();
				} else
					onFailure(call, new Throwable(response.message()));
			}
			public void onFailure(Call<TranslateResponse> call, Throwable t) {
				Snackbar.make(anchor, t.getMessage(), Snackbar.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onSelected(int buttonId, Lang lang) {
		switch (buttonId) {
			case R.id.src:
				onSrcLangChanged();
				break;
			case R.id.dst:
				// todo translate previous
				break;
		}
	}

	private void configureTranslateLabel() {
		if (fragmentView != null) {
			TextView yandexTranslateLabel = (TextView) fragmentView.findViewById(R.id.yandex_translate);
			yandexTranslateLabel.setText(Html.fromHtml(getString(R.string.use_service_translate)));
			yandexTranslateLabel.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}
}
