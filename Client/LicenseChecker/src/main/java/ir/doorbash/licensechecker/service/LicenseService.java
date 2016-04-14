package ir.doorbash.licensechecker.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import ir.doorbash.licensechecker.LC;
import ir.doorbash.licensechecker.preferences.Settings;
import ir.doorbash.licensechecker.util.LogUtil;
import ir.doorbash.licensechecker.util.NetUtil;
import ir.doorbash.licensechecker.util.lock.CheckPowerLock;

/**
 * Created by Milad Doorbash on 3/10/16.
 */
public class LicenseService extends Service {

    public static final int CHECK_TIME_GAP = 60 * 60 * 1000;
    public static final int ACTION_CHECK_LICENSE = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(LogUtil.TAG, "LicenseService.onStartCommand()");

        if (intent == null)
            return Service.START_NOT_STICKY;


        switch (intent.getIntExtra("action", -1)) {
            case ACTION_CHECK_LICENSE:
                // check if new version is available

                Log.d(LogUtil.TAG, "LicenseService : Checking license...");

                checkLicence();

                break;

            default:
                Log.e(LogUtil.TAG, "LicenseService : Bad Event Type : " + intent.getIntExtra("event", -1));
                break;
        }


        return Service.START_NOT_STICKY;
    }

    public synchronized void checkLicence() {
        if (!NetUtil.isDeviceConnectedToInternet(getApplicationContext())) {
            CheckPowerLock.release();
            Log.d(LogUtil.TAG, "LicenseService : checkLicense : Device is not connected to internet.");
            return;
        }
        Log.d(LogUtil.TAG, "LicenseService : checkLicense()");
        Settings settings = Settings.getInstance(getApplicationContext());
        long lastCheckTime = settings.getLong(Settings.KEY_LAST_CHECK_TIME, 0);
        long now = System.currentTimeMillis();

        Log.d(LogUtil.TAG, "LicenseService : checkLicense : last=" + lastCheckTime + " ::: now=" + now + " ::: now-last=" + (now - lastCheckTime));

        if (now > (lastCheckTime + CHECK_TIME_GAP)) {
            LC.checkLicense(getApplicationContext());
            settings.setLong(Settings.KEY_LAST_CHECK_TIME, now).commit();
        } else {
            Log.d(LogUtil.TAG, "LicenseService : Just checked.have to wait for time gap to pass");
            CheckPowerLock.release();
        }
    }
}
