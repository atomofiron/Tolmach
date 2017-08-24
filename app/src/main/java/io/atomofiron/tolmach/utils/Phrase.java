package io.atomofiron.tolmach.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;

public class Phrase implements Parcelable {
	public String original;
	public String translate;
	public String code;

	public Phrase(String original, String translate, String code) {
		this.original = original;
		this.translate = translate;
		this.code = code;
	}

	protected Phrase(Parcel in) {
		original = in.readString();
		translate = in.readString();
		code = in.readString();
	}

	public void vocalize(TextToSpeech textToSpeech) {
		textToSpeech.setLanguage(new Locale(code));
		textToSpeech.speak(translate, TextToSpeech.QUEUE_FLUSH, null);
	}

	public static ArrayList<Phrase> parce(ArrayList<Parcelable> list) {
		ArrayList<Phrase> phrases = new ArrayList<>();
		// такая фигня это нормально? или я что-то упускаю?
		for (Parcelable p : list)
			phrases.add((Phrase) p);

		return phrases;
	}

	public static final Creator<Phrase> CREATOR = new Creator<Phrase>() {
		@Override
		public Phrase createFromParcel(Parcel in) {
			return new Phrase(in);
		}

		@Override
		public Phrase[] newArray(int size) {
			return new Phrase[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(original);
		dest.writeString(translate);
		dest.writeString(code);
	}
}
