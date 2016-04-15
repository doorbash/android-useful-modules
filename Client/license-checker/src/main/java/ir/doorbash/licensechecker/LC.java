package ir.doorbash.licensechecker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ir.doorbash.licensechecker.api.Client;
import ir.doorbash.licensechecker.preferences.ConfigFile;
import ir.doorbash.licensechecker.preferences.Settings;
import ir.doorbash.licensechecker.util.LogUtil;
import ir.doorbash.licensechecker.util.lock.CheckPowerLock;

import java.io.File;

/**
 * Created by Milad Doorbash on 4/7/16.
 */
public class LC {

    private static final String EVENT_RECEIVER_ACTION = "LicenseChecker";

    private enum Event {
        EXPIRE_STATUS
    }

    public static void init(Context context, String packageName, int versionCode, String checkUrl, String configDir, boolean forceUninstall) {
        Settings.getInstance(context).setString(Settings.KEY_PACKAGE_NAME, packageName).setInt(Settings.KEY_VERSION_CODE, versionCode).setString(Settings.KEY_CHECK_URL, checkUrl).setString(Settings.KEY_CONFIG_DIR, configDir).setBoolean(Settings.KEY_FORCE_UNINSTALL, forceUninstall).commit();
        new File(configDir).mkdirs();
    }

    private static boolean isConfigured(Context c) {
        return Settings.getInstance(c).getInt(Settings.KEY_VERSION_CODE, 0) > 0;
    }

    public static void checkLicense(final Context c) {
        if (isConfigured(c)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Settings settings = Settings.getInstance(c);
                        boolean expired = Client.checkLicense(settings.getString(Settings.KEY_CHECK_URL, ""), settings.getString(Settings.KEY_PACKAGE_NAME, ""), settings.getInt(Settings.KEY_VERSION_CODE, 0));

                        ConfigFile configFile = new ConfigFile();
                        configFile.setBoolean("expired", expired);
                        configFile.write(Settings.getInstance(c).getString(Settings.KEY_CONFIG_DIR, "") + "/cfg.bin");

                        eventExpired(c, expired);

                        if (expired && settings.getBoolean(Settings.KEY_FORCE_UNINSTALL, false)) {
                            Uri packageUri = Uri.parse("package:" + settings.getString(Settings.KEY_PACKAGE_NAME, ""));
                            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            c.startActivity(uninstallIntent);
                        }

                    } catch (Exception e) {
                        Log.e(LogUtil.TAG, "Client : " + e.getMessage());
                    } finally {
                        CheckPowerLock.release();
                    }
                }
            }).start();
        } else {
            Log.e(LogUtil.TAG, "LicenseChecker has not been initialized yet.");
            CheckPowerLock.release();
        }
    }

    public static boolean isExpired(Context c) {
        if (!isConfigured(c)) {
            Log.e(LogUtil.TAG, "LicenseChecker has not been initialized yet.");
            return false;
        }
        return new ConfigFile(Settings.getInstance(c).getString(Settings.KEY_CONFIG_DIR, "") + "/cfg.bin").getBoolean("expired", false);
    }

    private static void eventExpired(Context c, boolean expired) {
        Intent mPMIntent = new Intent(EVENT_RECEIVER_ACTION);
        mPMIntent.putExtra("event", Event.EXPIRE_STATUS.ordinal());
        mPMIntent.putExtra("expired", expired);
        LocalBroadcastManager.getInstance(c).sendBroadcastSync(mPMIntent);
    }

}
