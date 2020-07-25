package com.araditc.chat.core.Repository.Network.ApiService;

import com.araditc.chat.core.Repository.Network.RequestBody.RequestTokenSubmiter;
import com.araditc.chat.core.Repository.Network.Response.ResponseTokenSubmiter;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TokenSubmiterService {

    @POST("")
    Call<ResponseTokenSubmiter> submitToken(@Body RequestTokenSubmiter request);
}
