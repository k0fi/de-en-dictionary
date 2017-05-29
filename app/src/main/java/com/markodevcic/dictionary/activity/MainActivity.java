package com.markodevcic.dictionary.activity;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.markodevcic.dictionary.R;
import com.markodevcic.dictionary.data.DatabaseHelper;
import com.markodevcic.dictionary.translation.DictionaryEntry;
import com.markodevcic.dictionary.translation.TranslationService;
import com.markodevcic.dictionary.utils.IOSchedulersTransformer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity
		implements Observer<List<DictionaryEntry>> {

	private final PublishSubject<String> searchSubject = PublishSubject.create();
	private final Runnable highlightRunnable = new Runnable() {
		@Override
		public void run() {
			highlightVisibleItems();
		}
	};
	private final Observable.Transformer IOSchedulersTransformer = new IOSchedulersTransformer<>();
	
	private TranslationService translationService;
	private DictViewAdapter dictViewAdapter;
	private RecyclerView recyclerView;
	private SearchView searchView;
	private TextView noResultsText;
	private ProgressBar progressBar;
	private ViewGroup buttonsHost;
	private Subscription translationSubscription = Subscriptions.unsubscribed();
	private LinearLayoutManager layoutManager;
	private boolean isSearching = false;
	private String searchTerm = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		translationService = new TranslationService(new DatabaseHelper(this));
		dictViewAdapter = new DictViewAdapter();
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		setupViews();
		setupRecyclerView();
		setupSearchView();
		setupSearchSubject();
		setupViewTreeObserver();
		setupGermanClickListeners();
	}
	
	private void setupViews() {
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		buttonsHost = (ViewGroup) findViewById(R.id.buttons_host);
		noResultsText = (TextView) findViewById(R.id.text_no_results);
		noResultsText.setVisibility(View.GONE);
	}
	
	private void setupRecyclerView() {
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
				setHighlightedText(dictViewHolder.enMainText);
				setHighlightedText(dictViewHolder.deAltText);
				setHighlightedText(dictViewHolder.enAltText);
			}

			@Override
			public void onChildViewDetachedFromWindow(View view) {

			}
		});
	}
	
	private void setupViewTreeObserver() {
		final View view = findViewById(R.id.main_view_host);
		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
				view.getWindowVisibleDisplayFrame(r);
				if (view.getRootView().getHeight() - (r.bottom - r.top) > 500) {
					onKeyboardShown();
				} else {
					onKeyboardHidden();
				}
			}
		});
	}
	
	private void onKeyboardShown() {
		buttonsHost.animate()
				.setStartDelay(150)
				.alpha(1)
				.scaleY(1)
				.scaleX(1);
		buttonsHost.setVisibility(View.VISIBLE);
	}
	
	private void onKeyboardHidden() {
		buttonsHost.setVisibility(View.GONE);
		buttonsHost.setAlpha(0);
		buttonsHost.setScaleX(0);
		buttonsHost.setScaleY(0);
	}
	
	private void setupSearchView() {
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) findViewById(R.id.search_text);
		searchView.setIconifiedByDefault(false);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, MainActivity.class)));
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(final String term) {
				searchSubject.onNext(term);
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String term) {
				searchSubject.onNext(term);
				return false;
			}
		});
	}
	
	private void setupSearchSubject() {
		searchSubject.throttleWithTimeout(400, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.doOnNext(new Action1<String>() {
					@Override
					public void call(String s) {
						noResultsText.setVisibility(View.GONE);
					}
				})
				.subscribe(new Action1<String>() {
					@Override
					public void call(String term) {
						onSearch(term);
					}
				});
	}
	
	private void setupGermanClickListeners() {
		setGermanButtonClickListener((Button)findViewById(R.id.btn_sharf_s));
		setGermanButtonClickListener((Button)findViewById(R.id.btn_e));
		setGermanButtonClickListener((Button)findViewById(R.id.btn_a));
		setGermanButtonClickListener((Button)findViewById(R.id.btn_u));
	}
	
	private void setGermanButtonClickListener(final Button btn) {
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchView.setQuery(searchView.getQuery().toString() + btn.getText().toString(), false);
			}
		});
	}

	private void onSearch(String term) {
		progressBar.setVisibility(View.VISIBLE);
		isSearching = true;
		
		translationSubscription.unsubscribe();
		dictViewAdapter.clearItems();
		
		translationSubscription = translationService.startQuery(term)
				.buffer(200, TimeUnit.MILLISECONDS)
				.onBackpressureBuffer()
				.compose(IOSchedulersTransformer)
				.subscribe(this);
		
		searchTerm = term.toLowerCase();
	}

	@Override
	protected void onResume() {
		super.onResume();
		progressBar.setVisibility(View.GONE);
		isSearching = false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			searchView.setQuery(query, false);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			String query = intent.getDataString();
			searchView.setQuery(query, false);
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
				setHighlightedText(dictViewHolder.enMainText);
				setHighlightedText(dictViewHolder.deAltText);
				setHighlightedText(dictViewHolder.enAltText);
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
		if (dictViewAdapter.getItemCount() > 0) {
			progressBar.postDelayed(highlightRunnable, 200);
		} else {
			if (!searchTerm.isEmpty()) {
				noResultsText.setVisibility(View.VISIBLE);
			}
		}
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