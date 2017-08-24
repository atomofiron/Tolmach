package io.atomofiron.tolmach.utils;

import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class Phrase {
	public String original;
	public String translate;
	public String code;

	public Phrase(String original, String translate, String code) {
		this.original = original;
		this.translate = translate;
		this.code = code;
	}

	public void vocalize(TextToSpeech textToSpeech) {
		textToSpeech.setLanguage(new Locale(code));
		textToSpeech.speak(translate, TextToSpeech.QUEUE_FLUSH, null);
	}

}
