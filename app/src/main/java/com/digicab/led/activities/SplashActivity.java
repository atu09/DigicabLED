package com.digicab.led.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.digicab.led.Constants;
import com.digicab.led.DatabaseHelper;
import com.digicab.led.R;
import com.digicab.led.callbacks.RegisterCall;
import com.digicab.led.databinding.ActivitySplashBinding;
import com.digicab.led.models.DeviceItem;
import com.digicab.led.models.MediaItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;

    static int PLAY_SERVICES_RESOLUTION_REQUEST = 10001;
    RotateAnimation animation;
    RegisterCall registerCall;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        databaseHelper = new DatabaseHelper(this);

        animation = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        animation.setDuration(2000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        binding.ivSync.setAnimation(animation);

        registerCall = new RegisterCall(this, registerCallback);
    }

    RegisterCall.Callback registerCallback = new RegisterCall.Callback() {
        @Override
        public void onSuccess(List<MediaItem> list, String message) {
            for (MediaItem media : list) {
                MediaItem originalMedia = databaseHelper.getMedia(media.id);
                if (originalMedia == null) {
                    databaseHelper.saveData(media);
                }
            }
            startActivity(SyncActivity.class);
        }

        @Override
        public void onFailure(String message) {
            startActivity(SyncActivity.class);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (checkPlayServices()) {
            DeviceItem device = Constants.getDevice(this);
            updateDevice(device);
        } else {
            animation.cancel();
            binding.tvSync.setText(getString(R.string.googlePlayServiceError));
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.d("play_services", "Unavailable");
            if (availability.isUserResolvableError(resultCode)) {
                Log.d("play_services", "Error");
                availability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            }
            return false;
        } else {
            Log.d("play_services", "Available");
        }
        return true;
    }

    void updateDevice(DeviceItem device) {
        if (device != null) {
            binding.tvSync.setText(getString(R.string.loading));
            registerCall.start(device.ac_no, device.plate_no);
        } else {
            startActivity(ConfigureActivity.class);
        }
    }


    void startActivity(final Class classInstance) {
        animation.cancel();
        binding.tvSync.setText(getString(R.string.beginJourney));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, classInstance));
                finish();
            }
        }, 2000);
    }

}
