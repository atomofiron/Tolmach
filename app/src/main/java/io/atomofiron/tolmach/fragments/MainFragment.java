package io.atomofiron.tolmach.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Build;
import android.speech.tts.TextToSpeech;
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
import io.atomofiron.tolmach.utils.Phrase;
import io.atomofiron.tolmach.retrofit.Api;
import io.atomofiron.tolmach.retrofit.LangsResponse;
import io.atomofiron.tolmach.retrofit.TranslateResponse;
import io.atomofiron.tolmach.utils.Lang;
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
	private FloatingActionButton frb;
	private View anchor;
	private ButtonList buttonSrcList;
	private ButtonList buttonDstList;

	private SharedPreferences sp;
	private Api retrofit;
	private VoiceRecognizer recognizer = null;
	private PhraseAdapter phraseAdapter;

	public static MainFragment newInstance(ArrayList<Lang> srcLangs, Lang srcLang) {

		Bundle args = new Bundle();
		args.putParcelableArrayList(SRC_LANGS_ARG_KEY, srcLangs);
		args.putParcelable(SRC_LANG_ARG_KEY, srcLang);

		MainFragment fragment = new MainFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public MainFragment() {
		retrofit = App.getRetrofitApi();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		I.log("onCreate()");
		setHasOptionsMenu(true);

		sp = I.sp(getActivity());
		recognizer = new YandexRecognizer(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		I.log("onDestroy()");
		phraseAdapter.shutdown();
		recognizer.cancel();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		I.log("onCreateView()");
		if (fragmentView != null)
			return fragmentView;

		fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

		TextView yandexTranslateLabel = (TextView) fragmentView.findViewById(R.id.yandex_translate);
		yandexTranslateLabel.setText(Html.fromHtml(getString(R.string.use_service_translate)));
		yandexTranslateLabel.setMovementMethod(LinkMovementMethod.getInstance());

		frb = (FloatingActionButton) fragmentView.findViewById(R.id.fab);
		frb.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!v.isActivated())
					checkPermissionAndStart();
				else
					reset();
			}
		});

		anchor = fragmentView.findViewById(R.id.language_bar);

		buttonSrcList = (ButtonList) fragmentView.findViewById(R.id.src);
		buttonDstList = (ButtonList) fragmentView.findViewById(R.id.dst);
		if (savedInstanceState == null) {
			buttonDstList.setEnabled(false);
			buttonSrcList.setOnItemSelectedListener(this);
			buttonSrcList.setList(Lang.parce(getArguments().getParcelableArrayList(SRC_LANGS_ARG_KEY)));
			buttonSrcList.setCurrent(Lang.parce(getArguments().getParcelable(SRC_LANG_ARG_KEY)));
		} else {
			buttonSrcList.setList(Lang.parce(savedInstanceState.getParcelableArrayList(SRC_LANGS_ARG_KEY)));
			buttonSrcList.setCurrent(Lang.parce(savedInstanceState.getParcelable(SRC_LANG_ARG_KEY)));
			buttonDstList.setList(Lang.parce(savedInstanceState.getParcelableArrayList(DST_LANGS_ARG_KEY)));
			buttonDstList.setCurrent(Lang.parce(savedInstanceState.getParcelable(DST_LANG_ARG_KEY)));
			buttonSrcList.setOnItemSelectedListener(this);
		}
		buttonDstList.setOnItemSelectedListener(this);

		RecyclerView recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		phraseAdapter = new PhraseAdapter(getActivity());
		recyclerView.setAdapter(phraseAdapter);
		phraseAdapter.setAutoVocalize(sp.getBoolean(I.PREF_AUTO_VOCALIZE, false));

		if (savedInstanceState != null) {
			phraseAdapter.setPhrases(Phrase.parce(savedInstanceState.getParcelableArrayList(PHRASES_KEY)));

		}

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
		if (item.getItemId() == R.id.auto_vocalize) {
			phraseAdapter.setAutoVocalize(!phraseAdapter.getAutoVocalize());
			updateMenuVocalizeIcon(item, phraseAdapter.getAutoVocalize());
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		I.log("onViewStateRestored()");

		if (savedInstanceState == null)
			return;

		if (!savedInstanceState.getBoolean(DST_LANGUAGES_ARE_LOADED_KEY, false))
			onSrcLangChanged();
		else if (savedInstanceState.getBoolean(RECOGNIZER_STARTED_KEY, false))
			start();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		I.log("onSaveInstanceState()");
		outState.putParcelable(SRC_LANG_ARG_KEY, buttonSrcList.getCurrent());
		outState.putParcelable(DST_LANG_ARG_KEY, buttonDstList.getCurrent());
		outState.putParcelableArrayList(SRC_LANGS_ARG_KEY, buttonSrcList.getList());
		outState.putParcelableArrayList(DST_LANGS_ARG_KEY, buttonDstList.getList());
		outState.putParcelableArrayList(PHRASES_KEY, phraseAdapter.getPhrases());
		outState.putBoolean(RECOGNIZER_STARTED_KEY, frb.isActivated());
		outState.putBoolean(DST_LANGUAGES_ARE_LOADED_KEY, buttonDstList.isEnabled());
		super.onSaveInstanceState(outState);
	}

	private void onSrcLangChanged() {
		frb.setEnabled(false);
		recognizer.cancel();

		buttonSrcList.setEnabled(false);
		buttonDstList.setEnabled(false);

		retrofit.getLangs(BuildConfig.API_KEY_TRANSLATE, buttonSrcList.getCurrent().code).enqueue(new Callback<LangsResponse>() {
			public void onResponse(Call<LangsResponse> call, Response<LangsResponse> response) {
				I.log("onResponse()");

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
				I.log("onFailure() " + t.getMessage());
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
		frb.setActivated(true);

		if (!recognizer.start(buttonSrcList.getCurrent().code))
			reset();
	}

	private void reset() {
		I.log("cancel()");
		frb.setActivated(false);
		frb.setEnabled(true);

		recognizer.cancel();
	}

	@Override
	public void onPowerUpdated(float v) {
		// todo visualisation
	}

	@Override
	public void onPartialResults(String text) {
		translate(text);
	}

	@Override
	public void onError(String message) {
		I.log("error: " + message);
			Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show();
	}

	private void translate(final String text) {
		I.log("translate: " + text);
		final String langCode = buttonDstList.getCurrent().code;
		retrofit.translate(BuildConfig.API_KEY_TRANSLATE, text, langCode, I.TEXT_FORMAT_PLAIN).enqueue(new Callback<TranslateResponse>() {
			public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
				if (response.isSuccessful()) {
					Phrase phrase = new Phrase(text, response.body().getText().get(0), langCode);
					phraseAdapter.addPhrase(phrase);
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
}
