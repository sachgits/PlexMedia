package com.vacuum.app.cinema.Fragments.MainFragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.vacuum.app.cinema.Adapter.MoviesAdapter;
import com.vacuum.app.cinema.Model.DoneMovies;
import com.vacuum.app.cinema.Model.Movie;
import com.vacuum.app.cinema.Model.MovieDetails;
import com.vacuum.app.cinema.Model.MoviesResponse;
import com.vacuum.app.cinema.R;
import com.vacuum.app.cinema.Utility.ApiClient;
import com.vacuum.app.cinema.Utility.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Home on 2/19/2018.
 */

public class DiscoverFragment extends Fragment {

    Context mContext;
    public static final String TAG_DISCVER_FRAGMENT = "TAG_DISCVER_FRAGMENT";
    RecyclerView discover_fragment_recylerview;
    private LinearLayoutManager mLayoutManager;
    List<Movie> movies ;
    TextView error;
    MoviesAdapter moviesAdapter;
    String TMBDB_API_KEY;
    ProgressBar progresssbar_watch;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.discoverfragment, container, false);
        mContext = this.getActivity();
         mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        mFirebaseAnalytics.setCurrentScreen(getActivity(), TAG_DISCVER_FRAGMENT, null );

        movies = new ArrayList<>();
        error=  view.findViewById(R.id.error);
        discover_fragment_recylerview=  view.findViewById(R.id.discover_fragment_recylerview);
        progresssbar_watch = view.findViewById(R.id.progresssbar_watch);
        //API_KEY = getString(R.string.TMBDB_API_KEY);
        SharedPreferences prefs = mContext.getSharedPreferences("Plex", Activity.MODE_PRIVATE);
        TMBDB_API_KEY = prefs.getString("TMBDB_API_KEY",null);

        retrofit();
        return view;
    }

    private void retrofit() {
        //====================================================================================
        //====================================================================================
        //====================================================================================

        ApiInterface apiService =
                ApiClient.getClient(mContext).create(ApiInterface.class);


        Call<DoneMovies> call = apiService.getDoneMovies("https://mohamedebrahim.000webhostapp.com/cimaclub/getDoneMovies.php");
        call.enqueue(new Callback<DoneMovies>() {
            @Override
            public void onResponse(Call<DoneMovies> call, Response<DoneMovies> response) {
                if(response.body() != null){
                    for(Movie m : response.body().getMovie()){
                        setup_gridMovie(m.getId());
                    }
                }else {
                    progresssbar_watch.setVisibility(View.GONE);
                    error.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<DoneMovies> call, Throwable t) {
                Log.e("TAG", t.toString());
            }
        });
    }

    private void setup_gridMovie(int id) {

        ApiInterface apiService =
                ApiClient.getClient(mContext).create(ApiInterface.class);


        Call<MovieDetails> call = apiService.getMovieDetails(id,TMBDB_API_KEY);
        call.enqueue(new Callback<MovieDetails>() {
            @Override
            public void onResponse(Call<MovieDetails> call, Response<MovieDetails> response) {

                if(response !=null){
                    Movie e = new Movie();
                    e.setTitle(response.body().getTitle());
                    e.setPosterPath(response.body().getPosterPath());
                    e.setVoteAverage(response.body().getVoteAverage());
                    e.setReleaseDate(response.body().getReleaseDate());
                    e.setOverview(response.body().getOverview());
                    e.setId(response.body().getId());
                    e.setVoteCount(response.body().getVoteCount());
                    e.setVoteAverage(response.body().getVoteAverage());
                    e.setBackdropPath(response.body().getBackdropPath());
                    e.setOriginalTitle(response.body().getOriginalTitle());

                    movies.add(e);
                    mLayoutManager = new GridLayoutManager(mContext,3);
                    discover_fragment_recylerview.setLayoutManager(mLayoutManager);
                    progresssbar_watch.setVisibility(View.GONE);
                    moviesAdapter = new MoviesAdapter(movies, mContext);
                    discover_fragment_recylerview.setAdapter(moviesAdapter);
                }

            }
            @Override
            public void onFailure(Call<MovieDetails> call, Throwable t) {
                Log.e("TAG", t.toString());
            }
        });
    }
}