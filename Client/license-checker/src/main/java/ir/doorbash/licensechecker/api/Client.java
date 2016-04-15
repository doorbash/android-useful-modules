package ir.doorbash.licensechecker.api;

import android.util.Log;

import ir.doorbash.licensechecker.util.LogUtil;
import ir.doorbash.licensechecker.util.Security;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by Milad Doorbash on 6/3/2015.
 */
public class Client {


    public static boolean checkLicense(String url, String packageName, int currentVersion) throws Exception {
        Log.d(LogUtil.TAG, "Client.checkLicense()");
        JSONObject res = requestFunction(url + "/" + packageName + "/" + currentVersion + "/" + Security.generateRand(15) + "/");
        if (res.has("ex") && res.getInt("ex") == 1) {
            Log.d(LogUtil.TAG, "Client : Oops! the application cannot continue to work :(");
            return true;
        }
        Log.d(LogUtil.TAG, "Client : App can continue to work :)");
        return false;
    }

    private static JSONObject requestFunction(String url) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).connectTimeout(30000, TimeUnit.MILLISECONDS).readTimeout(30000, TimeUnit.MILLISECONDS).build();
        JSONObject ret = new JSONObject(client.newCall(new Request.Builder().url(url).get().build()).execute().body().string());
        if (!ret.getString("result").equals("ok")) throw new Exception("error");
        return ret;
    }

}