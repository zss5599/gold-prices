package com.thundersoft.myapplication.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class FinanceWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "com.thundersoft.myapplication.ACTION_REFRESH_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        enqueueOneTimeWork(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(
                FinanceWidgetWorker.class,
                15,
                TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "finance_widget_update",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
        );
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        WorkManager.getInstance(context).cancelUniqueWork("finance_widget_update");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_REFRESH.equals(intent.getAction())) {
            enqueueOneTimeWork(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    private void enqueueOneTimeWork(Context context) {
        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(FinanceWidgetWorker.class).build();
        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
