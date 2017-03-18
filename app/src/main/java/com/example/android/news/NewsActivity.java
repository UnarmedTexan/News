package com.example.android.news;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<News>> {

    private static final String LOG_TAG = NewsActivity.class.getName();
    // Constant value for the news loader ID. This is required if multiple loaders are being used.
    private static final int NEWS_LOADER_ID = 1;
    // Main portion of the Url for the news data requested from TheGuardian API
    private static final String GUARDIAN_REQUEST_URL = "http://content.guardianapis.com/search?";


    private static String apiKey = "6ae62e1c-8f0b-4375-985b-9a6656c703bf";
    // Adapter for the list of news articles
    private NewsAdapter mAdapter;

    // When the list is empty, this is the TextView to be displayed.
    private TextView mEmptyStateView;

    // While waiting for the app to retrieve data, this is the Progress spinner to be displayed.
    private ProgressBar loadingData;

    // Initialize the ImageView used as the clickable icon to start the search.
    private ImageView termSearch;

    // Initialize the String object used to hold the user entered search criteria.
    private String searchTerm;

    // Initialize the ListView used to display a list of articles associated with the search criteria.
    private ListView newsListView;

    // Get a reference to the LoaderManager, in order to interact with loaders.
    private LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        // Find the item touched by the user to initiate a news article search
        termSearch = (ImageView) findViewById(R.id.subject_search);
        // Find the reference to the {@link ListView} in the layout
        newsListView = (ListView) findViewById(R.id.list);

        // Set the progress spinner to the Progress Bar view in news_activity.xml
        loadingData = (ProgressBar) findViewById(R.id.loading_progress);
        // Make the progress spinner invisible
        loadingData.setVisibility(View.INVISIBLE);

        // Initialize the LoaderManager.
        loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(NEWS_LOADER_ID, null, NewsActivity.this).forceLoad();

        // Find the EditText view where the user enters search criteria
        final EditText subjectEntered = (EditText) findViewById(R.id.subject_text);

        //Create a new news adapter which take an empty list of articles as input
        mAdapter = new NewsAdapter(NewsActivity.this, new ArrayList<News>());
        // Set the adapter on the {@link ListView}, so the list can be filled with articles
        // in the user interface.
        newsListView.setAdapter(mAdapter);

        // View to be displayed when no list items are available.
        mEmptyStateView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateView);

        termSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                // Convert EditText input to the String searchTerm
                searchTerm = subjectEntered.getText().toString();

                // Verify a search term has been entered
                if (searchTerm == null || searchTerm.equals("")) {
                    // Inform user no search term has been entered
                    Toast.makeText(NewsActivity.this, getString(R.string.no_search_term),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Hide keyboard once the search icon is touched
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            getSystemService(NewsActivity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(subjectEntered.getWindowToken(), 0);

                    // Find the Progress spinner & make it visible to the app user
                    loadingData.setVisibility(View.VISIBLE);

                    // Create a reference to ConnectivityManager & check for network connectivity
                    ConnectivityManager cm =
                            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                    // Check for a network connection and begin the process of fetching data
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                        // Create a reference to the LoaderManager to interact with the Loader(s).
                        LoaderManager loaderManager = getSupportLoaderManager();

                        // Loader reset - this is to clear out any existing news data.
                        loaderManager.restartLoader(NEWS_LOADER_ID, null, NewsActivity.this).
                                forceLoad();
                    } else {
                        // Hide the progress spinner
                        loadingData.setVisibility(View.GONE);
                        // Inform the user there is "No network connection"
                        mEmptyStateView.setText(R.string.no_connection);
                    }
                }
            }
        });

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Locate the current news being clicked on by the user
                News currentNews = mAdapter.getItem(position);

                // Prepare the the String URL to be passed into the intent constructor by first
                // converting it into a URL object.
                Uri newsUri = Uri.parse(currentNews.getUrl());

                // Generate a new intent to view the article URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Use the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        String section = sharedPreferences.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default));

        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("section", section);
        uriBuilder.appendQueryParameter("from-date", "2015-01-01");
        uriBuilder.appendQueryParameter("q", searchTerm);
        uriBuilder.appendQueryParameter("api-key", apiKey);

        Log.v(LOG_TAG, uriBuilder.toString());

        //  Generate a new loader for a particular URL
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        // Set TextView ID empty_view to display "No articles found."
        mEmptyStateView.setText(R.string.no_articles);

        // Set Progress spinner to invisible
        loadingData.setVisibility(View.INVISIBLE);

        //Clear the adapter of any previous news search results
        mAdapter.clear();

        // If there is a valid list of {@link News}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        // Loader reset - this is to clear out any existing news data.
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
