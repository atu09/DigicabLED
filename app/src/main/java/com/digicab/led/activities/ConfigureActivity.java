package com.digicab.led.activities;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.digicab.led.BuildConfig;
import com.digicab.led.Constants;
import com.digicab.led.R;
import com.digicab.led.callbacks.RegisterCall;
import com.digicab.led.DatabaseHelper;
import com.digicab.led.databinding.ActivityConfigureBinding;
import com.digicab.led.models.DeviceItem;
import com.digicab.led.models.MediaItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import atirek.pothiwala.connection.Connector;
import atirek.pothiwala.utility.helper.IntentHelper;
import atirek.pothiwala.utility.helper.PermissionHelper;

public class ConfigureActivity extends AppCompatActivity {

    ActivityConfigureBinding binding;
    DatabaseHelper databaseHelper;
    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    RegisterCall registerCall;

    RegisterCall.Callback registerCallback = new RegisterCall.Callback() {
        @Override
        public void onSuccess(List<MediaItem> list, String message) {

            String accountNumber = binding.etAccountNumber.getText().toString().trim();
            String taxiNumber = binding.etTaxiNumber.getText().toString().trim();
            String macNumber = Connector.getMacAddress();

            DeviceItem device = new DeviceItem(accountNumber, macNumber, taxiNumber);
            Constants.setDevice(ConfigureActivity.this, device);

/*
            for (MediaItem media : list){
                databaseHelper.saveData(media);
            }
*/
            databaseHelper.saveList(list);
            startActivity(new Intent(ConfigureActivity.this, SyncActivity.class));
        }

        @Override
        public void onFailure(String message) {
            Toast.makeText(ConfigureActivity.this, message, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_configure);
        databaseHelper = new DatabaseHelper(this);
        registerCall = new RegisterCall(this, registerCallback);

/*
        binding.etAccountNumber.setText("1310");
        binding.etTaxiNumber.setText("asdsad");
*/

        binding.btnConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentHelper.closeKeyboard(ConfigureActivity.this, null);

                if (!PermissionHelper.checkPermissions(ConfigureActivity.this, permissions)) {
                    PermissionHelper.requestPermissions(ConfigureActivity.this, permissions, 100);
                } else {
                    String accountNumber = binding.etAccountNumber.getText().toString();
                    String taxiNumber = binding.etTaxiNumber.getText().toString();
                    if (!accountNumber.trim().isEmpty() && !taxiNumber.trim().isEmpty()) {
                        registerCall.start(accountNumber.trim(), taxiNumber.trim());
                    } else {
                        Toast.makeText(ConfigureActivity.this, getString(R.string.cannotProceed), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        binding.etTaxiNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    binding.btnConfigure.callOnClick();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0) {
            binding.btnConfigure.callOnClick();
        } else {
            Toast.makeText(this, getString(R.string.permissionIsRequired), Toast.LENGTH_LONG).show();
        }
    }

    public boolean isExpireDialog() {
        if (isExpired("20-07-2019") && BuildConfig.DEBUG) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.app_name));
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage("Your debug app version is expired, please contact developer for support.");
            builder.setCancelable(false);
            builder.show();
            return true;
        }
        return false;
    }

    boolean isExpired(String date) {

        try {

            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date targetDate = format.parse(date);

            Calendar targetCalendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
            targetCalendar.setTime(targetDate);
            targetCalendar.set(Calendar.HOUR_OF_DAY, 0);
            targetCalendar.set(Calendar.MINUTE, 0);
            targetCalendar.set(Calendar.SECOND, 0);

            Calendar currentCalendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
            currentCalendar.setTime(new Date());
            currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
            currentCalendar.set(Calendar.MINUTE, 0);
            currentCalendar.set(Calendar.SECOND, 0);

            return currentCalendar.after(targetCalendar);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return true;

    }

}
