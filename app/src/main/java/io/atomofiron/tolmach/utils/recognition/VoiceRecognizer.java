package io.atomofiron.tolmach.utils.recognition;

import android.content.Context;

import java.util.ArrayList;

import io.atomofiron.tolmach.utils.Lang;

public abstract class VoiceRecognizer {
	protected VoiceListener voiceListener;

	public abstract void getLangs(Context context, LanguagesReceiver listener);
	public abstract boolean start(String code);
	public abstract void cancel();
	public abstract void destroy();

	public final void setVoiceListener(VoiceListener listener) {
		voiceListener = listener;
	}

	public interface VoiceListener {
		void onPowerUpdated(float v);
		void onPartialResults(String text);
		void onStopSelf();
		void onError(String message);
	}

	public interface LanguagesReceiver {
		void onReceive(Lang srcLang, ArrayList<Lang> srcLangs);
	}
}
