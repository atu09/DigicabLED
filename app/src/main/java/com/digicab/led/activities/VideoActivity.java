package com.digicab.led.activities;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;

import com.digicab.led.BuildConfig;
import com.digicab.led.Constants;
import com.digicab.led.DatabaseHelper;
import com.digicab.led.R;
import com.digicab.led.callbacks.ViewAdvertiseCall;
import com.digicab.led.databinding.ActivityVideoBinding;
import com.digicab.led.models.MediaItem;

import java.util.List;
import java.util.Locale;

import atirek.pothiwala.utility.helper.FusedLocationHelper;

public class VideoActivity extends AppCompatActivity implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, FusedLocationHelper.LocationListener {

    ActivityVideoBinding binding;

    Location currentLocation = new Location("Current");

    DatabaseHelper databaseHelper;
    FusedLocationHelper locationHelper;

    int lastViewId1 = 0;
    int lastViewId2 = 0;
    int lastMedia1 = 0;
    int lastMedia2 = 0;

    int lastAdvertiseType1 = 1;
    int lastAdvertiseType2 = 1;

    int apiDuration1 = 0;
    int apiDuration2 = 0;
    int currentDuration1 = 0;
    int currentDuration2 = 0;

    int DEFAULT_DURATION = 20000;

    Handler handler;
    Runnable imageRunner1 = new Runnable() {
        @Override
        public void run() {
            nextAdvertisement1(lastMedia1);
        }
    };
    Runnable imageRunner2 = new Runnable() {
        @Override
        public void run() {
            nextAdvertisement2(lastMedia2);
        }
    };

    Runnable apiCaller1 = new Runnable() {
        @Override
        public void run() {
            viewAdvertiseCall1.start(lastViewId1, lastMedia1, 1, currentLocation);
        }
    };
    Runnable apiCaller2 = new Runnable() {
        @Override
        public void run() {
            viewAdvertiseCall2.start(lastViewId2, lastMedia2, 2, currentLocation);
        }
    };

