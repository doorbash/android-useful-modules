package ir.doorbash.update.downloader.api;

import android.util.Log;


import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import ir.doorbash.update.downloader.model.Update;
import ir.doorbash.update.downloader.util.LogUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by mi1s0n on 6/3/2015.
 */
public class Client {


    public static Update checkNewVersion(String url, int currentVersion) throws Exception {

        Log.d(LogUtil.TAG, "Client.checkNewVersion()");

        JSONObject res = requestFunction(url);

        if (res.getInt("code") > currentVersion) {
            Log.d(LogUtil.TAG, "Client : new version available!");
            return new Update(res.getInt("code"), res.getInt("forceInstall") > 0, res.getString("lastChanges"),res.getInt("filesize"),res.getString("name"),res.getString("md5"));
        }
        Log.d(LogUtil.TAG, "Client : already up-to-date : " + res.getInt("code") + " ::: "+  (res.getInt("forceInstall") > 0) + " ::: " + res.getString("lastChanges"));
        return null;
    }


    private static JSONObject requestFunction(String url) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).connectTimeout(30000, TimeUnit.MILLISECONDS).readTimeout(30000, TimeUnit.MILLISECONDS).build();
        JSONObject ret = new JSONObject(client.newCall(new Request.Builder().url(url).get().build()).execute().body().string());
        if (!ret.getString("result").equals("ok")) throw new Exception("error");
        return ret;
    }

}