package ir.doorbash.update.downloader.util.lock;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import ir.doorbash.update.downloader.util.LogUtil;

/**
 * Created by Milad Doorbash on 3/16/16.
 */
public class DownloadPowerLock {

    public static final String WAKE_LOG_TAG = "UD_DOWNLOAD";
    private static PowerManager.WakeLock mWakeLock;

    public static void acquire(Context c) {
        Log.i(LogUtil.TAG, WAKE_LOG_TAG + " powerlock acquired!");
        try {
            if (mWakeLock == null) {
                PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        WAKE_LOG_TAG);
            }
            mWakeLock.acquire();
        } catch (Exception e) {

        }
    }

    public static void release() {
        Log.i(LogUtil.TAG, WAKE_LOG_TAG + " powerlock released!");
        try {
            if (mWakeLock != null) {
                mWakeLock.release();
            }
        } catch (Exception e) {

        }
    }
}
