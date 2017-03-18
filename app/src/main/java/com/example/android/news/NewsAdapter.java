package com.example.android.news;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Mark on 3/13/2017.
 */

public class NewsAdapter extends ArrayAdapter<News> {

    private static final String LOG_TAG = NewsAdapter.class.getSimpleName();


    public NewsAdapter(Context context, ArrayList<News> newsInfo) {
        super(context, 0, newsInfo);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (convertView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_news, parent, false);
        }
        //get the {@link News} object for this list position
        News currentArticle = getItem(position);

        //Find the TextView for the list_news.xml layout with the ID list_section.
        TextView sectionTitleView = (TextView) listItemView.findViewById(R.id.list_section);

        // Get the news section for currentNews object and set this text on the News TextView
        // for ID list_section.
        sectionTitleView.setText(currentArticle.getSection());

        //Find the TextView for the list_news.xml layout with the ID list_date.
        TextView dateView = (TextView) listItemView.findViewById(R.id.list_date);

        //Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = simpleDateFormat.parse(currentArticle.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat newDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String finalDate = newDateFormat.format(date);
        // Display the date of the current article in the dateView TextView
        dateView.setText(finalDate);

        //Find the TextView for the list_news.xml layout with the ID list_article.
        TextView articleByline = (TextView) listItemView.findViewById(R.id.list_article);

        // Get the news article byline from the currentNews object and set this text on the News
        // TextView for ID list_article.
        articleByline.setText(currentArticle.getArticle());

        return listItemView;
    }
}
