package io.atomofiron.tolmach;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import ru.yandex.speechkit.Vocalizer;

public class I {
	public static final String TEXT_FORMAT_PLAIN = "plain";
	public static final int PERMISSION_REQUEST_CODE = 1;

	public static final String[] SPEECH_CODES = new String[] {
			Vocalizer.Language.ENGLISH,
			Vocalizer.Language.RUSSIAN,
			Vocalizer.Language.TURKISH,
			Vocalizer.Language.UKRAINIAN
	};

	public static final String PREF_AUTO_VOCALIZE = "PREF_AUTO_VOCALIZE";

	public static void log(String log) {
		if (BuildConfig.DEBUG)
			Log.e("atomofiron", log);
		else
			Log.d("atomofiron", log);
	}

	public static SharedPreferences sp(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static boolean granted(Context context, String permission) {
		return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
	}
}
