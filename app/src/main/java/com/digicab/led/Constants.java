package com.digicab.led;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Size;

import com.digicab.led.models.DeviceItem;
import com.google.gson.Gson;

public class Constants {

    public interface PREFERENCES {
        String DEVICE = "devicePrefs";
        String SCREEN = "screenPrefs";
    }


    public static void setDevice(Context context, DeviceItem deviceItem) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES.DEVICE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("device", new Gson().toJson(deviceItem));
        editor.apply();
    }

    public static DeviceItem getDevice(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES.DEVICE, Context.MODE_PRIVATE);
        if (sharedPreferences.contains("device")) {
            return new Gson().fromJson(sharedPreferences.getString("device", ""), DeviceItem.class);
        }
        return null;
    }


    public static void clearData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES.DEVICE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }




    public static void setScreenSize(Context context, int height, int width) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES.SCREEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("screen_height", height);
        editor.putInt("screen_width", width);
        editor.apply();
    }

    public static Size getScreenSize(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES.SCREEN, Context.MODE_PRIVATE);
        return new Size(sharedPreferences.getInt("screen_width", 96), sharedPreferences.getInt("screen_height", 128));
    }

    public static void setScreenPoint(Context context, int x, int y) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES.SCREEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("screen_x", x);
        editor.putInt("screen_y", y);
        editor.apply();
    }

    public static Point getScreenPoint(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES.SCREEN, Context.MODE_PRIVATE);
        Point point = new Point();
        point.x = sharedPreferences.getInt("screen_x", 0);
        point.y = sharedPreferences.getInt("screen_y", 0);
        return point;
    }
}
