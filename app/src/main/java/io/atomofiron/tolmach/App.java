package io.atomofiron.tolmach;

import android.app.Application;

import io.atomofiron.tolmach.retrofit.Api;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.yandex.speechkit.SpeechKit;

public class App extends Application {
	private static Api retrofitApi;

	@Override
	public void onCreate() {
		super.onCreate();

		SpeechKit.getInstance().configure(getApplicationContext(), BuildConfig.API_KEY);

		retrofitApi = new Retrofit.Builder()
				.baseUrl("https://translate.yandex.net/")
				.addConverterFactory(GsonConverterFactory.create())
				.build().create(Api.class);
	}

	public static Api getRetrofitApi() {
		return retrofitApi;
	}
}
