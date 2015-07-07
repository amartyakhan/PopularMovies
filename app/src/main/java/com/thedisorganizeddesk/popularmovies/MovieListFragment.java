package com.thedisorganizeddesk.popularmovies;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.thedisorganizeddesk.popularmovies.api.MovieDbApi;
import com.thedisorganizeddesk.popularmovies.data.MovieContract;
import com.thedisorganizeddesk.popularmovies.model.Movies;
import com.thedisorganizeddesk.popularmovies.model.Results;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieListFragment extends Fragment {
    private final String LOG_TAG=this.getClass().getSimpleName();
    ArrayList<String> mPosterPaths;
    String mMovieDetails;
    private final String EXTRA_MESSAGE="MovieDetails";
    public MovieListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_movie_list_fragment, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            getMovieList(getView());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void getMovieList(View view){
        //check if network connection is there, otherwise return error
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //getting the sort by preference
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sort_by=sharedPref.getString(getString(R.string.pref_sort_title), getString(R.string.pref_sort_default_value));

            if(sort_by.compareTo("favorites")==0){ //change this logic
                //retrieve favorite movie details from db using content providers
                final Cursor cursor = getActivity().getContentResolver().query(
                        MovieContract.MovieEntries.CONTENT_URI,   // The content URI of the words table
                        null,                       // The columns to return for each row
                        null,                       // Selection criteria
                        null,                       // Selection criteria
                        null);                      // The sort order for the returned rows
                if(cursor == null){
                    Toast.makeText(getActivity()," No favorites found",Toast.LENGTH_SHORT).show();
                }
                else if(cursor.getCount()<1){
                    Toast.makeText(getActivity(),"No favorites found",Toast.LENGTH_SHORT).show();
                }
                else{
                    mPosterPaths = new ArrayList<String>();
                    //reading the data from the cursor
                    for(int i=0;i<cursor.getCount();i++){
                        cursor.moveToPosition(i);
                        String movie_details=cursor.getString(2);
                        try {
                            JSONObject movie_details_json = new JSONObject(movie_details);
                            mPosterPaths.add(movie_details_json.getString("poster_path"));
                        }catch(Exception e){

                        }
                    }
                    GridView gridview = (GridView) view.findViewById(R.id.movies_list_grid);
                    gridview.setAdapter(new ImageAdapter(getActivity(), mPosterPaths));

                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            //getting the movie details for the selected item
                            Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                            cursor.moveToPosition(position);
                            String movieDetails = cursor.getString(2);
                            intent.putExtra(EXTRA_MESSAGE, movieDetails);
                            startActivity(intent);
                        }
                    });
                }

            }
            else {
                // fetch data using Retrofit library
                String api = "http://api.themoviedb.org/3";
                String apiKey = "14e1d20ff72d6609b4526f32a29b8d20";
                RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(api).build();
                MovieDbApi movieDbApi = restAdapter.create(MovieDbApi.class);
                movieDbApi.getMovieList(sort_by, apiKey, new Callback<Movies>() {
                    @Override
                    public void success(Movies movies, Response response) {
                        //parsing the movies results
                        final List<Results> results = movies.getResults();

                        //saving the result in mMovieDetails for future parsing
                        Gson gson = new Gson();
                        mMovieDetails = gson.toJson(results);
                        mPosterPaths = new ArrayList<String>();
                        for (int i = 0; i < results.size(); i++) {
                            Results result = ((Results) results.get(i));
                            mPosterPaths.add(result.getPoster_path());
                        }
                        GridView gridview = (GridView) getActivity().findViewById(R.id.movies_list_grid);
                        gridview.setAdapter(new ImageAdapter(getActivity(), mPosterPaths));

                        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View v,
                                                    int position, long id) {
                                //getting the movie details for the selected item
                                Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                                Gson gson = new Gson();
                                String movieDetails = gson.toJson(results.get(position));
                                intent.putExtra(EXTRA_MESSAGE, movieDetails);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Context context = getActivity();
                        CharSequence text = ":( Not able to fetch movie list";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                });
            }
        } else {
            // display toast notification informing connectivity error
            Log.e(LOG_TAG,"No Network connection");
            Context context = getActivity();
            CharSequence text = "No Internet connection. :( Not able to fetch movie list";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            //TODO: Implement a separate view when movies are not accessible
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_movie_list, container, false);
        if(savedInstanceState==null || !savedInstanceState.containsKey("PosterPaths") || !savedInstanceState.containsKey("MovieDetails")){
            Log.v(LOG_TAG,"Loading movie details from network");
            getMovieList(view);
        }
        else{
            Log.v(LOG_TAG,"Loading movie details from savedBundleState");
            //retrieve the movie list from view
            mPosterPaths=savedInstanceState.getStringArrayList("PosterPaths");
            GridView gridview = (GridView) view.findViewById(R.id.movies_list_grid);
            gridview.setAdapter(new ImageAdapter(getActivity(), mPosterPaths));
            //retrieve the movie details from view
            mMovieDetails=savedInstanceState.getString("MovieDetails");

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    //getting the movie details for the selected item
                    Gson gson = new Gson();
                    Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                    final List<Results> results=gson.fromJson(mMovieDetails, List.class);
                    String dataToPass=gson.toJson(results.get(position));
                    intent.putExtra(EXTRA_MESSAGE, dataToPass );
                    startActivity(intent);
                }
            });

        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("PosterPaths", mPosterPaths);
        outState.putString("MovieDetails",mMovieDetails);
        super.onSaveInstanceState(outState);
    }
}
