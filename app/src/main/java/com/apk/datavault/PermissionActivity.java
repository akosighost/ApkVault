package com.apk.datavault;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.apk.datavault.extension.DataExtension;
import com.apk.datavault.offline.OfflineActivity;

public class PermissionActivity extends AppCompatActivity {
    private final Intent intent = new Intent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.dark));
        }
        LinearLayout button1 = findViewById(R.id.button1);
        LinearLayout button2 = findViewById(R.id.button2);
        {
            GradientDrawable SketchUi = new GradientDrawable();
            int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
            SketchUi.setStroke(d*3,0xFF2A2B2F);
            button2.setBackground(SketchUi);
        }
        button1.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
                } else {
                    if (DataExtension.isConnected(getApplicationContext())) {
                        intent.setClass(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    } else {
                        intent.setClass(getApplicationContext(), OfflineActivity.class);
                        startActivity(intent);
                    }
                }
            } else {
                if (DataExtension.isConnected(getApplicationContext())) {
                    intent.setClass(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                } else {
                    intent.setClass(getApplicationContext(), OfflineActivity.class);
                    startActivity(intent);
                }
            }
        });
        button2.setOnClickListener(view -> finishAffinity());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (DataExtension.isConnected(getApplicationContext())) {
                    intent.setClass(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                } else {
                    intent.setClass(getApplicationContext(), OfflineActivity.class);
                    startActivity(intent);
                }
                // Permission granted, you can now perform your actions that require the permissions
            } else {
                // Permission denied, handle this situation (e.g., show a message, disable features, etc.)
            }
        }
    }
}