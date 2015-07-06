package com.thedisorganizeddesk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.thedisorganizeddesk.popularmovies.api.MovieDbApi;
import com.thedisorganizeddesk.popularmovies.model.MovieReview;
import com.thedisorganizeddesk.popularmovies.model.MovieReviews;
import com.thedisorganizeddesk.popularmovies.model.MovieTrailer;
import com.thedisorganizeddesk.popularmovies.model.MovieTrailers;
import com.thedisorganizeddesk.popularmovies.model.Movies;
import com.thedisorganizeddesk.popularmovies.model.Results;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsActivityFragment extends Fragment {
    private final String LOG_TAG=this.getClass().getSimpleName();
    private final String EXTRA_MESSAGE="MovieDetails";
    private String mMovieDetails;
    private String mMovieId;
    private String mTrailerLink;
    public MovieDetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view=inflater.inflate(R.layout.fragment_movie_details, container, false);

        //get the favorite button
        ImageButton b= (ImageButton) view.findViewById(R.id.favoriteButton);
        b.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addFavorite(v);
            }
        });

        //get the JSON String from the Intent
        Intent intent = getActivity().getIntent();
        mMovieDetails= intent.getStringExtra(EXTRA_MESSAGE);

        //Parse the JSON String to a JSONObject
        //TODO: alternatively use gson to convert the JSON string to a Movies object and fetch properties from there
        try{
            JSONObject movie_detail= new JSONObject(mMovieDetails);
            mMovieId=movie_detail.getString("id");
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
            //TODO: Handle Picasso Exception
            Picasso.with(getActivity()).load(basepath + relativePath).into(backdrop);

            //get the imageView for backdrop
            ImageView poster= (ImageView) view.findViewById(R.id.movie_poster);
            //setup the URL for backdrop
            String basepath_poster="https://image.tmdb.org/t/p/w185/";
            String relativePath_poster=movie_detail.getString("poster_path");
            //set the image for backdrop
            //TODO: Handle Picasso Exception
            Picasso.with(getActivity()).load(basepath_poster + relativePath_poster).into(poster);

            // set the trailer link for the View trailer button
            String api="http://api.themoviedb.org/3";
            String apiKey="14e1d20ff72d6609b4526f32a29b8d20";
            RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(api).build();
            MovieDbApi movieDbApi= restAdapter.create(MovieDbApi.class);
            movieDbApi.getMovieTrailers(movie_detail.getString("id"), apiKey, new Callback<MovieTrailers>() {
                @Override
                public void success(MovieTrailers movieTrailers, Response response) {
                    List<MovieTrailer> trailers=movieTrailers.getResults();
                    for(MovieTrailer movieTrailer:trailers){
                        if(movieTrailer.getSite().compareTo("YouTube")==0){
                            mTrailerLink=movieTrailer.getKey();
                            break;
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    mTrailerLink="";
                }
            });

            //setting onclick listener
            Button bt= (Button) view.findViewById(R.id.button_trailer);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playYoutubeVideo();
                }
            });

            //set the movie reviews for the movie details page
            movieDbApi.getMovieReviews(movie_detail.getString("id"), apiKey, new Callback<MovieReviews>() {
                @Override
                public void success(MovieReviews movieReviews, Response response) {
                    List<MovieReview> movieReviewList = movieReviews.getResults();
                    LinearLayout ll= (LinearLayout) view.findViewById(R.id.movie_reviews);
                    for (MovieReview movieReview : movieReviewList) {
                        TextView movie_review_content= new TextView(view.getContext());
                        movie_review_content.setGravity(Gravity.LEFT);
                        movie_review_content.setPadding(10,10,10,10);
                        movie_review_content.setText(movieReview.getContent());
                        TextView movie_review_author = new TextView(view.getContext());
                        movie_review_author.setGravity(Gravity.RIGHT);
                        movie_review_author.setPadding(10,10,10,10);
                        movie_review_author.setTextColor(R.color.abc_primary_text_material_dark);
                        movie_review_author.setText("- Reviewed by: "+movieReview.getAuthor());
                        ll.addView(movie_review_content);
                        ll.addView(movie_review_author);
                    }
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });

        }catch (JSONException e){
            Log.e(LOG_TAG, "Error Parsing JSON: ", e);
        }



        return view;
    }

    public void playYoutubeVideo(){
        if(mTrailerLink == "" || mTrailerLink == null){
            //show some error regarding trailer not available
            Toast.makeText(getActivity(), "No trailer available", Toast.LENGTH_LONG).show();
        }
        else{
            //get the video link
            String youtubeLink="https://www.youtube.com/watch?v=";
            //set intent
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink+mTrailerLink));
            //start intent
            startActivity(intent);
        }
    }

    public void addFavorite(View view){
        //check the current state of the movie as favorite
        AddMovieTask addMovieTask=new AddMovieTask(getActivity());
        addMovieTask.execute(mMovieId,mMovieDetails);
    }
}
