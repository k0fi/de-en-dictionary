package com.markodevcic.dictionary.activity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.markodevcic.dictionary.injection.AppComponent;
import com.markodevcic.dictionary.translation.DictionaryEntry;
import com.markodevcic.dictionary.translation.TranslationService;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity extends BaseActivity {

	@Inject
	TranslationService translationService;

	private DictViewAdapter dictViewAdapter;
	private RecyclerView recyclerView;
	private SearchView searchText;
	private ProgressBar progressBar;
	private Subscription translationSubscription = Subscriptions.unsubscribed();
	private LinearLayoutManager layoutManager;

	private boolean isSearching = false;
	private String searchTerm = "";

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
		layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(dictViewAdapter);
		recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
			@Override
			public void onChildViewAttachedToWindow(View view) {
				DictViewHolder dictViewHolder = (DictViewHolder)recyclerView.getChildViewHolder(view);
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
				progressBar.setVisibility(View.VISIBLE);
				startSearch(term);
				isSearching = true;
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String term) {
				progressBar.setVisibility(View.VISIBLE);
				isSearching = true;
				startSearch(term);
				return false;
			}
		});
	}

	private void startSearch(final String term) {
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
						isSearching = false;
						searchTerm = term;
						progressBar.postDelayed(new Runnable() {
							@Override
							public void run() {
								highlightSearchTerm();
							}
						}, 500);
					}

					@Override
					public void onError(Throwable e) {
						progressBar.setVisibility(View.GONE);
						isSearching = false;
						Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onNext(DictionaryEntry dictionaryEntry) {
						dictViewAdapter.addItem(dictionaryEntry);
					}
				});
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

	private void highlightSearchTerm() {
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
		int index = term.indexOf(searchTerm.toLowerCase());
		if (index >= 0) {
			Spannable spannable = new SpannableString(originalTerm);
			spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)),
					index, index + searchTerm.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			textView.setText(spannable);
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
}