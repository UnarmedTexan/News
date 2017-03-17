package com.example.android.news;

/**
 * Created by Mark on 3/13/2017.
 */

public class News {

    // String for Section title
    private String mSection;

    // String for the date of the article
    private String mDate;

    // String for article title
    private String mArticle;

    // Website for article
    private String mUrl;



    public News (String section, String date, String article, String url){
        mSection = section;
        mDate = date;
        mArticle = article;
        mUrl = url;
    }

    public String getSection(){
        return mSection;
    }

    public String getDate(){
        return mDate;
    }

    public String getArticle() {
        return mArticle;
    }

    public String getUrl() {
        return mUrl;
    }
}
