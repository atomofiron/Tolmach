package io.atomofiron.tolmach.utils.recognition;

import android.content.Context;

import java.util.ArrayList;

import io.atomofiron.tolmach.I;
import io.atomofiron.tolmach.utils.Lang;
import io.atomofiron.tolmach.utils.LangUtils;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;

public class YandexRecognizer extends VoiceRecognizer implements RecognizerListener {
	private Recognizer recognizer = null;

	public YandexRecognizer(VoiceListener listener) {
		setVoiceListener(listener);
	}

	@Override
	public void getLangs(Context context, LanguagesReceiver listener) {
		ArrayList<Lang> srcLangs = LangUtils.getSrcLangs(I.SPEECH_CODES);
		Lang srcLang = LangUtils.getSrcLang(context.getResources(), srcLangs, context.getResources().getConfiguration().locale.getLanguage());

		listener.onReceive(srcLang, srcLangs);
	}

	@Override
	public boolean start(String code) {
		recognizer = Recognizer.create(code.split("-")[0], Recognizer.Model.NOTES, this, true);
		try {
			recognizer.start();
			return true;
		} catch (SecurityException ignored) {
			return false;
		}
	}

	@Override
	public void cancel() {
		if (recognizer != null)
			recognizer.cancel();
	}

	@Override
	public void destroy() {
		cancel();
	}

	@Override
	public void onRecordingBegin(Recognizer recognizer) {

	}

	@Override
	public void onSpeechDetected(Recognizer recognizer) {

	}

	@Override
	public void onSpeechEnds(Recognizer recognizer) {

	}

	@Override
	public void onRecordingDone(Recognizer recognizer) {

	}

	@Override
	public void onSoundDataRecorded(Recognizer recognizer, byte[] bytes) {

	}

	@Override
	public void onPowerUpdated(Recognizer recognizer, float v) {
		if (voiceListener != null)
			voiceListener.onPowerUpdated(v);
	}

	@Override
	public void onPartialResults(Recognizer recognizer, Recognition recognition, boolean b) {
		if (voiceListener != null && b)
			voiceListener.onPartialResults(recognition.getBestResultText());
	}

	@Override
	public void onRecognitionDone(Recognizer recognizer, Recognition recognition) {

	}

	@Override
	public void onError(Recognizer recognizer, Error error) {
		if (voiceListener != null && error.getCode() != Error.ERROR_CANCELED)
			voiceListener.onError(error.getString());
	}
}
