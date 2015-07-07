package com.thedisorganizeddesk.popularmovies;


import android.content.Context;
import android.os.AsyncTask;

//this would be an async task
public class FetchMovieTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private final Context mContext;

    public FetchMovieTask(Context context) {
        mContext = context;
    }


    @Override
    protected Void doInBackground(String... params) {

        return null;
    }
}
