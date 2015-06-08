package com.thedisorganizeddesk.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsActivityFragment extends Fragment {
    private final String LOG_TAG=this.getClass().getSimpleName();
    private final String EXTRA_MESSAGE="MovieDetails";
    public MovieDetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_movie_details, container, false);

        //get the JSON String from the Intent
        Intent intent = getActivity().getIntent();
        String movieDetails= intent.getStringExtra(EXTRA_MESSAGE);

        //Parse the JSON String to a JSONObject
        try{
            JSONObject movie_detail= new JSONObject(movieDetails);
            TextView title=(TextView) view.findViewById(R.id.movie_title);
            title.setText(movie_detail.getString("original_title"));
            TextView details=(TextView) view.findViewById(R.id.movie_details);
            details.setText(movie_detail.getString("overview"));
            TextView rating=(TextView) view.findViewById(R.id.movie_rating);
            rating.setText("Rating: "+movie_detail.getString("vote_average"));
            TextView releaseDate=(TextView) view.findViewById(R.id.movie_release_date);
            releaseDate.setText("Releases on: "+movie_detail.getString("release_date"));

        }catch (JSONException e){
            Log.e(LOG_TAG, "Error Parsing JSON: ", e);
        }
        return view;
    }
}
