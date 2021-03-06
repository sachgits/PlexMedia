package com.vacuum.app.cinema.Fragments.MainFragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.vacuum.app.cinema.Activities.WatchActivity;
import com.vacuum.app.cinema.Adapter.MoviesAdapter;
import com.vacuum.app.cinema.Fragments.MoreFragment;
import com.vacuum.app.cinema.MainActivity;
import com.vacuum.app.cinema.Model.Movie;
import com.vacuum.app.cinema.Model.MoviesResponse;
import com.vacuum.app.cinema.Model.OpenloadResult;
import com.vacuum.app.cinema.Model.OpenloadThumbnail;
import com.vacuum.app.cinema.Model.Slider;
import com.vacuum.app.cinema.R;
import com.vacuum.app.cinema.Utility.ApiClient;
import com.vacuum.app.cinema.Utility.ApiInterface;
import com.vacuum.app.cinema.Utility.GetOpenload;

import java.util.HashMap;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Home on 2/19/2018.
 */

public class HomeFragment extends Fragment implements View.OnClickListener{

    private static String TMBDB_API_KEY;
    RecyclerView movies_recycler1_UpComing,movies_recycler2_popular,movies_recycler3_top_rated;
    private Context mContext;
    AlertDialog.Builder alertadd;
    SliderLayout mDemoSlider;
    public static  ProgressBar progressBar;
    public static  ApiInterface apiService;
    TextView more_upcoming,more_Popular,more_top_rated;
    public  static  LinearLayout layout;
    public static InterstitialAd mInterstitialAd;
    AdView adView;
    String file_id,title;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = this.getActivity();

        SharedPreferences prefs = mContext.getSharedPreferences("Plex", Activity.MODE_PRIVATE);
        TMBDB_API_KEY = prefs.getString("TMBDB_API_KEY",null);

        movies_recycler1_UpComing=  view.findViewById(R.id.movies_recycler1_UpComing);
        movies_recycler2_popular=  view.findViewById(R.id.movies_recycler2_popular);
        movies_recycler3_top_rated=  view.findViewById(R.id.movies_recycler3_top_rated);
        adView = view.findViewById(R.id.adView);

        progressBar =  view.findViewById(R.id.progressBar);
        layout =  view.findViewById(R.id.layout);


        more_upcoming= view.findViewById(R.id.more_upcoming);
        more_Popular= view.findViewById(R.id.more_Popular);
        more_top_rated= view.findViewById(R.id.more_top_rated);

        more_upcoming.setOnClickListener(this);
        more_Popular.setOnClickListener(this);
        more_top_rated.setOnClickListener(this);



        mDemoSlider = view.findViewById(R.id.slider);


        //layoutManager= new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        movies_recycler1_UpComing.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        movies_recycler2_popular.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        movies_recycler3_top_rated.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));




        retrofit();
        Ads();
        return view;
    }

    private void Ads() {


        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-3341550634619945/9102050005");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }


    private void setupslider(final List<Slider> sliders) {


        for(final Slider s : sliders){
            TextSliderView textSliderView = new TextSliderView(mContext);
            // initialize a SliderLayout
            textSliderView
                    //.description(s.getTitle())
                    .image(s.getPosterPath())
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop);

            //add your extra information
            /*textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);*/

            textSliderView.image(s.getPosterPath())
                    .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                @Override
                public void onSliderClick(BaseSliderView slider) {
                    title = s.getTitle();
                    file_id = s.getFile_id();
                    new GetOpenload(mContext,file_id,title);
                }
            });
            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        //mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);

    }




    private void retrofit(){

        apiService =
                ApiClient.getClient(mContext).create(ApiInterface.class);

        Call<MoviesResponse> call_UpComing = apiService.getupcomingMovies(TMBDB_API_KEY,1);
        call_UpComing.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse>call, Response<MoviesResponse> response) {
                List<Movie> movies = response.body().getResults();
                movies_recycler1_UpComing.setAdapter(new MoviesAdapter(movies, mContext));
            }

            @Override
            public void onFailure(Call<MoviesResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e("tag", t.toString());
            }
        });
        //====================================================================================
        //====================================================================================
        //====================================================================================
        Call<MoviesResponse> call_popular = apiService.getpopularMovies(TMBDB_API_KEY,1);
        call_popular.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse>call, Response<MoviesResponse> response) {
                List<Movie> movies = response.body().getResults();
                movies_recycler2_popular.setAdapter(new MoviesAdapter(movies, mContext));
            }

            @Override
            public void onFailure(Call<MoviesResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e("tag", t.toString());
            }
        });
        //====================================================================================
        //====================================================================================
        //====================================================================================
        Call<MoviesResponse> call_top_rated = apiService.getTopRatedMovies(TMBDB_API_KEY,1);
        call_top_rated.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse>call, Response<MoviesResponse> response) {
                List<Movie> movies = response.body().getResults();
                movies_recycler3_top_rated.setAdapter(new MoviesAdapter(movies, mContext));
            }

            @Override
            public void onFailure(Call<MoviesResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e("tag", t.toString());
            }
        });

        //====================================================================================
        //====================================================================================
        //====================================================================================

        Call<List<Slider>> call_slider = apiService.getSlider("https://mohamedebrahim.000webhostapp.com/cimaclub/slider.php");
        call_slider.enqueue(new Callback<List<Slider>>() {
            @Override
            public void onResponse(Call<List<Slider>>call, Response<List<Slider>> response) {
                try {
                    List<Slider> slider = response.body();
                    Log.i("slider", slider.get(0).getTitle());
                    setupslider(slider);
                    progressBar.setVisibility(View.GONE);
                    layout.setVisibility(View.VISIBLE);
                }catch (Exception e ){
                    Log.e("Exception::",e.toString());
                }

            }

            @Override
            public void onFailure(Call<List<Slider>>call, Throwable t) {
                // Log error here since request failed
                Log.e("slider", t.toString());
            }
        });



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.more_upcoming:
                moreFragment("more_upcoming");
                break;
            case R.id.more_top_rated:
                moreFragment("more_top_rated");
                break;
            case R.id.more_Popular:
                moreFragment("more_Popular");
                break;
        }
    }

    private void moreFragment(String value) {
        Fragment fragment = new MoreFragment();
        Bundle bundle = new Bundle();
        bundle.putString("value", value);
        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment, MoreFragment.TAG_MORE_FRAGMENT);
        fragmentTransaction.addToBackStack(MainActivity.CURRENT_TAG);
        fragmentTransaction.commit();
    }








}
