package ir.doorbash.update.downloader.util;

/**
 * Created by mi1s0n on 5/1/2015.
 */
public interface DownloadListener {


    public enum ErrorType {IO_ERROR,NO_ENOUGH_SPACE_ON_DISK};

    void onDownloadProgress(long total, long downloaded);
    void onDownloadCompleted();
    void onDownloadStarted();
    void onConnected();
    void onDownloadStopped();
    void onDownloadError(ErrorType type);

}


