package com.thedisorganizeddesk.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;


public class ImageCursorAdapter extends CursorAdapter{
    public ImageCursorAdapter(Context context, Cursor c,int flags) {
        super(context, c,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_list_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //read the poster path from the cursor
        //String movie_details=cursor.getString()

    }
}
