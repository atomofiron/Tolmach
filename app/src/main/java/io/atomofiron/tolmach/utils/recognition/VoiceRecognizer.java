package io.atomofiron.tolmach.utils.recognition;

public abstract class VoiceRecognizer {
	protected VoiceListener voiceListener;

	public abstract boolean start(String code);
	public abstract void cancel();

	public final void setVoiceListener(VoiceListener listener) {
		voiceListener = listener;
	}

	public interface VoiceListener {
		void onPowerUpdated(float v);
		void onPartialResults(String text);
		void onError(String message);
	}
}
