package com.thundersoft.myapplication;

import android.os.Build;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class FlutterModuleApplication {

    private static final String CHANNEL = "com.thundersoft.myapplication/channel";

    public static void configureFlutterEngine(FlutterEngine flutterEngine) {
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler((call, result) -> {
                    if (call.method.equals("getPlatformVersion")) {
                        result.success("Android " + Build.VERSION.RELEASE);
                    } else {
                        result.notImplemented();
                    }
                });
    }
}
