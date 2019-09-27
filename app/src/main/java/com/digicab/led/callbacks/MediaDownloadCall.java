package com.digicab.led.callbacks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.digicab.led.BuildConfig;

import atirek.pothiwala.connection.Connector;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by Atirek Pothiwala on 5/20/2019.
 */
public class MediaDownloadCall {

    private String TAG = "media_download";
    private Connector connector;
    private Call<ResponseBody> call;
    private Callback callback;

    private CallInterface getRestInterface() {
        return Connector.getClient(BuildConfig.base_url).create(CallInterface.class);
    }

    public MediaDownloadCall(@NonNull Context context, @NonNull Callback callback) {
        this.connector = new Connector(context, BuildConfig.DEBUG);
        this.callback = callback;
    }

    public void start(@NonNull String downloadUrl) {

        call = getRestInterface().downloadMedia(BuildConfig.media_url + downloadUrl);
        connector.setListener(new Connector.ConnectListener() {
            @Override
            public void onSuccess(@NonNull String TAG, @Nullable String json, @NonNull Headers headers) {
                callback.onSuccess(json);
            }

            @Override
            public void onFailure(@NonNull String TAG, boolean isNetworkIssue, @Nullable String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
        connector.Download(TAG, call);
    }

    public void stop() {
        connector.cancelCall(call);
    }

    public interface Callback {
        void onSuccess(String mediaPath);

        void onFailure(String message);
    }
}
