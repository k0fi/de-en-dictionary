package com.markodevcic.dictionary.activity;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.markodevcic.dictionary.R;

final class DictViewHolder extends RecyclerView.ViewHolder {
	final TextView deMainText;
	final TextView frgnMainText;
	final TextView deAltText;
	final TextView frgnAltText;

	public DictViewHolder(View itemView) {
		super(itemView);
		deMainText = (TextView) itemView.findViewById(R.id.dict_entry_de_main_term);
		frgnMainText = (TextView) itemView.findViewById(R.id.dict_entry_frgn_main_term);
		deAltText = (TextView) itemView.findViewById(R.id.dict_entry_de_alt_term);
		frgnAltText = (TextView) itemView.findViewById(R.id.dict_entry_frgn_alt_term);
	}
}
