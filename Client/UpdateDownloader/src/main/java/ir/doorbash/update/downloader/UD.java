package ir.doorbash.update.downloader;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;

import ir.doorbash.update.downloader.api.Client;
import ir.doorbash.update.downloader.model.Update;
import ir.doorbash.update.downloader.preferences.Settings;
import ir.doorbash.update.downloader.service.UpdateService;
import ir.doorbash.update.downloader.util.FileUtil;
import ir.doorbash.update.downloader.util.LogUtil;
import ir.doorbash.update.downloader.util.lock.CheckPowerLock;

/**
 * Created by Milad Doorbash on 3/15/16.
 */
public class UD {

    public static final String EVENT_RECEIVER_ACTION = "UpdateDownloader";

    public enum Event {
        NEW_VERSION_AVAILABE
    }

    public static void init(Context context, int currentVersion, String checkUrl, String downloadUrl, String baseDir, boolean justOnWifi) {
        Settings.getInstance(context).setInt(Settings.KEY_CURRENT_VERSION, currentVersion).setString(Settings.KEY_BASE_DIR, baseDir).setString(Settings.KEY_DOWNLOAD_URL, downloadUrl).setString(Settings.KEY_CHECK_URL, checkUrl).setBoolean(Settings.KEY_JUST_ON_WIFI, justOnWifi).commit();
        deletePrevVersionAPKs(baseDir,currentVersion);
    }

    private static boolean isConfigured(Context c) {
        return Settings.getInstance(c).getInt(Settings.KEY_CURRENT_VERSION, 0) > 0;
    }

    public static void checkNewVersion(final Context c) {
        if (isConfigured(c)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Settings settings = Settings.getInstance(c);
                        Update update = Client.checkNewVersion(settings.getString(Settings.KEY_CHECK_URL, ""), settings.getInt(Settings.KEY_CURRENT_VERSION, 0));
                        if (update != null) {
                            Intent i = new Intent(c, UpdateService.class);


                            Settings.getInstance(c)
                                    .setInt(Settings.KEY_NEW_VERSION_CODE, update.versionCode)
                                    .setBoolean(Settings.KEY_NEW_VERSION_FORCE_INSTALL, update.forceInstall)
                                    .setString(Settings.KEY_NEW_VERSION_LAST_CHANGES, update.lastChanges)
                                    .setInt(Settings.KEY_NEW_VERSION_FILESIZE, update.fileSize)
                                    .setString(Settings.KEY_NEW_VERSION_NAME, update.name)
                                    .setString(Settings.KEY_NEW_VERSION_MD5, update.md5)
                                    .commit();

                            i.putExtra("action", UpdateService.ACTION_DOWNLOAD).putExtra("version", update.versionCode).putExtra("forceInstall", update.forceInstall);
                            i.putExtra("lastChanges", update.lastChanges);
                            i.putExtra("filesize", update.fileSize);
                            i.putExtra("name", update.name);
                            i.putExtra("md5", update.md5);
                            c.startService(i);
                        }
                    } catch (Exception e) {
                        Log.e(LogUtil.TAG, "Client : " + e.getMessage());
                    } finally {
                        CheckPowerLock.release();
                    }
                }
            }).start();
        } else {
            Log.e(LogUtil.TAG, "UD is not configured yet.");
            CheckPowerLock.release();
        }
    }

    public static Update isUpdateAvailable(Context c) {
        if(!isConfigured(c)) return null;
        Settings settings = Settings.getInstance(c);
        int newVersionCode = settings.getInt(Settings.KEY_NEW_VERSION_CODE, 0);
        int currentVersion = settings.getInt(Settings.KEY_CURRENT_VERSION, 0);
        if (newVersionCode == 0) return null;
        if (newVersionCode <= currentVersion) return null;
        String filePath = settings.getString(Settings.KEY_BASE_DIR, "") + "/" + newVersionCode + ".apk";
        File file = new File(filePath);
        if (file.exists() && file.length() > 0 && file.length() == settings.getInt(Settings.KEY_NEW_VERSION_FILESIZE, 0) && FileUtil.getMD5Checksum(filePath).equals(settings.getString(Settings.KEY_NEW_VERSION_MD5, ""))) {
            return new Update(settings.getInt(Settings.KEY_NEW_VERSION_CODE, 0), settings.getBoolean(Settings.KEY_NEW_VERSION_FORCE_INSTALL, false), settings.getString(Settings.KEY_NEW_VERSION_LAST_CHANGES, ""), settings.getInt(Settings.KEY_NEW_VERSION_FILESIZE, 0), settings.getString(Settings.KEY_NEW_VERSION_NAME, ""), settings.getString(Settings.KEY_NEW_VERSION_MD5, ""));
        }
        return null;
    }

    public static void eventNewVersionAvailable(Context c) {
        Update update = isUpdateAvailable(c);
        if (update != null) {
            Intent mPMIntent = new Intent(EVENT_RECEIVER_ACTION);
            mPMIntent.putExtra("event", Event.NEW_VERSION_AVAILABE.ordinal());
            mPMIntent.putExtra("version", update.versionCode);
            mPMIntent.putExtra("forceInstall", update.forceInstall);
            mPMIntent.putExtra("lastChanges", update.lastChanges);
            mPMIntent.putExtra("filesize", update.fileSize);
            mPMIntent.putExtra("name", update.name);
            mPMIntent.putExtra("md5", update.md5);
            LocalBroadcastManager.getInstance(c).sendBroadcastSync(mPMIntent);
        }
    }

    public static String getUpdateFilePath(Context c, int version) {
        return Settings.getInstance(c).getString(Settings.KEY_BASE_DIR, "") + "/" + version + ".apk";
    }

    public static void deletePrevVersionAPKs(String baseDir,int currentVersion) {
        for (int i = 0; i <= currentVersion; i++) {
            new File(baseDir + "/" +  i  + ".apk").delete();
        }
    }


}
