package com.thedisorganizeddesk.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.net.URI;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListFragment extends Fragment {
    private final String LOG_TAG=this.getClass().getSimpleName();

    public MovieListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //check if network connection is there, otherwise return error
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            new DiscoverMoviesTask().execute();
        } else {
            // display error

        }
        return inflater.inflate(R.layout.fragment_movie_list, container, false);
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the URL as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // Logged for now by the AsyncTask's onPostExecute method.
    private class DiscoverMoviesTask extends AsyncTask<Void, Void, String> {
        private final String LOG_TAG=this.getClass().getSimpleName();
        @Override
        protected String doInBackground(Void... params) {

            // URL for calling the API is needed
            String baseURL="api.themoviedb.org";
            String apiKey="14e1d20ff72d6609b4526f32a29b8d20";
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority(baseURL)
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("api_key",apiKey);
            String url=builder.toString();
            //fetch URL data
            return ""; //the return value will be used by onPostExecute to update UI
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            return;
        }
    }


}
