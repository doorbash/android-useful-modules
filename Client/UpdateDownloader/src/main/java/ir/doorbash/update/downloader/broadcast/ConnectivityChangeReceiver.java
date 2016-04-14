package ir.doorbash.update.downloader.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ir.doorbash.update.downloader.service.UpdateService;
import ir.doorbash.update.downloader.util.LogUtil;
import ir.doorbash.update.downloader.util.lock.CheckPowerLock;

/**
 * Created by Milad Doorbash on 3/10/16.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {

    public static final String TAG = "ConnectivityChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(LogUtil.TAG, "ConnectivityChangeReceiver.onReceive()");

        CheckPowerLock.acquire(context);

        Intent i = new Intent(context, UpdateService.class);
        i.putExtra("action", UpdateService.ACTION_CHECK_NEW_VERSION);
//        context.startService(i);

        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, PendingIntent.getService(context, 3242, i, 0));
    }
}
