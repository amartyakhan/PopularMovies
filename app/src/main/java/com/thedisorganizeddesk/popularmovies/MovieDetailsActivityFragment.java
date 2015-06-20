package com.thedisorganizeddesk.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
        //TODO: alternatively use gson to convert the JSON string to a Movies object and fetch properties from there
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

            //get the imageView for backdrop
            ImageView backdrop= (ImageView) view.findViewById(R.id.movie_backdrop_image);
            //setup the URL for backdrop
            String basepath="https://image.tmdb.org/t/p/w780/";
            String relativePath=movie_detail.getString("backdrop_path");
            //set the image for backdrop
            Picasso.with(getActivity()).load(basepath + relativePath).into(backdrop);

            //get the imageView for backdrop
            ImageView poster= (ImageView) view.findViewById(R.id.movie_poster);
            //setup the URL for backdrop
            String basepath_poster="https://image.tmdb.org/t/p/w185/";
            String relativePath_poster=movie_detail.getString("poster_path");
            //set the image for backdrop
            Picasso.with(getActivity()).load(basepath_poster + relativePath_poster).into(poster);

        }catch (JSONException e){
            Log.e(LOG_TAG, "Error Parsing JSON: ", e);
        }
        return view;
    }
}
