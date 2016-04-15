package ir.doorbash.update.downloader.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mi1s0n on 5/1/2015.
 */
public class FileDownloader {

    public static final int STATUS_INIT = 0;
    public static final int STATUS_RUN = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_STOP = 3;
    public static final int STATUS_COMPLETED = 4;

    public int status = STATUS_INIT;

    private String fileUrl;
    private String path;
    private DownloadListener listener;
    private HttpURLConnection connection;
    private BufferedOutputStream output;
    private boolean forceStop = false;
    private long download = 0;
    private long total = 0;
    private String md5;


    public FileDownloader(final String fileUrl, String path, String md5, final DownloadListener listener) {
        this.fileUrl = fileUrl;
        this.path = path;
        this.listener = listener;
        this.md5 = md5;
    }

    public void StopDownload() {
        this.forceStop = true;
    }

    public String getFilePath() {
        return path;
    }


    public void startDownload() {


        final File file = new File(path);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
            }

        }

        if (!file.exists()) {
            status = STATUS_ERROR;
            listener.onDownloadError(DownloadListener.ErrorType.IO_ERROR);
            return;
        }

        if (listener == null) return;


        try {

            if (forceStop) {
                status = STATUS_STOP;
                listener.onDownloadStopped();
                return;
            }

            listener.onDownloadStarted();


            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url
                    .openConnection();
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
//                connection.setRequestProperty("User-Agent",
//                        "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");


            connection.connect();

            int statusCode = connection.getResponseCode();

            if (statusCode != 200) {
                status = STATUS_ERROR;
                listener.onDownloadError(DownloadListener.ErrorType.IO_ERROR);
                return;
            }

            total = connection.getContentLength();

            connection.disconnect();

            if (total == -1) {
                status = STATUS_ERROR;
                listener.onDownloadError(DownloadListener.ErrorType.IO_ERROR);
                return;
            }

            if (file.length() == total && FileUtil.getMD5Checksum(file.getAbsolutePath()).equals(md5)) {
                status = STATUS_COMPLETED;
                Log.d(LogUtil.TAG, "FileDownloader : DownloadCompleted! " + FileUtil.getMD5Checksum(file.getAbsolutePath()) + " == " + md5);
                listener.onDownloadCompleted();
                return;
            }

            if (file.length() > total) {
                status = STATUS_ERROR;
                file.delete();
                listener.onDownloadError(DownloadListener.ErrorType.IO_ERROR);
            }


            File parent = file.getParentFile();
            if (parent != null && FileUtil.bytesAvailable(parent) < total) {
                status = STATUS_ERROR;
                listener.onDownloadError(DownloadListener.ErrorType.NO_ENOUGH_SPACE_ON_DISK);
                return;
            }

            listener.onConnected();

            if (file.exists()) {
                download = file.length();
            } else {
                download = 0;
            }


            if (forceStop) {
                listener.onDownloadStopped();
                return;
            }


            url = new URL(fileUrl);
            connection = (HttpURLConnection) url
                    .openConnection();
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
//                connection
//                        .setRequestProperty("User-Agent",
//                                "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
            connection.setRequestProperty("Range",
                    "bytes=" + (file.length()) + "-" + (total - 1));

            connection.connect();


            long contentLength = connection.getContentLength();

            if (contentLength != (total - file.length())) {
                status = STATUS_ERROR;
                listener.onDownloadError(DownloadListener.ErrorType.IO_ERROR);
                return;
            }


            statusCode = connection.getResponseCode();

            if (statusCode != 206) {
                status = STATUS_ERROR;
                listener.onDownloadError(DownloadListener.ErrorType.IO_ERROR);
            }

            // download the file
            BufferedInputStream input = new BufferedInputStream(
                    connection.getInputStream());

            int BUFFER_SIZE = 300 * 1024;

            output = new BufferedOutputStream(new FileOutputStream(file, true),
                    BUFFER_SIZE);

            byte data[] = new byte[BUFFER_SIZE];
            int count;


            int cc = 5;

            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
                output.flush();
                download += count;
                if (forceStop) {
                    status = STATUS_STOP;
                    listener.onDownloadStopped();
                    return;
                }


                cc++;

                if (cc < 5) {
                    continue;
                }

                cc = 0;

                status = STATUS_RUN;
                listener.onDownloadProgress(total, download);

            }

            if (forceStop) {
                status = STATUS_STOP;
                output.flush();
                output.close();
                input.close();
                connection.disconnect();
                listener.onDownloadStopped();
                return;
            }


            try {
                output.flush();
                output.close();
                input.close();
                connection.disconnect();
            } catch (Exception eee) {
            }

            if (file.length() == total && FileUtil.getMD5Checksum(file.getAbsolutePath()).equals(md5)) {
                status = STATUS_COMPLETED;
                Log.d(LogUtil.TAG, "FileDownloader : DownloadCompleted! " + FileUtil.getMD5Checksum(file.getAbsolutePath()) + " == " + md5);
                listener.onDownloadCompleted();
            } else {
                status = STATUS_ERROR;
                Log.d(LogUtil.TAG, "FileDownloader : maybe " + file.length() + " != " + total + " or " + FileUtil.getMD5Checksum(file.getAbsolutePath()) + " != " + md5);
                file.delete();
                listener.onDownloadError(DownloadListener.ErrorType.IO_ERROR);
            }

            return;

        } catch (Exception e) {
            e.printStackTrace();
            status = STATUS_ERROR;
            listener.onDownloadError(DownloadListener.ErrorType.IO_ERROR);
            return;
        }
    }


}
