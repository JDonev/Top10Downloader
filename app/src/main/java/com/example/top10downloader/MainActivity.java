package com.example.top10downloader;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listMovies;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topMovies/limit=%d/xml";
    private int feedLimit = 10;
    private String feedCachedUrl = "INVALIDATED";
    public static final String STATE_URL = "feedUrl";
    public static final String STATE_LIMIT = "feedLimit";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listMovies = (ListView) findViewById(R.id.xmlListView);

        //If the bundle isnt null it retrieves url and limit from the bundle. (Data is saved in the onSaveInstanceState to store values when you rotate to landscape)
        // It has to be here because the onRestoreSaved is called after onCreate in the lifecycle, which is a problem because the downloading of the url happens here.
        if (savedInstanceState != null) {
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }
        downloadUrl(String.format(feedUrl, feedLimit));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);

        if (feedLimit == 10) {
            menu.findItem(R.id.mnu10).setChecked(true);
        } else {
            menu.findItem(R.id.mnu25).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch (id) {
            case R.id.mnuMovies:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topMovies/limit=%d/xml";
                break;
            case R.id.mnuAlbums:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topalbums/limit=%d/xml";
                break;
            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.mnu10:
            case R.id.mnu25:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " setting feedlimit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " feedlimit unchanged");
                }
                break;
            case R.id.mnuRefresh:
                feedCachedUrl = "INVALIDATED";
                break;
            default:
                return super.onOptionsItemSelected(item);
            //default should always be included when creating code to react to menuchoices(?). It shouldnt
            //execute but if a submeny was created android will trigger a call to this method when the sumbmeny opens.
            //The only thing that submeny will call is the "downloadUrl" with an empty string.


        }


        downloadUrl(String.format(feedUrl, feedLimit));
        return true;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT, feedLimit);
        super.onSaveInstanceState(outState);
    }


    public void downloadUrl(String feedUrl) {
        //to prevent from reloading the same url this if statement compares the last one loaded. If its
        //not the same it loads the new one and sets the variable "feedCachedUrl" to the new url
        if (!feedUrl.equalsIgnoreCase(feedCachedUrl)) {
            Log.d(TAG, "downloadUrl: starting Asynctask");
            DownloadData downloadData = new DownloadData();
            //Dont forget to declare android:usesCleartextTraffic="true" in the manifest. Its not standard "true" sinc
            downloadData.execute(feedUrl);
            feedCachedUrl = feedUrl;
            Log.d(TAG, "downloadUrl: done");

        } else {
            Log.d(TAG, "downloadUrl: URL not changed");
        }

    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //  Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseMovies parseMovies = new ParseMovies();
            parseMovies.parse(s);

            //   ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseMovies.getMovies());
            //   listMovies.setAdapter(arrayAdapter);

            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record, parseMovies.getMovies());
            listMovies.setAdapter(feedAdapter);

        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error Downloading");
            }
            return rssFeed;

        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code was" + response);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close();
                return xmlResult.toString();
            } catch (MalformedURLException e) {
                //MalformedException is a subclass from IOException. It has to come first or else
                //we wont see the "invalid URL" message, since the "catch" are checked in order.
                Log.e(TAG, "downloadXML: Invalid URL" + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data" + e.getMessage());
            } catch (SecurityException e) {
                //Dont forget to put the permission for internet in the manifest. (Its a "normal" one, not a "dangerous" one
                Log.e(TAG, "downloadXML: Security Exception. Need permission?" + e.getMessage());
            }
            return null;
        }
    }
}
