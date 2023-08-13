package com.apk.datavault.offline;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
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
    private SwipeRefreshLayout swipe;
    private List<ApkFileData> fileData = new ArrayList<>();
    private AnimationUtils animationUtils;
    private static final int BACK_PRESS_DELAY = 2000; // 2 seconds
    private long backPressTime;

    @Override
    public void onBackPressed() {
        if (DataExtension.isConnected(getApplicationContext())) {
            finish();
        } else if (!DataExtension.isConnected(getApplicationContext())) {
            if (System.currentTimeMillis() - backPressTime < BACK_PRESS_DELAY) {
                finishAffinity(); // Exit the app or perform your desired action here
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                backPressTime = System.currentTimeMillis();
            }
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
        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Parcelable listState = listView.onSaveInstanceState();
                    ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                    listView.onRestoreInstanceState(listState);
                    swipe.setRefreshing(false);
                } catch (Exception e) {
                    swipe.setRefreshing(false);
                }
            }
        });
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
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".apk") || file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
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
                final LinearLayout linear1 = view.findViewById(R.id.linear1);
                final LinearLayout linear2 = view.findViewById(R.id.linear2);
                final TextView textview1 = view.findViewById(R.id.textview1);
                final TextView fileName = view.findViewById(R.id.textview2);
                final TextView filesize = view.findViewById(R.id.textview3);
                final TextView textview4 = view.findViewById(R.id.textview4);
                final TextView type1 = view.findViewById(R.id.type1);
                final TextView type2 = view.findViewById(R.id.type2);
                final TextView type3 = view.findViewById(R.id.type3);
                final TextView type4 = view.findViewById(R.id.type4);
                final TextView end_of_list = view.findViewById(R.id.end_of_list);
                final LinearLayout type_holder1 = view.findViewById(R.id.type_holder1);
                final LinearLayout type_holder2 = view.findViewById(R.id.type_holder2);
                final LinearLayout type_holder3 = view.findViewById(R.id.type_holder3);
                final LinearLayout type_holder4 = view.findViewById(R.id.type_holder4);
                final ImageView img1 = view.findViewById(R.id.img1);
                final ImageView img2 = view.findViewById(R.id.img2);
                final ImageView img3 = view.findViewById(R.id.img3);
                final ImageView img4 = view.findViewById(R.id.img4);
                final ImageView install = view.findViewById(R.id.install);
                final ImageView delete = view.findViewById(R.id.delete);
                final ImageView image = view.findViewById(R.id.image);
                textview4.setVisibility(View.GONE);
                type_holder1.setVisibility(View.GONE);
                install.setVisibility(View.GONE);
                delete.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                type2.setText(R.string.install);
                type3.setText(R.string.share);
                type4.setText(R.string.delete);
                {
                    android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
                    int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
                    SketchUi.setCornerRadius(d * 300);
                    SketchUi.setStroke(d, 0xFF9E9E9E);
                    type_holder1.setBackground(SketchUi);
                }
                {
                    android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
                    int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
                    SketchUi.setCornerRadius(d * 300);
                    SketchUi.setStroke(d, 0xFF2196F3);
                    type_holder2.setBackground(SketchUi);
                }
                {
                    android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
                    int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
                    SketchUi.setCornerRadius(d * 300);
                    SketchUi.setStroke(d, 0xFFF44336);
                    type_holder3.setBackground(SketchUi);
                }
                {
                    android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
                    int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
                    SketchUi.setCornerRadius(d * 300);
                    SketchUi.setStroke(d, 0xFF228B22);
                    type_holder4.setBackground(SketchUi);
                }
                if (position == getCount() - 1) {
                    end_of_list.setVisibility(View.VISIBLE);
                } else {
                    end_of_list.setVisibility(View.GONE);
                }
                {
                    {
                        gradientDrawable(linear1, 0, 0, 0, "#FF202226", "#000000", false);
                        gradientDrawable(linear2, 0, 2, 20, "#FF202226", "#FF2A2B2F", false);
                    }
                }
                File apkFile = getItem(position);
                if (apkFile != null) {
                    fileName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    fileName.setText(apkFile.getName());
                    fileName.setSelected(true);
                    fileName.setSingleLine(true);
                    long fileSize = apkFile.length();
                    String fileSizeFormatted = formatFileSize(fileSize);
                    filesize.setText(fileSizeFormatted);
                }
                if (String.valueOf(apkFile).contains(".apk")) {
                    image.setImageResource(R.drawable.install_mobile);
                } else if (String.valueOf(apkFile).contains(".txt")) {
                    image.setImageResource(R.drawable.file_open);
                }
//                assert apkFile != null;
//                Drawable apkIcon = getApkIcon(apkFile);
//                if (apkIcon != null) {
//                    apkicon.setImageDrawable(apkIcon);
//                } else {
//                    apkicon.setImageResource(R.drawable.update); // Set a default image if no icon found
//                }
                textview1.setText(String.valueOf((long) (position + 1)));
                type2.setOnClickListener(view1 -> {
                    if (String.valueOf(apkFile).contains(".apk")) {
                        install(String.valueOf(apkFile));
                    } else if (String.valueOf(apkFile).contains(".txt")) {
                        openTxtFile(apkFile);
                    }
                });
                type3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (apkFile != null) {
                            shareApkFile(apkFile);
                        }
                    }
                });
                type4.setOnClickListener(v -> {
                    if (apkFile != null) {
                        apkFiles.remove(apkFile);
                        apkFile.delete();
                        Parcelable listState = listView.onSaveInstanceState();
                        sortApkFilesByTime();
                        ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                        listView.onRestoreInstanceState(listState);
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
    private void shareApkFile(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.android.package-archive");
        Uri apkUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", apkFile);
        intent.putExtra(Intent.EXTRA_STREAM, apkUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(intent, "Share APK using"));
        } catch (ActivityNotFoundException e) {
            // Handle if no suitable app to handle sharing
            Toast.makeText(this, "No app found to share the APK.", Toast.LENGTH_SHORT).show();
        }
    }

    private Drawable getApkIcon(File apkFile) {
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), 0);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            appInfo.sourceDir = apkFile.getAbsolutePath();
            appInfo.publicSourceDir = apkFile.getAbsolutePath();
            return appInfo.loadIcon(pm);
        }
        return null;
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