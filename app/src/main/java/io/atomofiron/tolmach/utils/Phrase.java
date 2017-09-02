package io.atomofiron.tolmach.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class Phrase implements Parcelable {
	public String original;
	public String translate;
	public String code;
	public boolean isVoiced = false;
	private final String id = String.valueOf(System.currentTimeMillis());

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

	public String getId() {
		return id;
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