    ViewAdvertiseCall viewAdvertiseCall1, viewAdvertiseCall2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);


        viewAdvertiseCall1 = new ViewAdvertiseCall(this, advertiseCallback1);
        viewAdvertiseCall2 = new ViewAdvertiseCall(this, advertiseCallback2);

        databaseHelper = new DatabaseHelper(this);
        locationHelper = new FusedLocationHelper(this, "currentLocation", BuildConfig.DEBUG);
        locationHelper.setDisplacement(0);
        locationHelper.setTimeInterval(1000);
        locationHelper.initializeLocationProviders();
        locationHelper.setListener(this);

        binding.ivSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSizeDialog();
            }
        });
        initVideoPlayers();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Size size = Constants.getScreenSize(this);
        Point point = Constants.getScreenPoint(this);
        changeSize(size.getHeight(), size.getWidth(), point.x, point.y);
    }

    void initVideoPlayers() {

        handler = new Handler();
        binding.videoView.requestFocus();
        binding.videoView.setOnPreparedListener(this);
        binding.videoView.setOnCompletionListener(this);
        binding.imageView.setImageResource(R.drawable.default_advertise);


        binding.videoView2.requestFocus();
        binding.videoView2.setOnPreparedListener(this);
        binding.videoView2.setOnCompletionListener(this);
        binding.imageView2.setImageResource(R.drawable.default_advertise);

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setVolume(0, 0);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer.getAudioSessionId() == binding.videoView.getAudioSessionId()) {
            nextAdvertisement1(lastMedia1);
        } else {
            nextAdvertisement2(lastMedia2);
        }
    }

    void nextAdvertisement1(int media_id) {

        MediaItem media1 = databaseHelper.getMedia(media_id);

        if (media1 != null) {

            Uri fileUri = Uri.parse(media1.download);
            lastAdvertiseType1 = media1.ad_type;

            if (media1.ad_type != 1) {
                binding.videoView.setVisibility(View.VISIBLE);
                binding.imageView.setVisibility(View.GONE);
                binding.videoView.setVideoURI(fileUri);
                binding.videoView.start();
                apiCaller(binding.videoView.getDuration(), 1);
            } else {
                Log.d("url1>>1", media1.download);
                binding.videoView.setVisibility(View.GONE);
                binding.imageView.setVisibility(View.VISIBLE);
                binding.imageView.setImageURI(fileUri);
                handler.postDelayed(imageRunner1, DEFAULT_DURATION);
                apiCaller(DEFAULT_DURATION, 1);
            }

        } else {

            lastViewId1 = 0;
            lastMedia1 = 0;
            lastAdvertiseType1 = 1;
            binding.videoView.setVisibility(View.GONE);
            binding.imageView.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.default_advertise);
            handler.postDelayed(imageRunner1, DEFAULT_DURATION);
            apiCaller(DEFAULT_DURATION, 1);
        }
    }

    void nextAdvertisement2(int media_id) {

        MediaItem media2 = databaseHelper.getMedia(media_id);

        if (media2 != null) {

            Uri fileUri = Uri.parse(media2.download);
            lastAdvertiseType2 = media2.ad_type;

            if (media2.ad_type != 1) {
                binding.videoView2.setVisibility(View.VISIBLE);
                binding.imageView2.setVisibility(View.GONE);
                binding.videoView2.setVideoURI(fileUri);
                binding.videoView2.start();
                apiCaller(binding.videoView2.getDuration(), 2);
            } else {
                Log.d("url2>>", media2.download);
                binding.videoView2.setVisibility(View.GONE);
                binding.imageView2.setVisibility(View.VISIBLE);
                binding.imageView2.setImageURI(fileUri);
                handler.postDelayed(imageRunner2, DEFAULT_DURATION);
                apiCaller(DEFAULT_DURATION, 2);
            }

        } else {

            lastViewId2 = 0;
            lastMedia2 = 0;
            lastAdvertiseType2 = 1;
            binding.videoView2.setVisibility(View.GONE);
            binding.imageView2.setVisibility(View.VISIBLE);
            binding.imageView2.setImageResource(R.drawable.default_advertise);
            handler.postDelayed(imageRunner2, DEFAULT_DURATION);
            apiCaller(DEFAULT_DURATION, 2);
        }
    }

    void apiCaller(int duration, int screen) {
        int apiDuration = duration - duration / 3;
        if (screen == 1) {
            apiDuration1 = apiDuration;
            handler.postDelayed(apiCaller1, apiDuration);
        } else {
            apiDuration2 = apiDuration;
            handler.postDelayed(apiCaller2, apiDuration);
        }
    }

    void mediaService(int status) {
        if (status == 0) {
            if (!binding.videoView.isPlaying() && lastAdvertiseType1 != 1) {
                binding.videoView.seekTo(currentDuration1);
                binding.videoView.start();
                apiCaller(binding.videoView.getDuration() - currentDuration1, 1);
            } else {
                handler.postDelayed(imageRunner1, DEFAULT_DURATION);
                apiCaller(DEFAULT_DURATION, 1);
            }

            if (!binding.videoView2.isPlaying() && lastAdvertiseType2 != 1) {
                binding.videoView2.seekTo(currentDuration2);
                binding.videoView2.start();
                apiCaller(binding.videoView2.getDuration() - currentDuration2, 2);
            } else {
                handler.postDelayed(imageRunner2, DEFAULT_DURATION);
                apiCaller(DEFAULT_DURATION, 2);
            }

        } else if (status == 1) {
            if (binding.videoView.canPause() && lastAdvertiseType1 != 1) {
                currentDuration1 = binding.videoView.getCurrentPosition();
                binding.videoView.pause();
            } else {
                handler.removeCallbacks(imageRunner1);
            }
            handler.removeCallbacks(apiCaller1);

            if (binding.videoView2.canPause() && lastAdvertiseType2 != 1) {
                currentDuration2 = binding.videoView2.getCurrentPosition();
                binding.videoView2.pause();
            } else {
                handler.removeCallbacks(imageRunner2);
            }
            handler.removeCallbacks(apiCaller2);

        } else {
            binding.videoView.stopPlayback();
            binding.videoView2.stopPlayback();
        }

    }

    ViewAdvertiseCall.Callback advertiseCallback1 = new ViewAdvertiseCall.Callback() {
        @Override
        public void onSuccess(int viewId, MediaItem media, String message) {
            lastViewId1 = viewId;
            lastMedia1 = media.id;
            Log.d("success>>", message);
        }

        @Override
        public void onFailure(String message) {
            lastViewId1 = 0;
            lastMedia1 = 0;
            Log.d("failure>>", message);
        }
    };

    ViewAdvertiseCall.Callback advertiseCallback2 = new ViewAdvertiseCall.Callback() {
        @Override
        public void onSuccess(int viewId, MediaItem media, String message) {
            lastViewId2 = viewId;
            lastMedia2 = media.id;
            Log.d("success>>", message);
        }

        @Override
        public void onFailure(String message) {
            lastViewId2 = 0;
            lastMedia2 = 0;
            Log.d("failure>>", message);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mediaService(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaService(1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaService(2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        viewAdvertiseCall1.stop();
        viewAdvertiseCall2.stop();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    public void showSizeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(getString(R.string.managePreview));
        builder.setCancelable(true);

        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setPadding(50, 20, 50, 20);

        LinearLayout horizontalLayout1 = new LinearLayout(this);
        horizontalLayout1.setOrientation(LinearLayout.HORIZONTAL);
        final EditText etWidth = new EditText(this);
        etWidth.setHint(getString(R.string.screenWidth));
        etWidth.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        etWidth.setInputType(InputType.TYPE_CLASS_NUMBER);
        etWidth.setBackgroundResource(R.drawable.selector_edit_text);
        etWidth.setTextSize(18);
        etWidth.setTextColor(getResources().getColor(R.color.colorAccent));
        etWidth.setHintTextColor(getResources().getColor(R.color.colorBlackTranslucent));
        etWidth.setPadding(20, 20, 20, 20);
        horizontalLayout1.addView(etWidth);

        Space space1 = new Space(this);
        space1.setMinimumWidth(20);
        space1.setMinimumHeight(0);
        horizontalLayout1.addView(space1);

        final EditText etHeight = new EditText(this);
        etHeight.setHint(getString(R.string.screenHeight));
        etHeight.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        etHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
        etHeight.setBackgroundResource(R.drawable.selector_edit_text);
        etHeight.setTextSize(18);
        etWidth.setTextColor(getResources().getColor(R.color.colorAccent));
        etWidth.setHintTextColor(getResources().getColor(R.color.colorBlackTranslucent));
        etHeight.setPadding(20, 20, 20, 20);
        horizontalLayout1.addView(etHeight);

        LinearLayout horizontalLayout2 = new LinearLayout(this);
        horizontalLayout2.setOrientation(LinearLayout.HORIZONTAL);
        final EditText etX = new EditText(this);
        etX.setHint(getString(R.string.xPosition));
        etX.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        etX.setInputType(InputType.TYPE_CLASS_NUMBER);
        etX.setBackgroundResource(R.drawable.selector_edit_text);
        etX.setTextSize(18);
        etX.setTextColor(getResources().getColor(R.color.colorAccent));
        etX.setHintTextColor(getResources().getColor(R.color.colorBlackTranslucent));
        etX.setPadding(20, 20, 20, 20);
        horizontalLayout2.addView(etX);

        Space space2 = new Space(this);
        space2.setMinimumWidth(20);
        space2.setMinimumHeight(0);
        horizontalLayout2.addView(space2);

        final EditText etY = new EditText(this);
        etY.setHint(getString(R.string.yPosition));
        etY.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etY.setInputType(InputType.TYPE_CLASS_NUMBER);
        etY.setBackgroundResource(R.drawable.selector_edit_text);
        etY.setTextSize(18);
        etX.setTextColor(getResources().getColor(R.color.colorAccent));
        etX.setHintTextColor(getResources().getColor(R.color.colorBlackTranslucent));
        etY.setPadding(20, 20, 20, 20);
        horizontalLayout2.addView(etY);

        verticalLayout.addView(horizontalLayout1);

        Space space = new Space(this);
        space.setMinimumWidth(0);
        space.setMinimumHeight(20);
        verticalLayout.addView(space);

        verticalLayout.addView(horizontalLayout2);

        builder.setView(verticalLayout);

        int height = binding.layoutVideo.getHeight();
        int width = binding.layoutVideo.getWidth();
        int x = Math.round(binding.layoutVideo.getX());
        int y = Math.round(binding.layoutVideo.getY());

        etHeight.setText(String.valueOf(height));
        etWidth.setText(String.valueOf(width));
        etX.setText(String.valueOf(x));
        etY.setText(String.valueOf(y));

        builder.setPositiveButton(getString(R.string.configure), null);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setTextColor(getResources().getColor(R.color.colorAccent));
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int height = convertToInt(etHeight.getText().toString());
                        int width = convertToInt(etWidth.getText().toString());
                        int x = convertToInt(etX.getText().toString());
                        int y = convertToInt(etY.getText().toString());
                        changeSize(height, width, x, y);
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }

    void changeSize(final int height, final int width, final int x, final int y) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = binding.layoutVideo.getLayoutParams();
                params.height = height;
                params.width = width;
                binding.layoutVideo.setLayoutParams(params);
                binding.layoutVideo.setX(x);
                binding.layoutVideo.setY(y);

                Constants.setScreenSize(VideoActivity.this, height, width);
                Constants.setScreenPoint(VideoActivity.this, x, y);
            }
        });
    }

    int convertToInt(String value) {
        try {
            return Integer.valueOf(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private Address getArea(Location position) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(position.getLatitude(), position.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onLocationReceived(@NonNull Location location) {
        this.currentLocation.setLatitude(location.getLatitude());
        this.currentLocation.setLongitude(location.getLongitude());
        binding.tvLocation.setText(String.format(Locale.getDefault(), "Location: %f, %f", location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onLocationAvailability(boolean isAvailable) {
        if (!isAvailable) {
            this.currentLocation.setLongitude(0);
            this.currentLocation.setLongitude(0);

            binding.tvLocation.setText("Location Unavailable");
        }
    }
}
