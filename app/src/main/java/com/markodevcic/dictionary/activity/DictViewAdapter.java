package com.markodevcic.dictionary.activity;

import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.markodevcic.dictionary.R;
import com.markodevcic.dictionary.translation.DictionaryEntry;
import com.markodevcic.dictionary.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

final class DictViewAdapter extends RecyclerView.Adapter<DictViewHolder> {

	private final List<DictionaryEntry> dictionaryEntries = new ArrayList<>();

	@Override
	public DictViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View dictView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dict_entry, parent, false);
		return new DictViewHolder(dictView);
	}

	@Override
	public void onBindViewHolder(DictViewHolder holder, int position) {
		DictionaryEntry dictionaryEntry = dictionaryEntries.get(position);
		holder.deMainText.setText(dictionaryEntry.getDeMainTerm());
		holder.frgnMainText.setText(dictionaryEntry.getFrgnMainTerm());
		String[] deAltTerms = dictionaryEntry.getDeAltTerms();
		if (deAltTerms != null) {
			holder.deAltText.setVisibility(View.VISIBLE);
			holder.deAltText.setText(StringUtils.join(deAltTerms, "\n").trim());
		} else {
			holder.deAltText.setVisibility(View.GONE);
		}
		String[] frgnAltTerms = dictionaryEntry.getFrgnAltTerms();
		if (frgnAltTerms != null) {
			holder.frgnAltText.setVisibility(View.VISIBLE);
			holder.frgnAltText.setText(StringUtils.join(frgnAltTerms, "\n").trim());
		} else {
			holder.frgnAltText.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return dictionaryEntries.size();
	}

	void addItem(DictionaryEntry dictionaryEntry) {
		dictionaryEntries.add(dictionaryEntry);
		notifyItemInserted(dictionaryEntries.size() - 1);
	}

	void clearItems() {
		int size = dictionaryEntries.size();
		for (int i = 0; i < size; i++) {
			dictionaryEntries.clear();
			notifyItemRangeRemoved(0, size);
		}
	}
}
