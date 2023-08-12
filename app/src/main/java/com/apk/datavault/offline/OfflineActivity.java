package com.apk.datavault.offline;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.apk.datavault.R;
import com.apk.datavault.extension.DataExtension;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OfflineActivity extends AppCompatActivity {

    private ListView listView;
    private ImageView back;
    private List<ApkFileData> fileData = new ArrayList<>();

    @Override
    public void onBackPressed() {
        if (DataExtension.isConnected(getApplicationContext())) {
            finish();
        } else if (DataExtension.isConnected(getApplicationContext())) {
            finishAffinity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.dark));
        }
        listView = findViewById(R.id.listview);
        back = findViewById(R.id.back);

        back.setOnClickListener(view -> {
            if (DataExtension.isConnected(getApplicationContext())) {
                finish();
            } else if (DataExtension.isConnected(getApplicationContext())) {
                finishAffinity();
            }
        });
//        OverScrollDecoratorHelper.setUpOverScroll(listView);
        listView.setHorizontalScrollBarEnabled(false);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        // Specify the directory path where your APKs are stored
        String directoryPath = DataExtension.defaultApkDirectory();

        List<File> apkFiles = new ArrayList<>();
        File directory = new File(directoryPath);

        // Make sure the directory exists and is a directory
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".apk") || file.getName().toLowerCase().endsWith(".txt")) {
                        apkFiles.add(file);
                    }
                }
            }
        }
        ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, R.layout.home_list, R.id.textview2, apkFiles) {
            @Override
            public View getView(int position, View view, @NonNull ViewGroup parent) {
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.home_list, parent, false);
                }
                final TextView textview1 = view.findViewById(R.id.textview1);
                final TextView fileName = view.findViewById(R.id.textview2);
                final TextView filesize = view.findViewById(R.id.textview3);
                final TextView textview4 = view.findViewById(R.id.textview4);
                final LinearLayout linear1 = view.findViewById(R.id.linear1);
                final LinearLayout linear2 = view.findViewById(R.id.linear2);
                textview4.setVisibility(View.GONE);
                final ImageView install = view.findViewById(R.id.install);
                final ImageView delete = view.findViewById(R.id.delete);
                {
                    {
                        Animation animation;
                        animation = new AnimationUtils().loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                        animation.setDuration(700);
                        linear1.startAnimation(animation);
                        animation = null;
                    }
                    {
                        gradientDrawable(linear1, 0, 0, 0, "#FF202226", "#000000", false);
                        gradientDrawable(linear2, 0, 2, 20, "#FF202226", "#FF2A2B2F", false);
                    }
                }

                File apkFile = getItem(position);
                if (apkFile != null) {
                    fileName.setText(apkFile.getName());

                    long fileSize = apkFile.length();
                    String fileSizeFormatted = formatFileSize(fileSize);
                    filesize.setText(fileSizeFormatted);
                }
                if (!String.valueOf(apkFile).contains(".apk")) {
                    install.setVisibility(View.GONE);
                }
                textview1.setText(String.valueOf((long) (position + 1)));
                install.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (String.valueOf(apkFile).contains(".apk")) {
                            install(String.valueOf(apkFile));
                        }
                    }
                });
                linear1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (String.valueOf(apkFile).contains(".txt")) {
                            openTxtFile(apkFile);
                        }
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (apkFile != null) {
                            apkFiles.remove(apkFile);
                            apkFile.delete();
                            Parcelable listState = listView.onSaveInstanceState();
                            sortApkFilesByTime();
                            ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                            listView.onRestoreInstanceState(listState);
                        }
                    }
                });
                return view;
            }
        };
        Parcelable listState = listView.onSaveInstanceState();
        listView.setAdapter(adapter);
        sortApkFilesByTime();
        ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
        listView.onRestoreInstanceState(listState);
    }
    private void openTxtFile(File txtFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", txtFile);
        intent.setDataAndType(uri, "text/plain");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle if there's no suitable app to open the file
            Toast.makeText(this, "No app found to open the file.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortApkFilesByTime() {
        Collections.sort(fileData, new Comparator<ApkFileData>() {
            @Override
            public int compare(ApkFileData apk1, ApkFileData apk2) {
                return Long.compare(apk1.getTimestamp(), apk2.getTimestamp());
            }
        });
    }
    private static class ApkFileData {
        private File file;
        private long timestamp;

        public ApkFileData(File file, long timestamp) {
            this.file = file;
            this.timestamp = timestamp;
        }

        public File getFile() {
            return file;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
    private String formatFileSize(long fileSize) {
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = fileSize;
        while (size > 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return new DecimalFormat("#,##0.#").format(size) + " " + units[unitIndex];
    }
    public void install(final String PATH) {
        java.io.File file = new java.io.File(PATH);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uriFromFile(getApplicationContext(), new java.io.File(PATH)), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                getApplicationContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in opening the file!");
            }
        } else {
            Toast.makeText(getApplicationContext(),"installing",Toast.LENGTH_LONG).show();
        }
    }
    Uri uriFromFile(Context context, java.io.File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return androidx.core.content.FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName() + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }
    public void gradientDrawable(final View view, final double _radius, final double _stroke, final double _shadow, final String _color, final String _borderColor, final boolean _ripple) {
        if (_ripple) {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor(_color));
            gd.setCornerRadius((int)_radius);
            gd.setStroke((int)_stroke,Color.parseColor(_borderColor));
            view.setElevation((int)_shadow);
            android.content.res.ColorStateList clrb = new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#9E9E9E")});
            android.graphics.drawable.RippleDrawable ripdrb = new android.graphics.drawable.RippleDrawable(clrb , gd, null);
            view.setClickable(true);
            view.setBackground(ripdrb);
        }
        else {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor(_color));
            gd.setCornerRadius((int)_radius);
            gd.setStroke((int)_stroke,Color.parseColor(_borderColor));
            view.setBackground(gd);
            view.setElevation((int)_shadow);
        }
    }
}