package com.digicab.led.callbacks;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.digicab.led.BuildConfig;
import com.digicab.led.Constants;
import com.digicab.led.models.DeviceItem;
import com.digicab.led.models.MediaItem;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

import atirek.pothiwala.connection.Connector;
import okhttp3.Headers;
import retrofit2.Call;

/**
 * Created by Atirek Pothiwala on 5/20/2019.
 */
public class ViewAdvertiseCall {

    private String TAG = "advertise_view";
    private Context context;
    private Connector connector;
    private Call<String> call;
    private Callback callback;

    private CallInterface getRestInterface() {
        return Connector.getClient(BuildConfig.base_url).create(CallInterface.class);
    }

    public ViewAdvertiseCall(@NonNull Context context, @NonNull Callback callback) {
        this.context = context;
        this.connector = new Connector(context, BuildConfig.DEBUG);
        this.callback = callback;
    }

    public void start(int view_id, int advertise_id, int screen_no, @NonNull Location location) {

        DeviceItem device = Constants.getDevice(context);
        if (device == null) {
            callback.onFailure(null);
            return;
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("ac_no", device.ac_no);
        params.put("device_id", device.device_id);
        params.put("advertise_id", advertise_id);
        params.put("screen_no", screen_no);
        params.put("latitude", location.getLatitude());
        params.put("longitude", location.getLongitude());
        params.put("advertise_view_id", view_id);

        call = getRestInterface().advertise_view(params);

        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(@NonNull String TAG, @Nullable String json, @NonNull Headers headers) {
                try {
                    JSONObject response = new JSONObject(json);
                    if (response.optBoolean("success") && response.has("data")) {

                        String message = response.optString("message");
                        JSONObject data = response.optJSONObject("data");

                        MediaItem media = new Gson().fromJson(data.getJSONObject("advertise_detail").toString(), MediaItem.class);
                        int viewId = data.optInt("view_id", 0);

                        callback.onSuccess(viewId, media, message);

                    } else {
                        callback.onFailure(response.optString("message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(TAG, false, e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull String TAG, boolean isNetworkIssue, @Nullable String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
        connector.Request(TAG, call);
    }

    public void stop() {
        connector.cancelCall(call);
    }

    public interface Callback {
        void onSuccess(int viewId, MediaItem media, String message);

        void onFailure(String message);
    }
}
