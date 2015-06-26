package com.thedisorganizeddesk.popularmovies.api;

import com.thedisorganizeddesk.popularmovies.model.MovieTrailers;
import com.thedisorganizeddesk.popularmovies.model.Movies;
import com.thedisorganizeddesk.popularmovies.model.Results;


import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import java.util.List;

public interface MovieDbApi {
    @GET("/discover/movie")
    public void getMovieList(@Query("sort_by") String sort_by,@Query("api_key") String api_key, Callback<Movies> cb);

    @GET("/movie/{id}/videos")
    public void getMovieTrailers(@Path("id") String id,@Query("api_key") String api_key, Callback<MovieTrailers> cb);
}
