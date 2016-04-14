package ir.doorbash.licensechecker.util;

import java.security.MessageDigest;


public class Security {

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    private static final String randomChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public static String md5(String str) {
        try {
            byte[] bytesOfMessage = str.getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(bytesOfMessage);
            return bytesToHex(thedigest);

        } catch (Exception e) {
            return null;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String generateRand(int r) {
        String ret = "";
        for (int i = 0; i < r; i++) {
            ret += randomChars.charAt((int) (Math.random() * randomChars
                    .length()));
        }
        return ret;
    }


}
