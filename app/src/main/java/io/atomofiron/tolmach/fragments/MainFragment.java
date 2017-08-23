package io.atomofiron.tolmach.fragments;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.atomofiron.tolmach.I;
import io.atomofiron.tolmach.R;
import io.atomofiron.tolmach.adapters.PhraseAdapter;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;

public class MainFragment extends Fragment implements RecognizerListener {
	private View fragmentView = null;
	private FloatingActionButton frb;

	private Recognizer recognizer = null;
	private PhraseAdapter phraseAdapter;

	public MainFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (fragmentView != null)
			return fragmentView;

		fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

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

		RecyclerView recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		phraseAdapter = new PhraseAdapter();
		recyclerView.setAdapter(phraseAdapter);

		return fragmentView;
	}

	private void createRecognizerAndStart() {
		recognizer = Recognizer.create(Recognizer.Language.RUSSIAN, Recognizer.Model.NOTES, this, true);
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
			phraseAdapter.addPhrase(recognition.getBestResultText());
	}

	@Override
	public void onRecognitionDone(Recognizer recognizer, Recognition recognition) {
		resetButton();
	}

	@Override
	public void onError(Recognizer recognizer, Error error) {
		I.log("error: "+error.getString());
		resetButton();

		if (error.getCode() != Error.ERROR_CANCELED)
			Snackbar.make(frb, error.getString(), Snackbar.LENGTH_LONG).show();
	}
}
