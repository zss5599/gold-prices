package com.thundersoft.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class MetalPriceResponse {

    @SerializedName("symbol")
    private String symbol;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private double price;

    @SerializedName("currency")
    private String currency;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
