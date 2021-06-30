package com.wif.baseservice.reflex;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

public class PackageManagerReflex {
    public static final String TAG = "PackageManagerReflex";

    public static final int INSTALL_REPLACE_EXISTING = 0x00000002;

    public static final int INSTALL_SUCCEEDED = 1;

    public static final int DELETE_SUCCEEDED = 1;

    /**
     * 静默安转
     */
    public static void installPackage(Context context, File apk, IPackageInstallObserver observer) {
        if (context == null) {
            return;
        }

        try {
            PackageManager pm = context.getPackageManager();
            Class<?>[] types = new Class[]{Uri.class, IPackageInstallObserver.class, int.class, String.class};
            Method method = pm.getClass().getMethod("installPackage", types);
            method.invoke(pm, Uri.fromFile(apk), observer, INSTALL_REPLACE_EXISTING, null);
        } catch (Exception e) {
            Log.e(TAG, "installPackage: " + e.getMessage());
        }
    }

    /**
     * 静默卸载
     */
    public static void deletePackage(Context context, String packageName, IPackageDeleteObserver observer) {
        if (context == null) {
            return;
        }

        try {
            PackageManager pm = context.getPackageManager();
            Class<?>[] types = new Class[]{String.class, IPackageDeleteObserver.class, int.class};
            Method deletePackage = pm.getClass().getMethod("deletePackage", types);
            deletePackage.invoke(pm, packageName, observer, 0);
        } catch (Exception e) {
            Log.e(TAG, "deletePackage: " + e.getMessage());
        }
    }

}
