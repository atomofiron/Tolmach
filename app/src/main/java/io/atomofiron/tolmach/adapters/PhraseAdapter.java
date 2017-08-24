package io.atomofiron.tolmach.adapters;

import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.atomofiron.tolmach.R;
import io.atomofiron.tolmach.utils.Phrase;

public class PhraseAdapter extends RecyclerView.Adapter<PhraseAdapter.Holder> {
	private TextToSpeech textToSpeech;
	private final ArrayList<Phrase> phrases = new ArrayList<>();

	public PhraseAdapter(TextToSpeech textToSpeech) {
		this.textToSpeech = textToSpeech;
	}

	@Override
	public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
	}

	@Override
	public void onBindViewHolder(Holder holder, int position) {
		holder.text.setText(phrases.get(position).translate);
	}

	@Override
	public int getItemCount() {
		return phrases.size();
	}

	public void addPhrase(Phrase phrase) {
		phrases.add(phrase);
		notifyDataSetChanged();
	}

	class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView text;

		public Holder(View itemView) {
			super(itemView);

			text = (TextView) itemView.findViewById(R.id.text);
			itemView.findViewById(R.id.button_vocalize).setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			phrases.get(getAdapterPosition()).vocalize(textToSpeech);
		}
	}
}
