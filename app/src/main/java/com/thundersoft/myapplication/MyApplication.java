package com.thundersoft.myapplication;

import android.app.Application;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;

public class MyApplication extends Application {

    public static final String FLUTTER_ENGINE_ID = "my_engine";

    @Override
    public void onCreate() {
        super.onCreate();
        // App 启动时后台预热 Engine，避免首次打开 Flutter 页面时的冷启动延迟
        FlutterEngine flutterEngine = new FlutterEngine(this);
        flutterEngine.getDartExecutor().executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        );
        FlutterEngineCache.getInstance().put(FLUTTER_ENGINE_ID, flutterEngine);
    }
}
