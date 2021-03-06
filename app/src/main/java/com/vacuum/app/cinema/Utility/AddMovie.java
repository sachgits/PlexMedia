package com.vacuum.app.cinema.Utility;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AddMovie {
    String ROOT_URL = "https://mohamedebrahim.000webhostapp.com/";

    public AddMovie(final Context mContext, String id, final String title, final String file_id)
    {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ApiInterface api = retrofit.create(ApiInterface.class);
        api.addMovie(
                id,
                title,
                file_id
        ).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    String responsse ;
                    try {
                        responsse  = response.body().string();
                        Log.e("TAG", "Add Movie "+responsse);
                        new GetOpenload(mContext,file_id,title);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", "Unable to submit post to API.");
            }
        });


    }
}
