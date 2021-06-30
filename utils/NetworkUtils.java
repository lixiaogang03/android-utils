package com.wif.baseservice.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;

public class NetworkUtils {
    public static final String TAG = "NetworkUtils";

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null) {
            return false;
        }

        boolean isAvailable = networkInfo.isConnected() && networkInfo.isAvailable();

        if (isAvailable) {
            boolean ping = isAvailableByPing();
            Log.w(TAG, "ping -c 3 -w 3 223.5.5.5------" + ping);
            return ping;
        }

        return false;
    }


    /**
     * https://www.jianshu.com/p/c68376c615a5
     */
    public static boolean isAvailableByPing() {
        Runtime runtime = Runtime.getRuntime();
        Process ipProcess = null;
        try {
            ipProcess = runtime.exec("ping -c 3 -w 3 223.5.5.5");  // alibaba public ip
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (ipProcess != null) {
                ipProcess.destroy();
            }
            runtime.gc();
        }
        return false;
    }

}
