package com.markodevcic.dictionary.activity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.markodevcic.dictionary.R;
import com.markodevcic.dictionary.data.DatabaseHelper;
import com.markodevcic.dictionary.translation.DictionaryEntry;
import com.markodevcic.dictionary.translation.TranslationService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends AppCompatActivity
		implements Observer<List<DictionaryEntry>> {

	private TranslationService translationService;
	private DictViewAdapter dictViewAdapter;
	private RecyclerView recyclerView;
	private SearchView searchText;
	private ProgressBar progressBar;
	private Subscription translationSubscription = Subscriptions.unsubscribed();
	private LinearLayoutManager layoutManager;

	private boolean isSearching = false;
	private String searchTerm = "";

	private final Runnable highlightRunnable = new Runnable() {
		@Override
		public void run() {
			highlightVisibleItems();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		translationService = new TranslationService(new DatabaseHelper(this));
		dictViewAdapter = new DictViewAdapter();
		setContentView(R.layout.activity_main);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		recyclerView = (RecyclerView) findViewById(R.id.results_view);
		recyclerView.setHasFixedSize(false);
		layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(dictViewAdapter);
		recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
			@Override
			public void onChildViewAttachedToWindow(View view) {
				DictViewHolder dictViewHolder = (DictViewHolder) recyclerView.getChildViewHolder(view);
				setHighlightedText(dictViewHolder.deMainText);
				setHighlightedText(dictViewHolder.frgnMainText);
				setHighlightedText(dictViewHolder.deAltText);
				setHighlightedText(dictViewHolder.frgnAltText);
			}

			@Override
			public void onChildViewDetachedFromWindow(View view) {

			}
		});
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchText = (SearchView) findViewById(R.id.search_text);
		searchText.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, MainActivity.class)));
		searchText.setIconifiedByDefault(false);
		searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(final String term) {
				onSearch(term);
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String term) {
				onSearch(term);
				return false;
			}
		});
	}

	private void onSearch(String term) {
		progressBar.setVisibility(View.VISIBLE);
		isSearching = true;
		startSearch(term);
	}

	private void startSearch(final String term) {
		translationSubscription.unsubscribe();
		dictViewAdapter.clearItems();
		translationSubscription = translationService.startQuery(term)
				.buffer(200, TimeUnit.MILLISECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this);
		searchTerm = term.toLowerCase();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			searchText.setQuery(query, false);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			String query = intent.getDataString();
			searchText.setQuery(query, false);
		}
	}

	private void highlightVisibleItems() {
		if (isSearching) {
			return;
		}
		int firstPosition = layoutManager.findFirstVisibleItemPosition();
		int lastPosition = layoutManager.findLastVisibleItemPosition() + 1;
		for (int i = firstPosition; i < lastPosition; i++) {
			DictViewHolder dictViewHolder = (DictViewHolder) recyclerView.findViewHolderForLayoutPosition(i);
			if (dictViewHolder != null) {
				setHighlightedText(dictViewHolder.deMainText);
				setHighlightedText(dictViewHolder.frgnMainText);
				setHighlightedText(dictViewHolder.deAltText);
				setHighlightedText(dictViewHolder.frgnAltText);
			}
		}
	}

	private void setHighlightedText(TextView textView) {
		String originalTerm = textView.getText().toString();
		String term = originalTerm.toLowerCase();
		int index = term.indexOf(searchTerm);
		if (index < 0) {
			return;
		}

		Spannable spannable = new SpannableString(originalTerm);
		while (index >= 0) {
			spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)),
					index, index + searchTerm.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			index = term.indexOf(searchTerm, searchTerm.length() + index);
		}
		textView.setText(spannable);
	}

	@Override
	protected void onPause() {
		super.onPause();
		translationSubscription.unsubscribe();
	}

	@Override
	public void onCompleted() {
		progressBar.setVisibility(View.GONE);
		isSearching = false;
		progressBar.postDelayed(highlightRunnable, 200);
	}

	@Override
	public void onError(Throwable e) {
		progressBar.setVisibility(View.GONE);
		isSearching = false;
		Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNext(List<DictionaryEntry> dictionaryEntries) {
		int size = dictionaryEntries.size();
		for (int i = 0; i < size; i++) {
			dictViewAdapter.addItem(dictionaryEntries.get(i));
		}
	}
}