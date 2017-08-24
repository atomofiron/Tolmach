package io.atomofiron.tolmach.fragments;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;

public class MainFragment extends Fragment implements RecognizerListener, ButtonList.OnItemSelectedListener {
	private static String SRC_LANGS_ARG_KEY = "SRC_LANGS_ARG_KEY";
	private static String SRC_LANG_ARG_KEY = "SRC_LANG_ARG_KEY ";
	private View fragmentView = null;
	private FloatingActionButton frb;
	private View anchor;
	private ButtonList buttonSrcList;
	private ButtonList buttonDstList;

	private Api retrofit;
	private Recognizer recognizer = null;
	private PhraseAdapter phraseAdapter;
	private TextToSpeech textToSpeech;

	private boolean languagesCurrentlyLoaded = false;

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
		textToSpeech = new TextToSpeech(getActivity(), null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		textToSpeech.shutdown();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (fragmentView != null)
			return fragmentView;

		fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

		TextView yandex_translate = (TextView) fragmentView.findViewById(R.id.yandex_translate);
		yandex_translate.setText(Html.fromHtml(getString(R.string.use_service_translate)));
		yandex_translate.setMovementMethod(LinkMovementMethod.getInstance());

		frb = (FloatingActionButton) fragmentView.findViewById(R.id.fab);
		frb.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!v.isActivated()) {
					frb.setActivated(true);
					createRecognizerAndStart();
				} else {
					v.setEnabled(false);
					recognizer.cancel();
				}
			}
		});

		anchor = fragmentView.findViewById(R.id.language_bar);

		buttonSrcList = (ButtonList) fragmentView.findViewById(R.id.src);
		buttonDstList = (ButtonList) fragmentView.findViewById(R.id.dst);
		buttonSrcList.setOnItemSelectedListener(this);
		buttonDstList.setOnItemSelectedListener(this);
		buttonSrcList.setList(Lang.parce(getArguments().getParcelableArrayList(SRC_LANGS_ARG_KEY)));
		buttonSrcList.setCurrent(Lang.parce(getArguments().getParcelable(SRC_LANG_ARG_KEY)));

		RecyclerView recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		phraseAdapter = new PhraseAdapter(textToSpeech);
		recyclerView.setAdapter(phraseAdapter);

		return fragmentView;
	}

	private void onSrcLangChanged() {
		languagesCurrentlyLoaded = true;
		frb.setEnabled(false);
		if (recognizer != null)
			recognizer.cancel();

		buttonSrcList.setEnabled(false);
		buttonDstList.setEnabled(false);

		retrofit.getLangs(BuildConfig.API_KEY_TRANSLATE, buttonSrcList.getCurrent().code).enqueue(new Callback<LangsResponse>() {
			public void onResponse(Call<LangsResponse> call, Response<LangsResponse> response) {
				I.log("onResponse()");
				languagesCurrentlyLoaded = false;

				if (response.isSuccessful()) {
					buttonSrcList.setEnabled(true);
					buttonDstList.setList(response.body().getLangs(buttonSrcList.getCurrent().code));

					if (buttonDstList.getCurrent() != null) {
						resetButton();
						buttonDstList.setEnabled(true);
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

	private void createRecognizerAndStart() {
		recognizer = Recognizer.create(buttonSrcList.getCurrent().code, Recognizer.Model.NOTES, this, true);
		try {
			recognizer.start();
		} catch (SecurityException ignored) {
			resetButton();
		}
	}

	private void resetButton() {
		frb.setActivated(false);
		frb.setEnabled(true);
	}

	@Override
	public void onRecordingBegin(Recognizer recognizer) {}
	@Override
	public void onSpeechDetected(Recognizer recognizer) {}
	@Override
	public void onSpeechEnds(Recognizer recognizer) {}
	@Override
	public void onRecordingDone(Recognizer recognizer) {}
	@Override
	public void onSoundDataRecorded(Recognizer recognizer, byte[] bytes) {}

	@Override
	public void onPowerUpdated(Recognizer recognizer, float v) {
		// todo visualisation
	}

	@Override
	public void onPartialResults(Recognizer recognizer, Recognition recognition, boolean endOfUtterance) {
		if (endOfUtterance)
			translate(recognition.getBestResultText());
	}

	private void translate(final String text) {
		I.log("translate: " + text);
		final String langCode = buttonDstList.getCurrent().code;
		retrofit.translate(BuildConfig.API_KEY_TRANSLATE, text, langCode, I.TEXT_FORMAT_PLAIN).enqueue(new Callback<TranslateResponse>() {
			public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
				if (response.isSuccessful()) {
					Phrase phrase = new Phrase(text, response.body().getText().get(0), langCode);
					phraseAdapter.addPhrase(phrase);

					if (I.sp(getActivity()).getBoolean(I.PREF_AUTO_VOCALIZE, false))
						phrase.vocalize(textToSpeech);
				} else
					onFailure(call, new Throwable(response.message()));
			}
			public void onFailure(Call<TranslateResponse> call, Throwable t) {
				Snackbar.make(anchor, t.getMessage(), Snackbar.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onRecognitionDone(Recognizer recognizer, Recognition recognition) {
		if (!languagesCurrentlyLoaded)
			resetButton();
	}

	@Override
	public void onError(Recognizer recognizer, Error error) {
		I.log("error: "+error.getString());
		if (!languagesCurrentlyLoaded)
			resetButton();

		if (error.getCode() != Error.ERROR_CANCELED)
			Snackbar.make(anchor, error.getString(), Snackbar.LENGTH_LONG).show();
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
