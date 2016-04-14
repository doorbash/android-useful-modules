package ir.doorbash.useful.modules;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

import ir.doorbash.licensechecker.LC;
import ir.doorbash.update.downloader.UD;
import ir.doorbash.update.downloader.model.Update;

public class MainActivity extends AppCompatActivity {

    Dialog updateDialog;
    public boolean forceInstallUpdate = false;
    UpdateEventListener updateEventListener = new UpdateEventListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLicenseChecker();
        initUpdateDownloader();
    }

    public void initLicenseChecker() {
        LC.init(this, getPackageName(), BuildConfig.VERSION_CODE, "http://example.com/api/apps/license", Environment.getExternalStorageDirectory().getAbsolutePath() + "/.some/.place/.to/.hide/.data", true);
        if (LC.isExpired(this)) {
            new AlertDialog.Builder(this).setMessage("Sorry, You cannot use this app anymore.Please contact developer, maybe they know what's going on!").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create().show();
        }
    }


    public void showUpdateDialog(final Update update) {
        if (updateDialog != null && updateDialog.isShowing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Update Available!").setMessage("Name: " + update.name + "\n" + "Code: " + update.versionCode + "\n\n" + update.lastChanges).setPositiveButton("Install", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                forceInstallUpdate = update.forceInstall;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(UD.getUpdateFilePath(MainActivity.this, update.versionCode))), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        if (!update.forceInstall) {
            builder = builder.setNegativeButton("Cancel", null).setCancelable(true);
        } else {
            builder = builder.setCancelable(false);
        }
        updateDialog = builder.create();
        updateDialog.show();
    }

    private void initUpdateDownloader() {
        try {
            String checkUrl = "http://example.com/api/apps/version/check/" + getPackageName() + "/" + Math.floor(Math.random() * 1000000) + "/";
            String downloadUrl = "http://dl.example.com/apk/release";
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Client/apk";
            File baseDirFile = new File(baseDir);
            baseDirFile.mkdirs();
            UD.init(this, getPackageManager().getPackageInfo(getPackageName(), 0).versionCode, checkUrl, downloadUrl, baseDir, true);
            Update update = UD.isUpdateAvailable(this);
            if (update != null) {
                showUpdateDialog(update);
            }

            try {
                LocalBroadcastManager.getInstance(this).
                        registerReceiver(updateEventListener, new IntentFilter(UD.EVENT_RECEIVER_ACTION));
            } catch (Exception e) {
            }

        } catch (Exception e) {

        }
    }


    public class UpdateEventListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            UD.Event event = UD.Event.values()[intent.getIntExtra("event", 0)];

            switch (event) {
                case NEW_VERSION_AVAILABE:

                    final Update update = new Update(intent.getIntExtra("version", 0), intent.getBooleanExtra("forceInstall", false), intent.getStringExtra("lastChanges"), intent.getIntExtra("filesize", 0), intent.getStringExtra("name"), intent.getStringExtra("md5"));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showUpdateDialog(update);
                        }
                    });
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        if (forceInstallUpdate) {
            Update update = UD.isUpdateAvailable(this);
            if (update != null) {
                showUpdateDialog(update);
            }
        }
        super.onResume();
    }
}
