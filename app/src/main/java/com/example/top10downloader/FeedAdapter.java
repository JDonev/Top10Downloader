package com.example.top10downloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class FeedAdapter extends ArrayAdapter {
    private static final String TAG = "FeedAdapter";
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private List<FeedEntry> movies;


    public FeedAdapter(Context context, int resource, List<FeedEntry> movies) {
        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.movies = movies;
    }

    @Override
    public int getCount() {
        return movies.size();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Create a viewholder (a class further down) to not use findView every time, app runs smoother
        ViewHolder viewHolder;

        //Try to re-use views instead of making new ones everytime, less memory usage
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, parent, false);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        FeedEntry currentMovie = movies.get(position);


        viewHolder.tvName.setText(currentMovie.getName());
        viewHolder.tvGenre.setText(currentMovie.getGenre());
        viewHolder.tvSummary.setText(currentMovie.getSummary());


        return convertView;
    }


    //an inner-class that stores all the textviews that my layout has so to not call them every time. Less CPU and memory usage.
    private class ViewHolder {
        final TextView tvName;
        final TextView tvGenre;
        final TextView tvSummary;


        ViewHolder(View v) {
            this.tvName = v.findViewById(R.id.tvName);
            this.tvGenre = v.findViewById(R.id.tvGenre);
            this.tvSummary = v.findViewById(R.id.tvSummary);
        }

    }
}
