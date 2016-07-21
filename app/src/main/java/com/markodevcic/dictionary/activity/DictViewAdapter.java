package com.markodevcic.dictionary.activity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.markodevcic.dictionary.R;
import com.markodevcic.dictionary.translation.DictionaryEntry;
import com.markodevcic.dictionary.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/*package*/ final class DictViewAdapter extends RecyclerView.Adapter<DictViewHolder> {

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
		holder.enMainText.setText(dictionaryEntry.getEnMainTerm());
		String[] deAltTerms = dictionaryEntry.getDeAltTerms();
		if (deAltTerms != null) {
			holder.deAltText.setVisibility(View.VISIBLE);
			holder.deAltText.setText(StringUtils.join(deAltTerms, "\n").trim());
		} else {
			holder.deAltText.setVisibility(View.GONE);
		}
		String[] frgnAltTerms = dictionaryEntry.getEnAltTerms();
		if (frgnAltTerms != null) {
			holder.enAltText.setVisibility(View.VISIBLE);
			holder.enAltText.setText(StringUtils.join(frgnAltTerms, "\n").trim());
		} else {
			holder.enAltText.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return dictionaryEntries.size();
	}

	/*package*/ void addItem(DictionaryEntry dictionaryEntry) {
		dictionaryEntries.add(dictionaryEntry);
		notifyItemInserted(dictionaryEntries.size() - 1);
	}

	/*package*/ void clearItems() {
		int size = dictionaryEntries.size();
		dictionaryEntries.clear();
		notifyItemRangeRemoved(0, size);
	}
}