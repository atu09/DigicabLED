package com.digicab.led.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.digicab.led.DatabaseHelper;
import com.digicab.led.R;
import com.digicab.led.callbacks.MediaDownloadCall;
import com.digicab.led.databinding.ActivitySyncBinding;
import com.digicab.led.models.MediaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SyncActivity extends AppCompatActivity {

    RotateAnimation animation;
    ActivitySyncBinding binding;
    List<MediaItem> mediaList = new ArrayList<>();

    MediaDownloadCall downloadCall;
    DatabaseHelper databaseHelper;

    int seconds = 6;
    Handler handler;
    boolean isRetry = false;
    Runnable retryRunner = new Runnable() {
        @Override
        public void run() {
            seconds--;
            if (seconds < 0) {
                binding.btnAction.callOnClick();
            } else {
                isRetry = true;
                binding.btnAction.setText(String.format(Locale.getDefault(), getString(R.string.retry), seconds));
                handler.postDelayed(retryRunner, 1000);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (isRetry) {
            handler.post(retryRunner);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(retryRunner);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sync);
        databaseHelper = new DatabaseHelper(this);
        downloadCall = new MediaDownloadCall(this, downloadCallback);
        handler = new Handler();

        animation = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        animation.setDuration(2000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        binding.ivSync.setAnimation(animation);

        binding.btnAction.setVisibility(View.GONE);
        binding.btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                handler.removeCallbacks(retryRunner);
                isRetry = false;
                syncListener.nextMedia();
            }
        });
        syncListener.initSync();
    }

    @Override
    public void onBackPressed() {

    }

    SyncListener syncListener = new SyncListener() {

        MediaItem media;
        int syncPosition = -1;

        @Override
        public void initSync() {
            mediaList = new ArrayList<>(databaseHelper.getOnlineMediaList());
            syncListener.nextMedia();
        }

        @Override
        public void nextMedia() {
            if (binding.btnAction.getVisibility() == View.VISIBLE) {
                binding.btnAction.setVisibility(View.GONE);
            }

            if (!animation.hasStarted() || animation.hasEnded()) {
                animation.start();
            }

            syncPosition = syncPosition + 1;

            if (syncPosition < mediaList.size()) {
                binding.tvSync.setText(getString(R.string.syncing));
                media = mediaList.get(syncPosition);
                downloadCall.start(media.upload);
            } else {
                if (animation.hasStarted() || !animation.hasEnded()) {
                    animation.cancel();
                }
                binding.tvSync.setText(getString(R.string.syncCompleted));

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SyncActivity.this, VideoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }
        }

        @Override
        public void saveMedia(String path) {
            Log.d("url>>", path);
            media.download = path;
            databaseHelper.updateData(media);
            syncListener.nextMedia();
        }

        @Override
        public void error(String message) {
            animation.cancel();

            binding.tvSync.setText(message);
            binding.btnAction.setVisibility(View.VISIBLE);

            seconds = 6;
            handler.post(retryRunner);
        }
    };

    interface SyncListener {
        void initSync();

        void nextMedia();

        void saveMedia(String path);

        void error(String message);
    }

    MediaDownloadCall.Callback downloadCallback = new MediaDownloadCall.Callback() {
        @Override
        public void onSuccess(String mediaPath) {
            syncListener.saveMedia(mediaPath);
        }

        @Override
        public void onFailure(String message) {
            syncListener.error(message);
        }
    };
}
