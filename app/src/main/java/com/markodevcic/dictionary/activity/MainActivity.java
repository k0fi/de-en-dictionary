package com.markodevcic.dictionary.activity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.markodevcic.dictionary.R;
import com.markodevcic.dictionary.injection.AppComponent;
import com.markodevcic.dictionary.translation.DictionaryEntry;
import com.markodevcic.dictionary.translation.TranslationService;
import com.markodevcic.dictionary.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

public class MainActivity extends BaseActivity {

	@Inject
	TranslationService translationService;

	private DictViewAdapter dictViewAdapter;
	private RecyclerView recyclerView;
	private SearchView searchText;
	private ProgressBar progressBar;
	private Subscription translationSubscription = Subscriptions.unsubscribed();
	private PublishSubject<String> searchSubject = PublishSubject.create();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dictViewAdapter = new DictViewAdapter();
		setContentView(R.layout.activity_main);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		recyclerView = (RecyclerView) findViewById(R.id.results_view);
		recyclerView.setHasFixedSize(false);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(dictViewAdapter);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchText = (SearchView) findViewById(R.id.search_text);
		searchText.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, MainActivity.class)));
		searchText.setIconifiedByDefault(false);
		searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				searchSubject.onNext(query);
				progressBar.setVisibility(View.VISIBLE);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				searchSubject.onNext(newText);
				progressBar.setVisibility(View.VISIBLE);
				return false;
			}
		});
//		searchText.addTextChangedListener(new TextWatcher() {
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			}
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				searchSubject.onNext(s.toString());
//				if (start > 1) {
//					progressBar.setVisibility(View.VISIBLE);
//				}
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//			}
//		});

		searchSubject.asObservable().buffer(1000, TimeUnit.MILLISECONDS)
				.distinctUntilChanged()
				.filter(new Func1<List<String>, Boolean>() {
					@Override
					public Boolean call(List<String> strings) {
						return strings.size() > 0;
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<List<String>>() {
					@Override
					public void call(List<String> strings) {
						int size = strings.size();
						final String term = strings.get(size - 1);
						translationSubscription.unsubscribe();
						dictViewAdapter.clearItems();
						if (term.length() > 1) {
							translationSubscription.unsubscribe();
							dictViewAdapter.clearItems();
							translationSubscription = translationService.startQuery(term)
									.onBackpressureBuffer()
									.subscribeOn(Schedulers.io())
									.observeOn(AndroidSchedulers.mainThread())
									.subscribe(new Observer<DictionaryEntry>() {
										@Override
										public void onCompleted() {
											progressBar.setVisibility(View.GONE);
										}

										@Override
										public void onError(Throwable e) {

										}

										@Override
										public void onNext(DictionaryEntry dictionaryEntry) {
											dictViewAdapter.addItem(dictionaryEntry);
										}
									});
						} else {
							progressBar.setVisibility(View.GONE);
						}
					}
				});
	}

	private void enumerateChildren(String text) {
		int size = recyclerView.getChildCount();
		for (int i = 0; i < size; i++) {
			DictViewHolder viewHolder = (DictViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
			String term = viewHolder.frgnMainText.getText().toString();
			int index = term.indexOf(text);
			if (index >= 0) {
				Spannable spannable = new SpannableString(term);
				spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), index, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				viewHolder.frgnMainText.setText(spannable);
			}
		}
	}

	@Override
	protected void inject(AppComponent appComponent) {
		appComponent.inject(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		translationSubscription.unsubscribe();
	}

	private final static class DictViewAdapter extends RecyclerView.Adapter<DictViewHolder> {

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
				holder.deAltText.setText(StringUtils.join(deAltTerms, "\n").trim());
			}
			String[] frgnAltTerms = dictionaryEntry.getFrgnAltTerms();
			if (frgnAltTerms != null) {
				holder.frgnAltText.setText(StringUtils.join(frgnAltTerms, "\n").trim());
			}
		}

		@Override
		public int getItemCount() {
			return dictionaryEntries.size();
		}

		public void addItem(DictionaryEntry dictionaryEntry) {
			dictionaryEntries.add(dictionaryEntry);
			notifyItemInserted(dictionaryEntries.size() - 1);
		}

		public void clearItems() {
			int size = dictionaryEntries.size();
			for (int i = 0; i < size; i++) {
				dictionaryEntries.clear();
				notifyItemRangeRemoved(0, size);
			}
		}

		public void highlightText(String text) {

		}
	}

	private final static class DictViewHolder extends RecyclerView.ViewHolder {
		private final TextView deMainText;
		private final TextView frgnMainText;
		private final TextView deAltText;
		private final TextView frgnAltText;

		public DictViewHolder(View itemView) {
			super(itemView);
			deMainText = (TextView) itemView.findViewById(R.id.dict_entry_de_main_term);
			frgnMainText = (TextView) itemView.findViewById(R.id.dict_entry_frgn_main_term);
			deAltText = (TextView) itemView.findViewById(R.id.dict_entry_de_alt_term);
			frgnAltText = (TextView) itemView.findViewById(R.id.dict_entry_frgn_alt_term);
		}
	}
}
