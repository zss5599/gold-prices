package com.thundersoft.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ExchangeRateResponse {

    @SerializedName("result")
    private String result;

    @SerializedName("base_code")
    private String baseCode;

    @SerializedName("time_last_update_utc")
    private String timeLastUpdateUtc;

    @SerializedName("rates")
    private Map<String, Double> rates;

    public String getResult() {
        return result;
    }

    public String getBaseCode() {
        return baseCode;
    }

    public String getTimeLastUpdateUtc() {
        return timeLastUpdateUtc;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public Double getRate(String currencyCode) {
        if (rates == null) return null;
        return rates.get(currencyCode);
    }
}
