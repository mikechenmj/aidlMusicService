package com.android.mikechenmj.myapplication.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikechenmj on 17-2-14.
 */
public class PermissionHelper {

    private PermissionHelper() {
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode,
                                          PermissionCallback callback) {
        boolean isPermissionGranted;
        boolean isAllGranted;
        boolean doNotRequest = false;
        List<String> permissionsRequestList = new ArrayList<String>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsRequestList.add(permission);
                isPermissionGranted = false;
            } else {
                isPermissionGranted = true;
            }
            if (callback != null) {
                doNotRequest = callback.onPermissionGrantedStates(permission, isPermissionGranted,
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission));
            }
        }
        if (permissionsRequestList.size() > 0) {
            isAllGranted = false;
            if (!doNotRequest) {
                ActivityCompat.requestPermissions(activity,
                        permissionsRequestList.toArray(new String[permissionsRequestList.size()]),
                        requestCode);
            }
        } else {
            isAllGranted = true;
        }
        if (callback != null) {
            callback.onAllGranted(isAllGranted);
        }
    }

    public static void onRequestPermissionsResult(Activity activity, String[] permissions,
                                                  int[] grantResults, PermissionCallback callback) {
        boolean isPermissionGranted;
        boolean isAllGranted = true;
        if (grantResults != null && grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = true;
                } else {
                    isAllGranted = false;
                    isPermissionGranted = false;
                }
                if (callback != null) {
                    callback.onPermissionGrantedStates(permissions[i], isPermissionGranted,
                            ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]));
                }
            }
            if (callback != null) {
                callback.onAllGranted(isAllGranted);
            }
        }
    }


    public interface PermissionCallback {

        boolean onPermissionGrantedStates(String permission, boolean isGranted, boolean shouldShowRationale);

        void onAllGranted(boolean isAllGranted);
    }
}
