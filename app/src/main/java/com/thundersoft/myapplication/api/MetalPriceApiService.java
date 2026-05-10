package com.thundersoft.myapplication.api;

import com.thundersoft.myapplication.model.MetalPriceResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MetalPriceApiService {

    @GET("price/{symbol}")
    Call<MetalPriceResponse> getMetalPrice(@Path("symbol") String symbol);
}
