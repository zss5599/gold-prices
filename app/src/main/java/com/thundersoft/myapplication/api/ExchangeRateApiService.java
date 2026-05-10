package com.thundersoft.myapplication.api;

import com.thundersoft.myapplication.model.ExchangeRateResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ExchangeRateApiService {

    @GET("v6/latest/USD")
    Call<ExchangeRateResponse> getUsdExchangeRates();
}
