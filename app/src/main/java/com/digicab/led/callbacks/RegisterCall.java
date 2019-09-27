package com.digicab.led.callbacks;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.digicab.led.BuildConfig;
import com.digicab.led.R;
import com.digicab.led.models.MediaItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.Loader;
import okhttp3.Headers;
import retrofit2.Call;

/**
 * Created by Atirek Pothiwala on 5/20/2019.
 */
public class RegisterCall {

    private String TAG = "register";
    private Context context;
    private Connector connector;
    private Call<String> call;
    private Callback callback;

    private CallInterface getRestInterface() {
        return Connector.getClient(BuildConfig.base_url).create(CallInterface.class);
    }

    public RegisterCall(@NonNull Context context, @NonNull Callback callback) {
        this.context = context;
        this.connector = new Connector(context, BuildConfig.DEBUG);
        this.callback = callback;
    }

    public void start(@NonNull String ac_no, @NonNull String plate_no) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("ac_no", ac_no);
        params.put("device_id", Connector.getMacAddress());
        params.put("plate_no", plate_no);
        call = getRestInterface().device_register(params);

        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(@NonNull String TAG, @Nullable String json, @NonNull Headers headers) {
                try {
                    JSONObject response = new JSONObject(json);
                    if (response.optBoolean("success")) {
                        JSONArray data = response.getJSONArray("data");
                        Type listType = new TypeToken<List<MediaItem>>() {
                        }.getType();
                        List<MediaItem> list = new Gson().fromJson(data.toString(), listType);
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        callback.onSuccess(list, response.optString("message"));
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
        connector.setLoaderDialog(getLoader());
        connector.Request(TAG, call);
    }

    public void stop() {
        connector.cancelCall(call);
    }

    private Dialog getLoader() {
        Loader loader = new Loader(context);
        loader.setColor(R.color.colorAccent);
        loader.setCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                stop();
            }
        });
        return loader.getDialog();
    }

    public interface Callback {
        void onSuccess(List<MediaItem> list, String message);

        void onFailure(String message);
    }
}
