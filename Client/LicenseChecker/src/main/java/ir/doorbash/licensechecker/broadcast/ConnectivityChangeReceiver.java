package ir.doorbash.licensechecker.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ir.doorbash.licensechecker.util.LogUtil;
import ir.doorbash.licensechecker.util.lock.CheckPowerLock;

import ir.doorbash.licensechecker.service.LicenseService;


/**
 * Created by Milad Doorbash on 3/10/16.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(LogUtil.TAG, "ConnectivityChangeReceiver.onReceive()");

        CheckPowerLock.acquire(context);

        Intent i = new Intent(context, LicenseService.class);
        i.putExtra("action", LicenseService.ACTION_CHECK_LICENSE);

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, PendingIntent.getService(context, 3333, i, 0));
    }
}
