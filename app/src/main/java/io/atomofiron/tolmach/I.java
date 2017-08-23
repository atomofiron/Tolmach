package io.atomofiron.tolmach;

import android.util.Log;

public class I {

	public static void log(String log) {
		if (BuildConfig.DEBUG)
			Log.e("atomofiron", log);
		else
			Log.d("atomofiron", log);
	}
}
