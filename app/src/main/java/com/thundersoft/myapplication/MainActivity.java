package com.thundersoft.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thundersoft.myapplication.api.ExchangeRateApiService;
import com.thundersoft.myapplication.api.MetalPriceApiService;
import com.thundersoft.myapplication.model.ExchangeRateResponse;
import com.thundersoft.myapplication.model.MetalPriceResponse;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String EXCHANGE_RATE_BASE_URL = "https://open.er-api.com/";
    private static final String METAL_PRICE_BASE_URL = "https://api.gold-api.com/";
    private static final long REFRESH_INTERVAL_MS = 30_000;

    private TextView tvUsdToCny;
    private TextView tvUsdToJpy;
    private TextView tvCnyToJpy;
    private TextView tvGoldPrice;
    private TextView tvSilverPrice;
    private TextView tvLastUpdate;
    private TextView tvNextRefresh;
    private Button btnRefresh;
    private ProgressBar progressBar;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private Runnable countdownRunnable;

    private ExchangeRateApiService exchangeRateApi;
    private MetalPriceApiService metalPriceApi;

    private int countdownSeconds = 30;
    private boolean isRefreshing = false;

    private Double usdToCnyRate = null;
    private Double goldPriceUsd = null;
    private Double silverPriceUsd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initApis();
        startAutoRefresh();
    }

    private void initViews() {
        tvUsdToCny = findViewById(R.id.tv_usd_to_cny);
        tvUsdToJpy = findViewById(R.id.tv_usd_to_jpy);
        tvCnyToJpy = findViewById(R.id.tv_cny_to_jpy);
        tvGoldPrice = findViewById(R.id.tv_gold_price);
        tvSilverPrice = findViewById(R.id.tv_silver_price);
        tvLastUpdate = findViewById(R.id.tv_last_update);
        tvNextRefresh = findViewById(R.id.tv_next_refresh);
        btnRefresh = findViewById(R.id.btn_refresh);
        progressBar = findViewById(R.id.progress_bar);

        btnRefresh.setOnClickListener(v -> {
            if (!isRefreshing) {
                refreshData();
            }
        });
    }

    private void initApis() {
        Retrofit exchangeRetrofit = new Retrofit.Builder()
                .baseUrl(EXCHANGE_RATE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Retrofit metalRetrofit = new Retrofit.Builder()
                .baseUrl(METAL_PRICE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        exchangeRateApi = exchangeRetrofit.create(ExchangeRateApiService.class);
        metalPriceApi = metalRetrofit.create(MetalPriceApiService.class);
    }

    private void startAutoRefresh() {
        refreshData();

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshData();
                handler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        handler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                countdownSeconds--;
                if (countdownSeconds <= 0) {
                    countdownSeconds = 30;
                }
                tvNextRefresh.setText("下次刷新: " + countdownSeconds + " 秒");
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(countdownRunnable, 1000);
    }

    private void refreshData() {
        if (isRefreshing) return;
        isRefreshing = true;
        progressBar.setVisibility(View.VISIBLE);
        countdownSeconds = 30;

        AtomicInteger completedCount = new AtomicInteger(0);
        int totalRequests = 3;

        Runnable onComplete = () -> {
            if (completedCount.incrementAndGet() == totalRequests) {
                isRefreshing = false;
                progressBar.setVisibility(View.GONE);
                tvLastUpdate.setText("最后更新: " + java.text.DateFormat.getDateTimeInstance().format(new Date()));
            }
        };

        fetchExchangeRates(onComplete);
        fetchGoldPrice(onComplete);
        fetchSilverPrice(onComplete);
    }

    private void fetchExchangeRates(Runnable onComplete) {
        exchangeRateApi.getUsdExchangeRates().enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExchangeRateResponse data = response.body();
                    DecimalFormat df = new DecimalFormat("#.0000");
                    Double cnyRate = data.getRate("CNY");
                    Double jpyRate = data.getRate("JPY");

                    if (cnyRate != null) {
                        usdToCnyRate = cnyRate;
                        tvUsdToCny.setText("1 USD = " + df.format(cnyRate) + " CNY");
                    }
                    if (jpyRate != null) {
                        tvUsdToJpy.setText("1 USD = " + df.format(jpyRate) + " JPY");
                    }
                    if (cnyRate != null && jpyRate != null) {
                        double cnyToJpy = jpyRate / cnyRate;
                        tvCnyToJpy.setText("1 CNY = " + df.format(cnyToJpy) + " JPY");
                    }
                    updateMetalPricesInCny();
                } else {
                    Toast.makeText(MainActivity.this, "汇率数据获取失败", Toast.LENGTH_SHORT).show();
                }
                onComplete.run();
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "汇率请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void fetchGoldPrice(Runnable onComplete) {
        metalPriceApi.getMetalPrice("XAU").enqueue(new Callback<MetalPriceResponse>() {
            @Override
            public void onResponse(Call<MetalPriceResponse> call, Response<MetalPriceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MetalPriceResponse data = response.body();
                    goldPriceUsd = data.getPrice();
                    updateMetalPricesInCny();
                } else {
                    Toast.makeText(MainActivity.this, "黄金价格获取失败", Toast.LENGTH_SHORT).show();
                }
                onComplete.run();
            }

            @Override
            public void onFailure(Call<MetalPriceResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "黄金请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private void fetchSilverPrice(Runnable onComplete) {
        metalPriceApi.getMetalPrice("XAG").enqueue(new Callback<MetalPriceResponse>() {
            @Override
            public void onResponse(Call<MetalPriceResponse> call, Response<MetalPriceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MetalPriceResponse data = response.body();
                    silverPriceUsd = data.getPrice();
                    updateMetalPricesInCny();
                } else {
                    Toast.makeText(MainActivity.this, "白银价格获取失败", Toast.LENGTH_SHORT).show();
                }
                onComplete.run();
            }

            @Override
            public void onFailure(Call<MetalPriceResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "白银请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                onComplete.run();
            }
        });
    }

    private static final double TROY_OUNCE_TO_GRAM = 31.1034768;

    private void updateMetalPricesInCny() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        if (goldPriceUsd != null) {
            if (usdToCnyRate != null) {
                double pricePerGram = goldPriceUsd * usdToCnyRate / TROY_OUNCE_TO_GRAM;
                tvGoldPrice.setText(df.format(pricePerGram) + " CNY/克");
            } else {
                double pricePerGram = goldPriceUsd / TROY_OUNCE_TO_GRAM;
                tvGoldPrice.setText(df.format(pricePerGram) + " USD/克");
            }
        }
        if (silverPriceUsd != null) {
            if (usdToCnyRate != null) {
                double pricePerGram = silverPriceUsd * usdToCnyRate / TROY_OUNCE_TO_GRAM;
                tvSilverPrice.setText(df.format(pricePerGram) + " CNY/克");
            } else {
                double pricePerGram = silverPriceUsd / TROY_OUNCE_TO_GRAM;
                tvSilverPrice.setText(df.format(pricePerGram) + " USD/克");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshRunnable);
        handler.removeCallbacks(countdownRunnable);
    }
}