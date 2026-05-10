package com.thundersoft.myapplication.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.thundersoft.myapplication.R;
import com.thundersoft.myapplication.api.ExchangeRateApiService;
import com.thundersoft.myapplication.api.MetalPriceApiService;
import com.thundersoft.myapplication.model.ExchangeRateResponse;
import com.thundersoft.myapplication.model.MetalPriceResponse;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FinanceWidgetWorker extends Worker {

    private static final String EXCHANGE_RATE_BASE_URL = "https://open.er-api.com/";
    private static final String METAL_PRICE_BASE_URL = "https://api.gold-api.com/";
    private static final double TROY_OUNCE_TO_GRAM = 31.1034768;

    public FinanceWidgetWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        try {
            Retrofit exchangeRetrofit = new Retrofit.Builder()
                    .baseUrl(EXCHANGE_RATE_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Retrofit metalRetrofit = new Retrofit.Builder()
                    .baseUrl(METAL_PRICE_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ExchangeRateApiService exchangeRateApi = exchangeRetrofit.create(ExchangeRateApiService.class);
            MetalPriceApiService metalPriceApi = metalRetrofit.create(MetalPriceApiService.class);

            DecimalFormat dfRate = new DecimalFormat("#.00");
            DecimalFormat dfMetal = new DecimalFormat("#,##0.00");

            Double usdToCny = null;
            Double usdToJpy = null;
            ExchangeRateResponse rateResponse = exchangeRateApi.getUsdExchangeRates().execute().body();
            if (rateResponse != null && rateResponse.getRates() != null) {
                usdToCny = rateResponse.getRates().get("CNY");
                usdToJpy = rateResponse.getRates().get("JPY");
            }

            Double goldPriceCny = null;
            MetalPriceResponse goldResponse = metalPriceApi.getMetalPrice("XAU").execute().body();
            if (goldResponse != null && usdToCny != null) {
                goldPriceCny = goldResponse.getPrice() * usdToCny / TROY_OUNCE_TO_GRAM;
            }

            Double silverPriceCny = null;
            MetalPriceResponse silverResponse = metalPriceApi.getMetalPrice("XAG").execute().body();
            if (silverResponse != null && usdToCny != null) {
                silverPriceCny = silverResponse.getPrice() * usdToCny / TROY_OUNCE_TO_GRAM;
            }

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_finance);

            String usdCnyText = usdToCny != null ? "USD/CNY " + dfRate.format(usdToCny) : "USD/CNY --";
            String usdJpyText = usdToJpy != null ? "USD/JPY " + dfRate.format(usdToJpy) : "USD/JPY --";
            String cnyJpyText = (usdToCny != null && usdToJpy != null)
                    ? "CNY/JPY " + dfRate.format(usdToJpy / usdToCny) : "CNY/JPY --";
            String goldText = goldPriceCny != null ? "黄金 " + dfMetal.format(goldPriceCny) + " CNY/g" : "黄金 --";
            String silverText = silverPriceCny != null ? "白银 " + dfMetal.format(silverPriceCny) + " CNY/g" : "白银 --";
            String timeText = "更新: " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            views.setTextViewText(R.id.widget_usd_cny, usdCnyText);
            views.setTextViewText(R.id.widget_usd_jpy, usdJpyText);
            views.setTextViewText(R.id.widget_cny_jpy, cnyJpyText);
            views.setTextViewText(R.id.widget_gold, goldText);
            views.setTextViewText(R.id.widget_silver, silverText);
            views.setTextViewText(R.id.widget_update_time, timeText);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, FinanceWidgetProvider.class);
            appWidgetManager.updateAppWidget(componentName, views);

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
