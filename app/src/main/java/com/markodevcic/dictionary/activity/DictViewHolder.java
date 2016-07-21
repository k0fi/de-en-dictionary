package com.markodevcic.dictionary.activity;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.markodevcic.dictionary.R;

/*package*/ final class DictViewHolder extends RecyclerView.ViewHolder {
	/*package*/	final TextView deMainText;
	/*package*/ final TextView enMainText;
	/*package*/ final TextView deAltText;
	/*package*/ final TextView enAltText;

	public DictViewHolder(View itemView) {
		super(itemView);
		deMainText = (TextView) itemView.findViewById(R.id.dict_entry_de_main_term);
		enMainText = (TextView) itemView.findViewById(R.id.dict_entry_en_main_term);
		deAltText = (TextView) itemView.findViewById(R.id.dict_entry_de_alt_term);
		enAltText = (TextView) itemView.findViewById(R.id.dict_entry_en_alt_term);
	}
}