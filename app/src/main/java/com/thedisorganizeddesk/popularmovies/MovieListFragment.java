package com.thedisorganizeddesk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
            getMovieList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void getMovieList(){
        //check if network connection is there, otherwise return error
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            new DiscoverMoviesTask().execute();
        } else {
            // display error
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
        if(savedInstanceState==null || !savedInstanceState.containsKey("PosterPaths")){
            Log.v(LOG_TAG,"Loading movie details from network");
            getMovieList();
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
                    try {
                        JSONObject movie_list_json = new JSONObject(mMovieDetails);
                        JSONObject movieDetails=movie_list_json.getJSONArray("results").getJSONObject(position);
                        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, movieDetails.toString());
                        startActivity(intent);
                    }catch (JSONException e){
                        Log.e(LOG_TAG,"Error parsing JSON: ",e);
                    }
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
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sort_by=sharedPref.getString(getString(R.string.pref_sort_title), getString(R.string.pref_sort_default_value));
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority(baseURL)
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("api_key", apiKey)
                    .appendQueryParameter("sort_by",sort_by);
            String url=builder.toString();
            //fetch URL data
            mMovieDetails=downloadUrl(url);
            return mMovieDetails; //the return value will be used by onPostExecute to update UI
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(final String result) {
            //parse the result into an JSON Object
            final JSONObject movies_list_json;
            mPosterPaths =new ArrayList<String>();
            JSONArray movies_list_array=new JSONArray();
            try{
                movies_list_json=new JSONObject(result);
                movies_list_array=movies_list_json.getJSONArray("results");
                for(int i=0;i<movies_list_array.length();i++){
                    JSONObject movie = movies_list_array.getJSONObject(i);
                    mPosterPaths.add(movie.getString("poster_path"));
                }
            }catch(JSONException e){
                Log.e(LOG_TAG,"Error parsing JSON:",e);
            }


            GridView gridview = (GridView) getActivity().findViewById(R.id.movies_list_grid);
            gridview.setAdapter(new ImageAdapter(getActivity(), mPosterPaths));

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    //Toast.makeText(getActivity(), "" + position,Toast.LENGTH_SHORT).show();

                    //getting the movie details for the selected item
                    try {
                        JSONObject movie_list_json = new JSONObject(result);
                        JSONObject movieDetails=movie_list_json.getJSONArray("results").getJSONObject(position);
                        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, movieDetails.toString());
                        startActivity(intent);
                    }catch (JSONException e){
                        Log.e(LOG_TAG,"Error parsing JSON: ",e);
                    }
                }
            });
            return;
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                URL url = new URL(myurl);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
                return moviesJsonStr;
            }catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movies data, there's no point in
                // attempting to parse it.
                return null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }
    }


}
