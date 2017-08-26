package io.atomofiron.tolmach.utils.recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

import io.atomofiron.tolmach.I;
import io.atomofiron.tolmach.R;
import io.atomofiron.tolmach.utils.Lang;
import io.atomofiron.tolmach.utils.LangUtils;

import static android.speech.SpeechRecognizer.*;

public class GoogleRecognizer extends VoiceRecognizer implements RecognitionListener {
	private SpeechRecognizer speechRecognizer;
	private PackageManager packManager;
	private Intent speechRecognizerIntent;
	private ArrayList<String> errors = new ArrayList<>();

	public GoogleRecognizer(Context context, VoiceListener listener) {
		voiceListener = listener;

		packManager = context.getPackageManager();
		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
		speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechRecognizerIntent.putExtra(
				RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		speechRecognizerIntent.putExtra(
				RecognizerIntent.EXTRA_CALLING_PACKAGE,
				context.getPackageName());
		speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		speechRecognizer.setRecognitionListener(this);

		configureErrors(context);
	}


	@Override
	public void getLangs(Context context, final LanguagesReceiver listener) {
		Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
		context.sendOrderedBroadcast(intent, null, new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				ArrayList<Lang> langs = LangUtils.getSrcLangs(getResultExtras(true).getCharSequenceArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES));
				listener.onReceive(LangUtils.getSrcLang(context.getResources(), langs, context.getResources().getConfiguration().locale.getLanguage()), langs);
			}
		}, null, 1, null, null);
	}

	private void configureErrors(Context context) {
		errors.add(context.getString(R.string.error, "unknown"));
		errors.add(context.getString(R.string.error, "network timeout"));
		errors.add(context.getString(R.string.error, "network"));
		errors.add(context.getString(R.string.error, "audio"));
		errors.add(context.getString(R.string.error, "server"));
		errors.add(context.getString(R.string.error, "client"));
		errors.add(context.getString(R.string.error, "speech timeout"));
		errors.add(context.getString(R.string.error, "no match"));
		errors.add(context.getString(R.string.error, "recognizer busy"));
		errors.add(context.getString(R.string.error, "insufficient permissions"));
	}

	@Override
	public boolean start(String code) {
		if (packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0).size() > 0) {
			speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, code.toUpperCase());

			startListening();
			return true;
		} else
			return false;
	}

	private void startListening() {
		speechRecognizer.startListening(speechRecognizerIntent);
	}

	@Override
	public void stop() {
		speechRecognizer.stopListening();
	}

	@Override
	public void cancel() {
		speechRecognizer.cancel();
	}

	@Override
	public void destroy() {
		cancel();
		speechRecognizer.destroy();
	}

	@Override
	public void onReadyForSpeech(Bundle params) {

	}

	@Override
	public void onBeginningOfSpeech() {

	}

	@Override
	public void onRmsChanged(float rmsdB) {
		if (voiceListener != null)
			voiceListener.onPowerUpdated((rmsdB + 2) / 12);
	}

	@Override
	public void onBufferReceived(byte[] buffer) {

	}

	@Override
	public void onEndOfSpeech() {
		I.log("onEndOfSpeech()");
	}

	@Override
	public void onError(int error) {
		I.log("onError() "+error);
		if (voiceListener != null) {
			if (error == ERROR_NO_MATCH || error == ERROR_SPEECH_TIMEOUT || error == ERROR_CLIENT)
				voiceListener.onStopSelf();
			else
				voiceListener.onError(errors.get(error >= errors.size() ? 0 : error));
		}
	}

	@Override
	public void onResults(Bundle results) {
		ArrayList<String> resultsArray = results.getStringArrayList(RESULTS_RECOGNITION);
		if (voiceListener != null && resultsArray != null && resultsArray.size() > 0)
			voiceListener.onPartialResults(resultsArray.get(resultsArray.size() - 1));

		startListening();
	}

	@Override
	public void onPartialResults(Bundle partialResults) {

	}


	@Override
	public void onEvent(int eventType, Bundle params) {

	}
}
