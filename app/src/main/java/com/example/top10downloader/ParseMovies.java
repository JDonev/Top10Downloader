package com.example.top10downloader;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

public class ParseMovies {

    private static final String TAG = "ParseMovies";
    private ArrayList<FeedEntry> movies;

    public ParseMovies() {
        this.movies = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getMovies() {
        return movies;
    }

    public boolean parse(String xmlData) {
        boolean status = true;
        FeedEntry currentRecord = null;
        boolean inEntry = false;
        String textValue = "";


        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        //       Log.d(TAG, "parse: Starting tag for: " + tagName);
                        if ("entry".equalsIgnoreCase(tagName)) {
                            inEntry = true;
                            currentRecord = new FeedEntry();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        //      Log.d(TAG, "parse: Ending tag for " + tagName);
                        if (inEntry) {
                            if ("entry".equalsIgnoreCase(tagName)) {
                                movies.add(currentRecord);
                                inEntry = false;
                            } else if ("name".equalsIgnoreCase(tagName)) {
                                currentRecord.setName(textValue);
                            } else if ("category".equalsIgnoreCase(tagName)) {
                                currentRecord.setGenre(xpp.getAttributeValue(null, "term"));
                            } else if ("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            } else if ("price".equalsIgnoreCase(tagName)) {
                                currentRecord.setPrice(textValue);
                            } else if ("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            } else if ("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue);
                            }
                        }
                        break;
                    default:
                        //Nothing else to do

                }
                eventType = xpp.next();


            }


        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }

        return status;
    }
}
