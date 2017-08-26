package io.atomofiron.tolmach.adapters;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import io.atomofiron.tolmach.R;
import io.atomofiron.tolmach.utils.Phrase;

public class PhraseAdapter extends RecyclerView.Adapter<PhraseAdapter.Holder> {
	private TextToSpeech textToSpeech;
	private UtteranceListener utteranceListener;
	private final ArrayList<Phrase> phrases = new ArrayList<>();
	private final HashMap<String, Phrase> phrasesMap = new HashMap<>();
	private OnVocalizeStartListener onVocalizeStartListener;
	private boolean autoVocalize = false;

	public PhraseAdapter(Context context) {
		textToSpeech = new TextToSpeech(context, null);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			utteranceListener = new UtteranceListener(new Handler());
			textToSpeech.setOnUtteranceProgressListener(utteranceListener);
		}
	}

	@Override
	public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
	}

	@Override
	public void onBindViewHolder(Holder holder, int position) {
		Phrase phrase = phrases.get(position);
		holder.text.setText(phrase.translate);
		holder.buttonVocalize.setActivated(phrase.sound);
	}

	@Override
	public int getItemCount() {
		return phrases.size();
	}

	public boolean getAutoVocalize() {
		return autoVocalize;
	}

	public void setAutoVocalize(boolean autoVocalize) {
		this.autoVocalize = autoVocalize;
	}

	public void addPhrase(Phrase phrase) {
		phrases.add(0, phrase);
		phrasesMap.put(phrase.getId(), phrase);
		notifyDataSetChanged();

		if (autoVocalize)
			vocalize(phrase, true);
	}

	public void setPhrases(ArrayList<Phrase> phrases) {
		for (Phrase phrase : phrases)
			phrase.sound = false;

		this.phrases.clear();
		this.phrases.addAll(phrases);
		notifyDataSetChanged();
	}

	public ArrayList<Phrase> getPhrases() {
		return phrases;
	}

	public void clear() {
		phrases.clear();
		phrasesMap.clear();
		notifyDataSetChanged();

		shutUp();
	}

	private void vocalize(Phrase phrase, boolean addToqQueue) {
		textToSpeech.setLanguage(new Locale(phrase.code));
		HashMap<String, String> map = new HashMap<>();
		map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, phrase.getId());
		textToSpeech.speak(phrase.translate, addToqQueue ? TextToSpeech.QUEUE_ADD : TextToSpeech.QUEUE_FLUSH, map);

		if (onVocalizeStartListener != null)
			onVocalizeStartListener.onVocalize();
	}

	public void shutUp() {
		textToSpeech.stop();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
			utteranceListener.onStop();
	}

	public void shutdown() {
		textToSpeech.shutdown();
	}

	public void setOnVocalizeStartListener(OnVocalizeStartListener listener) {
		onVocalizeStartListener = listener;
	}

	class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView text;
		ImageButton buttonVocalize;

		Holder(View itemView) {
			super(itemView);

			text = (TextView) itemView.findViewById(R.id.text);
			buttonVocalize = (ImageButton) itemView.findViewById(R.id.button_vocalize);
			buttonVocalize.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			Phrase phrase = phrases.get(getAdapterPosition());

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				v.setActivated(!v.isActivated());

				if (v.isActivated()) {
					phrase.sound = true;
					vocalize(phrase, false);
				} else {
					shutUp();
					phrasesMap.get(utteranceListener.startedId).sound = false;
				}
			} else
				vocalize(phrase, false);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	private class UtteranceListener extends UtteranceProgressListener {
		private Handler handler;
		private String startedId = "";

		UtteranceListener(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void onStart(String utteranceId) {
			startedId = utteranceId;
			Phrase phrase = phrasesMap.get(utteranceId);
			if (phrase != null && !phrase.sound) {
				phrase.sound = true;
				notifyDataSetChanged();
			}
		}

		@Override
		public void onDone(String utteranceId) {
			startedId = "";
			Phrase phrase = phrasesMap.get(utteranceId);
			if (phrase != null && phrase.sound) {
				phrase.sound = false;
				notifyDataSetChanged();
			}
		}

		@Override
		public void onError(String utteranceId) {
			onDone(utteranceId);
		}

		public void onStop() {
			onDone(startedId);
		}

		void notifyDataSetChanged() {
			handler.post(new Runnable() {
				public void run() {
					PhraseAdapter.this.notifyDataSetChanged();
				}
			});
		}
	}

	public interface OnVocalizeStartListener {
		void onVocalize();
	}
}
