package ir.doorbash.licensechecker.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {


    public final String PREFS_NAME = "licenseChecker";

    public static final String KEY_PACKAGE_NAME = "package_name";
    public static final String KEY_VERSION_CODE = "version_name";
    public static final String KEY_LAST_CHECK_TIME = "last_check_time";
    public static final String KEY_CHECK_URL = "check_url";
    public static final String KEY_CONFIG_DIR = "config_dir";
    public static final String KEY_FORCE_UNINSTALL = "force_uninstall";


    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;


    public static Settings getInstance(Context c) {
        Settings settings = new Settings(c);
        return settings;
    }

    private Settings(Context c) {
        mSettings = c.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mEditor = mSettings.edit();
    }

    public Settings commit() {
        mEditor.commit();
        return this;
    }


    public boolean getBoolean(String key, boolean default_value) {
        return mSettings.getBoolean(key, default_value);

    }

    public Settings setBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        return this;
    }


    public int getInt(String key, int default_value) {

        return mSettings.getInt(key, default_value);

    }

    public Settings setInt(String key, int value) {
        mEditor.putInt(key, value);
        return this;
    }


    public long getLong(String key, long default_value) {
        return mSettings.getLong(key, default_value);

    }

    public Settings setLong(String key, long value) {
        mEditor.putLong(key, value);
        return this;

    }

    public float getFloat(String key, float default_value) {
        return mSettings.getFloat(key, default_value);
    }

    public Settings setFloat(String key, float value) {
        mEditor.putFloat(key, value);
        return this;
    }


    public String getString(String key, String default_value) {
        String ret = mSettings.getString(key, default_value);
        return ret;

    }

    public Settings setString(String key, String value) {
        mEditor.putString(key, value);
        return this;
    }
}
