package com.apk.datavault;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.apk.datavault.extension.DataExtension;
import com.apk.datavault.offline.OfflineActivity;

import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity {

    private Intent i = new Intent();
    private Handler handler;
    private int dotCount = 0;
    private TextView loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.dark));
        }
        handler = new Handler();
        startDotAnimation();
        loading = findViewById(R.id.loading);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                i.setClass(getApplicationContext(), PermissionActivity.class);
                                startActivity(i);
                            } else {
                                if (DataExtension.isConnected(getApplicationContext())) {
                                    i.setClass(getApplicationContext(), HomeActivity.class);
                                    startActivity(i);
                                } else {
                                    i.setClass(getApplicationContext(), OfflineActivity.class);
                                    startActivity(i);
                                }
                            }
                        } else {
                            if (DataExtension.isConnected(getApplicationContext())) {
                                i.setClass(getApplicationContext(), HomeActivity.class);
                                startActivity(i);
                            } else {
                                i.setClass(getApplicationContext(), OfflineActivity.class);
                                startActivity(i);
                            }
                        }
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, (int) 3000);
    }
    private void startDotAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dotCount = (dotCount + 1) % 4;
                updateText(dotCount);
                handler.postDelayed(this, 500); // Change the duration here to control the animation speed.
            }
        }, 0);
    }
    private void updateText(int dotCount) {
        StringBuilder dotsBuilder = new StringBuilder();
        for (int i = 0; i < dotCount; i++) {
            dotsBuilder.append(".");
        }
//        loading.setText(getString(R.string.please_wait).concat(String.valueOf(dotsBuilder)));
        loading.setText(String.valueOf(dotsBuilder));
    }
}