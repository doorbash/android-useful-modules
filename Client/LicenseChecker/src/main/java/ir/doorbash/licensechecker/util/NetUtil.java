package ir.doorbash.licensechecker.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Milad Doorbash on 3/16/16.
 */
public class NetUtil {
    public static boolean isDeviceConnectedToInternet(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetwork != null && wifiNetwork.isConnected()) {
                return true;
            }

            NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobileNetwork != null && mobileNetwork.isConnected()) {
                return true;
            }

            return false;
        } catch (Throwable e) {

        }
        return true;

    }
}
