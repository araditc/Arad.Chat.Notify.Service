package com.araditc.chat.core.Repository.Network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    public static final String BASE_URL = "";
    private static Retrofit retrofit = null;
    private static Retrofit retrofitBank = null;

    public static OkHttpClient.Builder client = new OkHttpClient.Builder()
            .addInterceptor(new HttpInterceptor());

    public static Retrofit getClient(String URL) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}