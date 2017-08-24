package io.atomofiron.tolmach;

import android.util.Log;

public class I {
	public static final String TEXT_FORMAT_PLAIN = "plain";
	public static final String[] SPEECH_CODES = new String[]{ "ru", "en", "uk", "tr" };

	public static void log(String log) {
		if (BuildConfig.DEBUG)
			Log.e("atomofiron", log);
		else
			Log.d("atomofiron", log);
	}
}
