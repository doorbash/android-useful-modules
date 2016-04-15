package ir.doorbash.update.downloader.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import ir.doorbash.update.downloader.UD;
import ir.doorbash.update.downloader.model.Update;
import ir.doorbash.update.downloader.preferences.Settings;
import ir.doorbash.update.downloader.util.DownloadListener;
import ir.doorbash.update.downloader.util.FileDownloader;
import ir.doorbash.update.downloader.util.LogUtil;
import ir.doorbash.update.downloader.util.NetUtil;
import ir.doorbash.update.downloader.util.lock.CheckPowerLock;
import ir.doorbash.update.downloader.util.lock.DownloadPowerLock;

/**
 * Created by Milad Doorbash on 3/10/16.
 */
public class UpdateService extends Service {

    public static final int CHECK_TIME_GAP = 60 * 60 * 1000;
    public static final int ACTION_CHECK_NEW_VERSION = 0;
    public static final int ACTION_DOWNLOAD = 1;
    public FileDownloader fileDownloader;
    public DownloadListener listener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(LogUtil.TAG, "UpdateService.onStartCommand()");

        if (intent == null)
            return Service.START_NOT_STICKY;


        switch (intent.getIntExtra("action", -1)) {
            case ACTION_CHECK_NEW_VERSION:
                // check if new version is available

                Log.d(LogUtil.TAG, "UpdateService : Checking new version...");

                checkNewVersion();

                break;
            case ACTION_DOWNLOAD:
                // new version is available download it !

                Log.d(LogUtil.TAG, "UpdateService : Downloading new version...");

                int version = intent.getIntExtra("version", 0);
                boolean forceInstall = intent.getBooleanExtra("forceInstall", false);
                String lastChanges = intent.getStringExtra("lastChanges");
                int fileSize = intent.getIntExtra("filesize", 0);
                String name = intent.getStringExtra("name");
                String md5 = intent.getStringExtra("md5");

                Update update = new Update(version, forceInstall, lastChanges, fileSize, name, md5);

                processNewVersion(update);


                break;

            default:
                Log.e(LogUtil.TAG, "UpdateService : Bad Event Type : " + intent.getIntExtra("event", -1));
                break;
        }


        return Service.START_NOT_STICKY;
    }

    public synchronized void checkNewVersion() {
        if (!NetUtil.isDeviceConnectedToInternet(getApplicationContext())) {
            CheckPowerLock.release();
            Log.d(LogUtil.TAG, "UpdateService : checkNewVersion : Device is not connected to internet.");
            return;
        }
        Log.d(LogUtil.TAG, "UpdateService : checkNewVersion()");
        Settings settings = Settings.getInstance(getApplicationContext());
        long lastCheckTime = settings.getLong(Settings.KEY_LAST_CHECK_TIME, 0);
        long now = System.currentTimeMillis();

        Log.d(LogUtil.TAG, "UpdateService : checkNewVersion : last=" + lastCheckTime + " ::: now=" + now + " ::: now-last=" + (now - lastCheckTime));

        if (now > (lastCheckTime + CHECK_TIME_GAP)) {
            UD.checkNewVersion(getApplicationContext());
            settings.setLong(Settings.KEY_LAST_CHECK_TIME, now).commit();
        } else {
            Log.d(LogUtil.TAG, "UpdateService : Just checked.have to wait for time gap to pass");
            CheckPowerLock.release();
        }
    }

    public void processNewVersion(Update update) {
        Log.d(LogUtil.TAG, "UpdateService : processNewVersion(" + update.versionCode + " , " + update.forceInstall + " , " + "..." + ")");

        if (Settings.getInstance(getApplicationContext()).getBoolean(Settings.KEY_JUST_ON_WIFI, true)) {
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (!wifi.isWifiEnabled()) {
                Log.d(LogUtil.TAG, "UpdateService : processNewVersion : Cannot start download since WIFI IS OFF");
                if (fileDownloader != null)
                    fileDownloader.StopDownload();
                return;
            }
        }

        if (fileDownloader != null) {
            if (fileDownloader.status == FileDownloader.STATUS_RUN && fileDownloader.getFilePath().endsWith(update.versionCode + ".apk")) {
                //agar dasht hamin noskhe ro danlod mikard beza danlod kone karish nadarim
                Log.i(LogUtil.TAG, "version " + update.versionCode + " is downloading.No need to download again.");
                return;
            }
            fileDownloader.StopDownload();
        }
        listener = new DownloadListener() {
            @Override
            public void onDownloadProgress(long total, long downloaded) {
                Log.d(LogUtil.TAG, "onDownloadProgress " + Math.floor(downloaded * 100 / total) + "%");
            }

            @Override
            public void onDownloadCompleted() {
                Log.d(LogUtil.TAG, "onDownloadCompleted");
                UD.eventNewVersionAvailable(getApplicationContext());
            }

            @Override
            public void onDownloadStarted() {
                Log.d(LogUtil.TAG, "onDownloadStarted");
            }

            @Override
            public void onConnected() {
                Log.d(LogUtil.TAG, "onConnected");
            }

            @Override
            public void onDownloadStopped() {
                Log.d(LogUtil.TAG, "onStopped");
            }

            @Override
            public void onDownloadError(ErrorType type) {
                Log.e(LogUtil.TAG, "onDownloadError : " + type.toString());
            }
        };
        Settings settings = Settings.getInstance(getApplicationContext());
        fileDownloader = new FileDownloader(settings.getString(Settings.KEY_DOWNLOAD_URL, "") + "-" + update.versionCode + ".apk", settings.getString(Settings.KEY_BASE_DIR, "") + "/" + update.versionCode + ".apk", update.md5, listener);
        DownloadPowerLock.acquire(getApplicationContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                fileDownloader.startDownload();
                DownloadPowerLock.release();
            }
        }).start();
    }
}
