package com.nikosdouvlis.navigationbar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;

import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@NativePlugin()
public class NavigationBar extends Plugin {
    private String TAG = "capacitor-navigationbar";

    @PluginMethod()
    public void setBackgroundColor(PluginCall call) {
        String color = call.getString("color");
        if (color == null || color.equals("") || !color.contains("#")) {
            call.error("Color cannot be empty or null. It should be a hex value, eg: #d1009d");
        }

        int parsedColor = Color.parseColor(color);
        final Window window = ((Activity) getContext()).getWindow();
        final boolean isLight = ColorUtils.calculateLuminance(parsedColor) > 0.5;
        final boolean isUltaLight = ColorUtils.calculateLuminance(parsedColor) > 0.9;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                applyUIElementColor(window, isUltaLight);
            } catch (Exception e) {
                call.error(e.toString());
            }
        } else {
            if (isLight) {
                // Make light color a bit darker for old devices that don't feature
                // light UI elements
                parsedColor = ColorUtils.blendARGB(parsedColor, Color.BLACK, 0.17f);
            }
        }

        final int finalColor = parsedColor;

        try {
            // only update views on ui thread
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    window.setNavigationBarColor(finalColor);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "setBackgroundColor: " + e.toString());
            call.error(e.toString());
        }

        call.success();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void applyUIElementColor(final Window window, final boolean isLight) {
        getActivity().runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                View decorView = window.getDecorView();
                int visibilityFlags = decorView.getSystemUiVisibility();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                if (isLight) {
                    decorView.setSystemUiVisibility(visibilityFlags | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                } else {
                    decorView.setSystemUiVisibility(visibilityFlags & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                }
            }
        });
    }

}

