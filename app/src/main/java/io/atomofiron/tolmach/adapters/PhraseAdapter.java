package io.atomofiron.tolmach.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.atomofiron.tolmach.R;

public class PhraseAdapter extends RecyclerView.Adapter<PhraseAdapter.Holder> {
	private final ArrayList<String> phrases = new ArrayList<>();

	@Override
	public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
	}

	@Override
	public void onBindViewHolder(Holder holder, int position) {
		holder.text.setText(phrases.get(position));
	}

	@Override
	public int getItemCount() {
		return phrases.size();
	}

	public void addPhrase(String phrase) {
		phrases.add(phrase);
		notifyDataSetChanged();
	}

	class Holder extends RecyclerView.ViewHolder {
		TextView text;

		public Holder(View itemView) {
			super(itemView);

			text = (TextView) itemView.findViewById(R.id.text);
		}
	}

}
