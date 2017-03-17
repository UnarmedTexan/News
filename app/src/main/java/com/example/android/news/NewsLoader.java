package com.example.android.news;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

/**
 * Created by Mark on 3/13/2017.
 */

// Loads a list of news objects by using an AsyncTaskLoader to perform a network request to a
// specified Url, which includes user supplied search criteria
public class NewsLoader extends AsyncTaskLoader<List<News>> {

    //Tag for log messages
    private static final String LOG_TAG = News.class.getName();

    // Search URL
    private String mUrl;

    /**
     * Constructs a new {@link NewsLoader}.
     * @param context of the activity
     * @param url to load data from
     */
    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<News> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of articles.
        List<News> newsArticles = QueryUtils.fetchBookData(mUrl);
        return newsArticles;
    }
}
