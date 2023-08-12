package com.apk.datavault.extension;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class DataExtension {
    public static boolean isConnected(Context _context) {
        ConnectivityManager _connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo _activeNetworkInfo = _connectivityManager.getActiveNetworkInfo();
        return _activeNetworkInfo != null && _activeNetworkInfo.isConnected();
    }
    public static void sortListMap(final ArrayList<HashMap<String, Object>> listMap, final String key, final boolean isNumber, final boolean ascending) {
        Collections.sort(listMap, new Comparator<HashMap<String, Object>>() {
            public int compare(HashMap<String, Object> _compareMap1, HashMap<String, Object> _compareMap2) {
                if (isNumber) {
                    int _count1 = Integer.parseInt(_compareMap1.get(key).toString());
                    int _count2 = Integer.parseInt(_compareMap2.get(key).toString());
                    if (ascending) {
                        return _count1 < _count2 ? -1 : 0;
                    } else {
                        return _count1 > _count2 ? -1 : 0;
                    }
                } else {
                    if (ascending) {
                        return (_compareMap1.get(key).toString()).compareTo(_compareMap2.get(key).toString());
                    } else {
                        return (_compareMap2.get(key).toString()).compareTo(_compareMap1.get(key).toString());
                    }
                }
            }
        });
    }
    public static String getExternalStorageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
    public static String defaultApkDirectory() {
        return getExternalStorageDir().concat("/ApkVault/files/Apk/");
    }
    public static boolean isExistFile(String path) {
        File file = new File(path);
        return file.exists();
    }
    public static void makeDirectory(String path) {
        if (!isExistFile(path)) {
            File file = new File(path);
            file.mkdirs();
        }
    }
    public static void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        if (file.isFile()) {
            file.delete();
            return;
        }
        File[] fileArr = file.listFiles();
        if (fileArr != null) {
            for (File subFile : fileArr) {
                if (subFile.isDirectory()) {
                    deleteFile(subFile.getAbsolutePath());
                }

                if (subFile.isFile()) {
                    subFile.delete();
                }
            }
        }
        file.delete();
    }
    public static void renameFile(final String path, final String oldFileName, final String newFileName) {
        //Create file objects for the old and new file names
        File oldFile = new File(path, oldFileName);
        File newFile = new File(path, newFileName);
        if (oldFile.exists()) {
            if (oldFile.renameTo(newFile)) {
                //Toast.makeText(activity, "File renamed successfully!", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(activity, "Failed to rename the file.", Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(activity, "The old file does not exist.", Toast.LENGTH_SHORT).show();
        }
    }
}
