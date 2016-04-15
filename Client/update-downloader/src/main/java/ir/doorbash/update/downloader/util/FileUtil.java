package ir.doorbash.update.downloader.util;

import android.os.StatFs;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Created by mi1s0n on 5/1/2015.
 */
public class FileUtil {

    public static long bytesAvailable(File f) {
        StatFs stat = new StatFs(f.getPath());
        long bytesAvailable = (long) stat.getBlockSize()
                * (long) stat.getAvailableBlocks();
        return bytesAvailable;
    }

    public static long bytesTotal(File f) {
        StatFs stat = new StatFs(f.getPath());
        long bytesAvailable = (long) stat.getBlockSize()
                * (long) stat.getBlockCount();
        return bytesAvailable;
    }

    public static String bytesToMegabytes(long b) {
        if (b >= 1048576000)
            return String.format("%.2f", (((float) b) / 1073741824)) + "GB";
        if (b >= 1024000)
            return String.format("%.2f", (((float) b) / 1048576)) + "MB";
        return String.format("%.2f", (((float) b) / 1024)) + "KB";
    }

    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) {
        try {
            byte[] b = createChecksum(filename);
            String result = "";

            for (int i = 0; i < b.length; i++) {
                result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
            }
            return result;
        }
        catch (Exception e)
        {

        }
        return "NO_MD5";
    }


}
