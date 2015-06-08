package com.thedisorganizeddesk.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsActivityFragment extends Fragment {
    private final String EXTRA_MESSAGE="MovieDetails";
    public MovieDetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        String movieDetails= intent.getStringExtra(EXTRA_MESSAGE);
        View view=inflater.inflate(R.layout.fragment_movie_details, container, false);
        TextView textView=(TextView) view.findViewById(R.id.movie_details);
        textView.setText(movieDetails);
        return view;
    }
}
