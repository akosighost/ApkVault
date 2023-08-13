package com.apk.datavault;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.apk.datavault.extension.DataExtension;
import com.apk.datavault.extension.RequestNetwork;
import com.apk.datavault.extension.RequestNetworkController;
import com.apk.datavault.extension.Utils;
import com.apk.datavault.offline.OfflineActivity;
import com.bumptech.glide.Glide;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class HomeActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private EditText search;
    private LinearLayout close_holder;
    private LinearLayout search_holder;
    private ImageView close;
    private LinearLayout linear1;
    private HorizontalScrollView scroll1;
    private LinearLayout slot1;
    private LinearLayout slot2;
    private LinearLayout slot3;
    private TextView textview_slot1;
    private TextView textview_slot2;
    private TextView textview_slot3;
    private AlertDialog successDialog;
    private AlertDialog downloadDialog;
    private AlertDialog updateDialog;
    private RequestNetwork internet;
    private RequestNetwork.RequestListener _internet_request_listener;
    private RequestNetwork update;
    private RequestNetwork.RequestListener _update_request_listener;
    private SharedPreferences save;
    private Intent intent = new Intent();
    private ArrayList<HashMap<String, Object>> datakey = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> dataupdate = new ArrayList<>();
    private int downloadId;
    private double number = 0;
    private double check = 0;
    private double length = 0;
    private String string_search = "";
    private String value = "";
    private String data = "";
    private static final int BACK_PRESS_DELAY = 2000; // 2 seconds
    private long backPressTime;

    @Override
    protected void onStart() {
        super.onStart();
        update.startRequestNetwork(RequestNetworkController.GET, getString(R.string.update_data), "", _update_request_listener);
    }
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - backPressTime < BACK_PRESS_DELAY) {
            finishAffinity(); // Exit the app or perform your desired action here
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            backPressTime = System.currentTimeMillis();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initialize(savedInstanceState);
        initialize();
        if (!DataExtension.isExistFile(DataExtension.defaultApkDirectory())) {
            DataExtension.makeDirectory(DataExtension.defaultApkDirectory());
        }
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.dark));
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
        }
    }
    private void initialize(Bundle savedInstanceState) {
        internet = new RequestNetwork(this);
        update = new RequestNetwork(this);
        save = getSharedPreferences("save", Activity.MODE_PRIVATE);
        swipeRefreshLayout = findViewById(R.id.swipe);
        search = findViewById(R.id.search);
        search_holder = findViewById(R.id.search_holder);
        close_holder = findViewById(R.id.close_holder);
        close = findViewById(R.id.close);
        listView = findViewById(R.id.listview);
        linear1 = findViewById(R.id.linear1);
        scroll1 = findViewById(R.id.scroll1);
        slot1 = findViewById(R.id.slot1);
        slot2 = findViewById(R.id.slot2);
        slot3 = findViewById(R.id.slot3);
        textview_slot1 = findViewById(R.id.textview_slot1);
        textview_slot2 = findViewById(R.id.textview_slot2);
        textview_slot3 = findViewById(R.id.textview_slot3);

        _internet_request_listener = new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
                final String _tag = _param1;
                final String _response = _param2;
                final HashMap<String, Object> _responseHeaders = _param3;
                try {
                    swipeRefreshLayout.setRefreshing(false);
                    datakey = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
                    listView.setAdapter(new listAdapter(datakey));
                    ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                    string_search = new Gson().toJson(datakey);
                    DataExtension.sortListMap(datakey, "name", false, true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
            @Override
            public void onErrorResponse(String _param1, String _param2) {
                final String _tag = _param1;
                final String _message = _param2;
                Toast.makeText(HomeActivity.this, _message, Toast.LENGTH_SHORT).show();
            }
        };
        _update_request_listener = new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
                final String _tag = _param1;
                final String _response = _param2;
                final HashMap<String, Object> _responseHeaders = _param3;
                swipeRefreshLayout.setRefreshing(false);
                try{
                    dataupdate = new Gson().fromJson(_response, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
                    try {
                        android.content.pm.PackageManager pm = getApplicationContext().getPackageManager(); android.content.pm.PackageInfo pinfo = pm.getPackageInfo(getApplicationContext().getPackageName().toString(), 0); String your_version = pinfo.versionName;
                        if (!your_version.equals(dataupdate.get((int)0).get("version").toString())) {
                            {
                                updateDialog = new AlertDialog.Builder(HomeActivity.this).create();
                                LayoutInflater cvLI = getLayoutInflater();
                                View cvCV = (View) cvLI.inflate(R.layout.dialog_popup1, null);
                                updateDialog.setView(cvCV);
                                Objects.requireNonNull(updateDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                final LinearLayout bg = cvCV.findViewById(R.id.linear1);
                                final LinearLayout download_holder = cvCV.findViewById(R.id.download_holder);
                                final LinearLayout button_holder = cvCV.findViewById(R.id.button_holder);
                                final TextView title = (TextView) cvCV.findViewById(R.id.textview1);
                                final TextView descriptions = (TextView) cvCV.findViewById(R.id.textview2);
                                final TextView size = (TextView) cvCV.findViewById(R.id.size);
                                final TextView button1 = (TextView) cvCV.findViewById(R.id.button1);
                                final TextView button2 = (TextView) cvCV.findViewById(R.id.button2);
                                final TextView value = (TextView) cvCV.findViewById(R.id.value);
                                final TextView percent = (TextView) cvCV.findViewById(R.id.percent);
                                final ImageView image = (ImageView) cvCV.findViewById(R.id.image);
                                final ProgressBar progressBar = (ProgressBar) cvCV.findViewById(R.id.progress);
                                download_holder.setVisibility(View.GONE);
                                button2.setText(R.string.update_dialog);
                                if (dataupdate.get((int) 0).containsKey("title")) {
                                    title.setVisibility(View.VISIBLE);
                                    title.setText(dataupdate.get((int) 0).get("title").toString().trim());
                                } else {
                                    title.setVisibility(View.GONE);
                                }
                                if (dataupdate.get((int) 0).containsKey("description")) {
                                    descriptions.setVisibility(View.VISIBLE);
                                    descriptions.setText(dataupdate.get((int) 0).get("description").toString().trim());
                                } else {
                                    descriptions.setVisibility(View.GONE);
                                }
                                button1.setOnClickListener(view -> {
                                    updateDialog.dismiss();
                                });
                                button2.setOnClickListener(view -> {
                                    _Transition(bg, 300);
                                    button_holder.setVisibility(View.GONE);
                                    download_holder.setVisibility(View.VISIBLE);
                                    if (dataupdate.get((int) 0).containsKey("link")) {
                                        if (!Objects.equals(dataupdate.get((int) 0).get("link"), "")) {
                                            {

                                                PRDownloader.initialize(getApplicationContext());
                                                PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                                                        .setDatabaseEnabled(true)
                                                        .build();
                                                final String url = dataupdate.get((int) 0).get("link").toString().replace("blob", "raw").trim();
                                                final String path = DataExtension.defaultApkDirectory();
                                                final String filename = new File(url).getName();
                                                final String newName = dataupdate.get((int) 0).get("name").toString().concat(" ".concat(dataupdate.get((int) 0).get("version").toString().concat(".apk")));
                                                PRDownloader.initialize(getApplicationContext(), config);
                                                downloadId = PRDownloader.download(url, path, filename)
                                                        .build()
                                                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                                                            @Override
                                                            public void onStartOrResume() {
                                                            }
                                                        })
                                                        .setOnPauseListener(new OnPauseListener() {
                                                            @Override
                                                            public void onPause() {
                                                            }
                                                        })
                                                        .setOnCancelListener(new OnCancelListener() {
                                                            @Override
                                                            public void onCancel() {
                                                            }
                                                        })
                                                        .setOnProgressListener(new OnProgressListener() {
                                                            @Override
                                                            public void onProgress(Progress progress) {
                                                                long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                                                                value.setText(Utils.getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                                                                progressBar.setProgress((int)progressPercent);
                                                                percent.setText(String.valueOf((long)progressPercent).concat(" %"));
                                                            }
                                                        })
                                                        .start(new OnDownloadListener() {
                                                            @Override
                                                            public void onDownloadComplete() {
                                                                DataExtension.renameFile(DataExtension.defaultApkDirectory(), filename, newName);
                                                                _Transition(bg, 300);
                                                                button_holder.setVisibility(View.VISIBLE);
                                                                download_holder.setVisibility(View.GONE);
                                                                size.setVisibility(View.GONE);
                                                                install(DataExtension.defaultApkDirectory().concat(newName));
                                                                button1.setOnClickListener(view12 -> {
                                                                    install(DataExtension.defaultApkDirectory().concat(newName));
                                                                });
                                                            }
                                                            @Override
                                                            public void onError(Error error) {
                                                                Toast.makeText(getApplicationContext(), R.string.unable_to_download, Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                image.setOnClickListener(view1 -> {
                                                    if (number == 1) {
                                                        number = 0;
                                                        PRDownloader.resume(downloadId);
                                                        button1.setText(R.string.pause_download_dialog);
                                                        image.setImageResource(R.drawable.pause);
                                                    } else if (number == 0) {
                                                        number = 1;
                                                        PRDownloader.pause(downloadId);
                                                        button1.setText(R.string.resume_download_dialog);
                                                        image.setImageResource(R.drawable.resume);
                                                    }
                                                });
                                            }
                                        } else {
                                            Toast.makeText(HomeActivity.this, R.string.unable_to_download, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(HomeActivity.this, R.string.unable_to_download, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                // Always false
                                updateDialog.setCancelable(false);
                                updateDialog.show();
                            }
                        }
                    } catch (android.content.pm.PackageManager.NameNotFoundException e) { e.printStackTrace(); }
                }catch(Exception e){
                    Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErrorResponse(String _param1, String _param2) {
                final String _tag = _param1;
                final String _message = _param2;
                Toast.makeText(HomeActivity.this, _message, Toast.LENGTH_SHORT).show();
            }
        };
        close.setOnClickListener(view -> {search.setText("");});
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
                final String _charSeq = _param1.toString();
                double num = 200.0;
                if (search.getText().toString().length() == 0) {
                    _Transition(search_holder, num);
                    _Transition(close_holder, num);
                    close_holder.setVisibility(View.GONE);
                    search.setCursorVisible(false);

                } else {
                    _Transition(search_holder, num);
                    _Transition(close_holder, num);
                    close_holder.setVisibility(View.VISIBLE);
                    search.setCursorVisible(true);
                }

                try {
                    datakey = new Gson().fromJson(string_search, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
                    length = datakey.size();
                    number = length - 1;
                    for (int _repeat17 = 0; _repeat17 < (int)(length); _repeat17++) {
                        value = datakey.get((int)number).get("name").toString();
                        if (!(_charSeq.length() > value.length()) && value.toLowerCase().contains(_charSeq.toLowerCase())) {
                        }
                        else {
                            datakey.remove((int)(number));
                            listView.setAdapter(new listAdapter(datakey));
                            ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                        }
                        number --;
                    }
                    listView.setAdapter(new listAdapter(datakey));
                    ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
                }
                catch (Exception ignored) {
                }
            }
            @Override
            public void afterTextChanged(Editable _param1) {
            }
            @Override
            public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {

            }
        });
    }
    private void initialize() {
        onCreate();
        dark(slot1);
        dark(slot2);
        slot1.performClick();
        {
            GradientDrawable SketchUi = new GradientDrawable();
            int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
            SketchUi.setColor(0xFF202226);
            SketchUi.setCornerRadius(d*5);
            SketchUi.setStroke(d,0xFF2A2B2F);
            search.setBackground(SketchUi);
        }
        {
            scroll1.setHorizontalScrollBarEnabled(false);
            scroll1.setVerticalScrollBarEnabled(false);
            OverScrollDecoratorHelper.setUpOverScroll(scroll1);
            swipeRefreshLayout.setHorizontalScrollBarEnabled(false);
            swipeRefreshLayout.setHorizontalScrollBarEnabled(false);
            listView.setHorizontalScrollBarEnabled(false);
            listView.setVerticalScrollBarEnabled(false);
            listView.setDivider(null);
            listView.setDividerHeight(0);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    internet.startRequestNetwork(RequestNetworkController.GET, getString(R.string.update_data), "", _update_request_listener);
                }
            });
        }
    }
    public class listAdapter extends BaseAdapter{
        ArrayList<HashMap<String, Object>> data;
        public listAdapter(ArrayList<HashMap<String, Object>> _arr) {
            data = _arr;
        }
        @Override
        public int getCount() {
            return data.size();
        }
        @Override
        public HashMap<String, Object> getItem(int _index) {
            return data.get(_index);
        }
        @Override
        public long getItemId(int _index) {
            return _index;
        }
        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public View getView(final int position, View v, ViewGroup _container) {
            LayoutInflater _inflater = getLayoutInflater();
            View view = v;
            if (view == null) {
                view = _inflater.inflate(R.layout.home_list, null);
            }
            final LinearLayout linear1 = view.findViewById(R.id.linear1);
            final LinearLayout linear2 = view.findViewById(R.id.linear2);
            final TextView textview1 = view.findViewById(R.id.textview1);
            final TextView textview2 = view.findViewById(R.id.textview2);
            final TextView textview3 = view.findViewById(R.id.textview3);
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
            final ImageView apkicon = view.findViewById(R.id.image);
            install.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            apkicon.setVisibility(View.GONE);
            if (position == getCount() - 1) {
                end_of_list.setVisibility(View.VISIBLE);
            } else {
                end_of_list.setVisibility(View.GONE);
            }
            {
                textview3.setVisibility(View.GONE);
                textview4.setVisibility(View.GONE);
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
                textview1.setText(String.valueOf((long) (position + 1)));
                if (data.get((int) position).containsKey("name")) {
                    if (!data.get((int) position).get("name").equals("")) {
                        linear1.setVisibility(View.VISIBLE);
                        textview2.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        textview2.setText(data.get((int) position).get("name").toString().trim());
                        textview2.setSelected(true);
                        textview2.setSingleLine(true);
                    } else {
                        linear1.setVisibility(View.GONE);
                    }

                }
                if (data.get((int) position).containsKey("version")) {
                    if (!data.get((int) position).get("version").equals("")) {
                        textview3.setVisibility(View.VISIBLE);
                        textview3.setText(getString(R.string.version).concat(" : ").concat(data.get((int) position).get("version").toString()));
                    } else {
                        textview3.setVisibility(View.GONE);
                    }
                } else {
                    textview3.setVisibility(View.GONE);
                }
                if (data.get((int) position).containsKey("link")) {
                    if (Objects.equals(data.get((int) position).get("link"), "")) {
                        textview4.setText(R.string.unable_to_download);
                    } else {
                        textview4.setVisibility(View.GONE);
                    }
                } else {
                    textview4.setVisibility(View.GONE);
                }
                if (data.get((int) position).containsKey("type1")) {
                    if (Objects.equals(data.get((int) position).get("type1"), "")) {
                        type_holder1.setVisibility(View.GONE);
                    } else {
                        type_holder1.setVisibility(View.VISIBLE);
                        type1.setText(data.get((int) position).get("type1").toString().trim());
                    }
                } else {
                    type_holder1.setVisibility(View.GONE);
                }
                if (data.get((int) position).containsKey("type2")) {
                    if (data.get((int) position).get("type2").equals("")) {
                        type_holder2.setVisibility(View.GONE);
                    } else {
                        type_holder2.setVisibility(View.VISIBLE);
                        type2.setText(data.get((int) position).get("type2").toString().trim());
                    }
                } else {
                    type_holder2.setVisibility(View.GONE);
                }
                if (data.get((int) position).containsKey("type3")) {
                    if (Objects.equals(data.get((int) position).get("type3"), "")) {
                        type_holder3.setVisibility(View.GONE);
                    } else {
                        type_holder3.setVisibility(View.VISIBLE);
                        type3.setText(data.get((int) position).get("type3").toString().trim());
                    }
                } else {
                    type_holder3.setVisibility(View.GONE);
                }
                if (data.get((int) position).containsKey("type4")) {
                    if (Objects.equals(data.get((int) position).get("type4"), "")) {
                        type_holder4.setVisibility(View.GONE);
                    } else {
                        type_holder4.setVisibility(View.VISIBLE);
                        type4.setText(data.get((int) position).get("type4").toString().trim());
                    }
                } else {
                    type_holder4.setVisibility(View.GONE);
                }
                if (data.get((int) position).containsKey("img1")) {
                    if (Objects.equals(data.get((int) position).get("img1"), "")) {
                        img1.setVisibility(View.GONE);
                    } else {
                        img1.setVisibility(View.VISIBLE);
                        Glide.with(getApplicationContext()).load(Uri.parse(data.get((int) position).get("img1").toString().replace("blob", "raw"))).into(img1);
                    }
                } else {
                    img1.setVisibility(View.GONE);
                }
                if (data.get((int) position).containsKey("img2")) {
                    if (Objects.equals(data.get((int) position).get("img2"), "")) {
                        img2.setVisibility(View.GONE);
                    } else {
                        img2.setVisibility(View.VISIBLE);
                        Glide.with(getApplicationContext()).load(Uri.parse(data.get((int) position).get("img2").toString().replace("blob", "raw"))).into(img2);
                    }
                } else {
                    img2.setVisibility(View.GONE);
                }
                if (data.get((int) position).containsKey("img3")) {
                    if (Objects.equals(data.get((int) position).get("img3"), "")) {
                        img3.setVisibility(View.GONE);
                    } else {
                        img3.setVisibility(View.VISIBLE);
                        Glide.with(getApplicationContext()).load(Uri.parse(data.get((int) position).get("img3").toString().replace("blob", "raw"))).into(img3);
                    }
                } else {
                    img3.setVisibility(View.GONE);
                }
                if (data.get((int) position).containsKey("img4")) {
                    if (Objects.equals(data.get((int) position).get("img4"), "")) {
                        img4.setVisibility(View.GONE);
                    } else {
                        img4.setVisibility(View.VISIBLE);
                        Glide.with(getApplicationContext()).load(Uri.parse(data.get((int) position).get("img4").toString().replace("blob", "raw"))).into(img4);
                    }
                } else {
                    img4.setVisibility(View.GONE);
                }
            }
            linear1.setOnClickListener(_view -> {
                if (data.get((int)position).containsKey("link")) {
                    if (!Objects.equals(data.get((int) position).get("link"), "")) {
                        {
                            downloadDialog = new AlertDialog.Builder(HomeActivity.this).create();
                            LayoutInflater cvLI = getLayoutInflater();
                            View cvCV = (View) cvLI.inflate(R.layout.download, null);
                            downloadDialog.setView(cvCV);
                            Objects.requireNonNull(downloadDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            final LinearLayout bg = (LinearLayout) cvCV.findViewById(R.id.linear1);
                            final TextView value = (TextView) cvCV.findViewById(R.id.value);
                            final TextView title = (TextView) cvCV.findViewById(R.id.title);
                            final TextView percent = (TextView) cvCV.findViewById(R.id.percent);
                            final TextView button1 = (TextView) cvCV.findViewById(R.id.b1);
                            final TextView button2 = (TextView) cvCV.findViewById(R.id.b2);
                            final ProgressBar progressBar = (ProgressBar) cvCV.findViewById(R.id.progress);
                            final CheckBox checkBox = (CheckBox) cvCV.findViewById(R.id.checkbox);
                            checkBox.setChecked(true);
                            {
                                GradientDrawable SketchUi = new GradientDrawable();
                                int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
                                SketchUi.setColor(getColor(R.color.dark));
                                SketchUi.setCornerRadius(d*20);
                                bg.setBackground(SketchUi);
                            }
                            try {
                                if (Double.parseDouble(save.getString("save_switch", "")) == 0) {
                                    checkBox.setChecked(true);
                                } else {
                                    checkBox.setChecked(false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String url = data.get((int)position).get("link").toString().replace("blob", "raw").trim();
                            String path = DataExtension.defaultApkDirectory();
                            String name = data.get((int)position).get("name").toString().concat(" ".concat(data.get((int)position).get("version").toString().concat(".apk")));
                            {

                                PRDownloader.initialize(getApplicationContext());
                                PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                                        .setDatabaseEnabled(true)
                                        .build();
                                PRDownloader.initialize(getApplicationContext(), config);
                                downloadId = PRDownloader.download(url, path, name)
                                        .build()
                                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                                            @Override
                                            public void onStartOrResume() {
                                            }
                                        })
                                        .setOnPauseListener(new OnPauseListener() {
                                            @Override
                                            public void onPause() {
                                            }
                                        })
                                        .setOnCancelListener(new OnCancelListener() {
                                            @Override
                                            public void onCancel() {
                                            }
                                        })
                                        .setOnProgressListener(new OnProgressListener() {
                                            @Override
                                            public void onProgress(Progress progress) {
                                                long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                                                value.setText(Utils.getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                                                progressBar.setProgress((int)progressPercent);
                                                percent.setText(String.valueOf((long)progressPercent).concat(" %"));
                                            }
                                        })
                                        .start(new OnDownloadListener() {
                                            @Override
                                            public void onDownloadComplete() {
                                                title.setText(getString(R.string.download_success));
                                                if (checkBox.isChecked()) {
                                                    downloadDialog.dismiss();
                                                    install(DataExtension.defaultApkDirectory().concat(name));
                                                } else {
                                                    downloadDialog.dismiss();
                                                    successDialog = new AlertDialog.Builder(HomeActivity.this).create();
                                                    LayoutInflater cvLI = getLayoutInflater();
                                                    View cvCV = (View) cvLI.inflate(R.layout.dialog_popup2, null);
                                                    successDialog.setView(cvCV);
                                                    Objects.requireNonNull(successDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                    final LinearLayout bg = (LinearLayout)  cvCV.findViewById(R.id.linear1);
                                                    final TextView descriptions = (TextView) cvCV.findViewById(R.id.textview2);
                                                    final TextView open = (TextView) cvCV.findViewById(R.id.textview3);
                                                    {
                                                        GradientDrawable SketchUi = new GradientDrawable();
                                                        int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
                                                        SketchUi.setColor(getColor(R.color.dark));
                                                        SketchUi.setCornerRadius(d*20);
                                                        bg.setBackground(SketchUi);
                                                    }
                                                    open.setText(R.string.install_dialog);
                                                    descriptions.setText(DataExtension.defaultApkDirectory().concat(name));
                                                    open.setOnClickListener(view -> {
                                                        install(DataExtension.defaultApkDirectory().concat(name));
                                                        successDialog.dismiss();
                                                    });
                                                    successDialog.setCancelable(true);
                                                    successDialog.show();
                                                }
                                            }
                                            @Override
                                            public void onError(Error error) {
                                                downloadDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Unable to download!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                                    if (b) {
                                        check = 0;
                                        save.edit().putString("save_switch", String.valueOf((long)(check))).apply();
                                    } else {
                                        check = 1;
                                        save.edit().putString("save_switch", String.valueOf((long)(check))).apply();
                                    }
                                });
                                button1.setOnClickListener(view1 -> {
                                    if (number == 1) {
                                        number = 0;
                                        PRDownloader.resume(downloadId);
                                        button1.setText(R.string.pause_download_dialog);
                                    } else if (number == 0) {
                                        number = 1;
                                        PRDownloader.pause(downloadId);
                                        button1.setText(R.string.resume_download_dialog);
                                    }
                                });
                                button2.setOnClickListener(view12 -> {
                                    PRDownloader.cancel(downloadId);
                                    downloadDialog.dismiss();
                                });
                            }
                            downloadDialog.setCancelable(false);
                            downloadDialog.show();
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "Unable to download!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Unable to download!", Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }
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
        }
    }
    Uri uriFromFile(Context context, java.io.File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return androidx.core.content.FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName() + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }
    public void refresh(final String data_key) {
        swipeRefreshLayout.setRefreshing(true);
        internet.startRequestNetwork(RequestNetworkController.GET, data_key, "", _internet_request_listener);
    }
    public void _Transition(final View _view, final double _duration) {
        LinearLayout ViewGroup = (LinearLayout) _view;
        android.transition.AutoTransition autoTransition = new android.transition.AutoTransition();
        autoTransition.setDuration((long)_duration);
        android.transition.TransitionManager.beginDelayedTransition(ViewGroup, autoTransition);
    }
    public void gradientDrawable(final View _view, final double _radius, final double _stroke, final double _shadow, final String _color, final String _borderColor, final boolean _ripple) {
        if (_ripple) {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor(_color));
            gd.setCornerRadius((int)_radius);
            gd.setStroke((int)_stroke,Color.parseColor(_borderColor));
            _view.setElevation((int)_shadow);
            android.content.res.ColorStateList clrb = new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{Color.parseColor("#9E9E9E")});
            android.graphics.drawable.RippleDrawable ripdrb = new android.graphics.drawable.RippleDrawable(clrb , gd, null);
            _view.setClickable(true);
            _view.setBackground(ripdrb);
        }
        else {
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor(_color));
            gd.setCornerRadius((int)_radius);
            gd.setStroke((int)_stroke,Color.parseColor(_borderColor));
            _view.setBackground(gd);
            _view.setElevation((int)_shadow);
        }
    }
    public void onCreate() {
        slot1.setOnClickListener(v -> {
            data = getString(R.string.data1);
            refresh(data);
            slot1();
        });
        slot2.setOnClickListener(v -> {
            data = getString(R.string.data2);
            refresh(data);
            slot2();
        });
        slot3.setOnClickListener(v -> {
            intent.setClass(getApplicationContext(), OfflineActivity.class);
            startActivity(intent);
        });
    }
    public void lightDark(final View _view) {
        {
            android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
            int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
            SketchUi.setColor(0xFF2A2B2F);
            SketchUi.setCornerRadius(d*300);
            SketchUi.setStroke(d*2,0xFF2A2B2F);
            _view.setBackground(SketchUi);
        }
    }
    public void dark(final View _view){
        {
            android.graphics.drawable.GradientDrawable SketchUi = new android.graphics.drawable.GradientDrawable();
            int d = (int) getApplicationContext().getResources().getDisplayMetrics().density;
            SketchUi.setColor(0xFF202226);
            SketchUi.setCornerRadius(d*300);
            SketchUi.setStroke(d*2,0xFF2A2B2F);
            _view.setBackground(SketchUi);
        }
    }
    public void slot1() {
        lightDark(slot1);
        dark(slot2);
        dark(slot3);
    }
    public void slot2() {
        dark(slot1);
        lightDark(slot2);
        dark(slot3);
    }
    public void slot3() {
        dark(slot1);
        dark(slot2);
        lightDark(slot3);
    }
}