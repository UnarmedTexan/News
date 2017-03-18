package com.example.android.news;

/**
 * Created by Mark on 3/13/2017.
 */


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods needed for requesting and receiving article information from TheGuardian API.
 */
public class QueryUtils {

    //Tag for the log messages
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Search the TheGuardian API and return an {@link News} object to represent a news article
     */
    public static List<News> fetchNewsData(String requestUrl) {
        // Genereate URL object
        URL url = createURL(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }
        //Retrieve relevant fields from the JSON response and create an {@link News} object
        List<News> newsArticles = extractArticleInfoFromJson(jsonResponse);

        // return the {@link News}
        return newsArticles;
    }

    // Returns new URL object from the given string URL.
    private static URL createURL(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link News} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<News> extractArticleInfoFromJson(String newsJSON) {

        // Create an empty ArrayList for the purpose of adding newsArticles
        List<News> newsArticles = new ArrayList<>();

        // Try to parse the JSON response. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create JSONObject from newsJSON string.
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            // For a given article, extract the JSONObject associated with the
            // key called "response", which represent the top level key for the JSON response.
            JSONObject response = baseJsonResponse.getJSONObject("response");

            // Extract the JSONArray associated with the key called "results",
            // which represents a list of newsArticles.
            JSONArray newsArray = response.getJSONArray("results");

            // Create an {@link News} object in the newsArray for each news article.
            for (int i = 0; i < newsArray.length(); i++) {

                // Get the article at position i within the list of newsArticles
                JSONObject currentNews = newsArray.getJSONObject(i);

                // Extract the value for the key called "sectionName"
                String sectionTitle = currentNews.optString("sectionName");

                // Extract the value for the key called "webPublicationDate"
                // Reduce the String date to yyyy-MM-dd.
                String date = currentNews.optString("webPublicationDate").substring(0, 10);

                // Extract the value for the key called "webTitle"
                String byline = currentNews.optString("webTitle");
                // Trim the byline if it's length is over 60 characters
                if (byline.length() > 100) {
                    byline = byline.substring(0, 100) + "...";
                }
                // Extract the value for the key called "webUrl", which is the TheGuardian API url
                // for the given newsArticle.
                String url = currentNews.optString("webUrl");


                // Create a new {@link News} object with the article section, article byline, and
                // the url linking to the article found at TheGuardian.
                // Add the new {@link News} to the list of newsArticles.
                newsArticles.add(new News(sectionTitle, date, byline, url));
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the News JSON results", e);
        }
        // return the list of newsArticles
        return newsArticles;
    }
}