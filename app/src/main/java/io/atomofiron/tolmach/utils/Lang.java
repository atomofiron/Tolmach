package io.atomofiron.tolmach.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Lang implements Parcelable, Cloneable {
	public String name;
	public String code;
	public String country;

	private Lang(Lang lang) {
		this.name = lang.name == null ? "" : lang.name;
		this.code = lang.code == null ? "" : lang.code;
	}

	public Lang(String code, String name) {
		this.name = name == null ? "" : name;
		this.code = code == null ? "" : code;
	}

	public Lang(String code, String name, String country) {
		this.name = name == null ? "" : name;
		this.code = code == null ? "" : code;
		this.country= country == null ? "" : country;
	}

	private Lang(Parcel in) {
		code = in.readString();
		name = in.readString();
	}

	public String getFullCode() {
		return code + "-" + country;
	}

	public static final Creator<Lang> CREATOR = new Creator<Lang>() {
		@Override
		public Lang createFromParcel(Parcel in) {
			return new Lang(in);
		}

		@Override
		public Lang[] newArray(int size) {
			return new Lang[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(code);
		dest.writeString(name);
	}

	@Override
	public String toString() {
		return code+"-"+name;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass() == getClass() && code.equals(((Lang) obj).code);
	}

	@Override
	protected Lang clone() {
		return new Lang(this);
	}
}
