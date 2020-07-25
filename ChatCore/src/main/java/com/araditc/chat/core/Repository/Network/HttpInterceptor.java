package com.araditc.chat.core.Repository.Network;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.araditc.chat.core.BuildConfig;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class HttpInterceptor implements Interceptor {

    public static final String TAG = "interceptor";

    @Override
    @NonNull
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder headerBuilder = original.newBuilder()
                .header("User-Agent", "AradITC-ChatCore")
                .header("Accept", "application/json");
        Request request = headerBuilder
                .method(original.method(), original.body())
                .build();

        Response response = chain.proceed(request);

        if (BuildConfig.DEBUG) {
            long t1 = System.nanoTime();
            long t2 = System.nanoTime();
            @SuppressLint("DefaultLocale") String responseLog = String.format("Received response for %s in %.1fms%n",
                    response.request().url(), (t2 - 0) / 1e6d);

            String bodyString = "";
            try {
                ResponseBody responseBody = response.body();
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE);
                Buffer buffer = source.buffer();
                bodyString = buffer.clone().readString(Charset.forName("UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "RESPONSE:" + "\n" + responseLog);
            Log.i(TAG, "Response Body:\n" + bodyString);
        }

        return response;
    }
}
