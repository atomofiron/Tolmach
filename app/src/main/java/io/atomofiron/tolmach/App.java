package io.atomofiron.tolmach;

import android.app.Application;

import ru.yandex.speechkit.SpeechKit;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		SpeechKit.getInstance().configure(getApplicationContext(), BuildConfig.API_KEY);
	}
}
