package com.thedisorganizeddesk.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    List<String> poster_paths;

    public ImageAdapter(Context c,List<String> poster_paths_passed) {
        mContext = c;
        //setup the poster paths
        this.poster_paths=poster_paths_passed;
    }

    public int getCount() {
        return poster_paths.toArray().length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView=new ImageView(mContext);
        Picasso.with(mContext).load("http://i.imgur.com/DvpvklR.png").into(imageView);
        return imageView;
    }
}
