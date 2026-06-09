package com.thundersoft.myapplication;

import android.os.Bundle;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.plugin.common.MethodChannel;

public class CustomFlutterActivity extends FlutterActivity {

    private static final String EXTRA_ROUTE = "route";
    private static final String NAV_CHANNEL = "com.thundersoft.myapplication/navigation";

    @Override
    public String getCachedEngineId() {
        return MyApplication.FLUTTER_ENGINE_ID;
    }

    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        FlutterModuleApplication.configureFlutterEngine(flutterEngine);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 通过 MethodChannel 通知 Flutter 切换路由
        String route = getIntent().getStringExtra(EXTRA_ROUTE);
        if (route != null) {
            sendRouteToFlutter(route);
        }
    }

    private void sendRouteToFlutter(String route) {
        FlutterEngine engine = FlutterEngineCache.getInstance().get(MyApplication.FLUTTER_ENGINE_ID);
        if (engine != null) {
            new MethodChannel(engine.getDartExecutor().getBinaryMessenger(), NAV_CHANNEL)
                    .invokeMethod("setRoute", route);
        }
    }
}
