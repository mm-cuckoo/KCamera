package com.sgf.kcamera.log;

import android.text.TextUtils;
import android.util.Log;

public class KLog {

    private static String sTag = "k-camera";

    private static boolean debug = false;

    public static void setPrintTag(String tag) {
        sTag = tag;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        KLog.debug = debug;
    }

    public static void d(String message) {
        if (!debug) {
            return;
        }
        try {
            String className = new Exception().getStackTrace()[1].getFileName();
            className = className.substring(0, className.indexOf("."));
            Log.d(printTagFormat(className),message);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(printTagFormat(null),message);
        }
    }

    public static void e(String message) {
        try {
            String className = new Exception().getStackTrace()[1].getFileName();
            className = className.substring(0, className.indexOf("."));
            Log.e(printTagFormat(className),message);
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(printTagFormat(null),message);
        }
    }

    public static void i(String message) {
        try {
            String className = new Exception().getStackTrace()[1].getFileName();
            className = className.substring(0, className.indexOf("."));
            Log.i(printTagFormat(className),message);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(printTagFormat(null),message);
        }
    }

    public static void w(String message) {
        try {
            String className = new Exception().getStackTrace()[1].getFileName();
            className = className.substring(0, className.indexOf("."));
            Log.w(printTagFormat(className),message);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(printTagFormat(null),message);
        }
    }

    private static String printTagFormat(String className) {
        String resultLog = sTag;
        if (className != null) {
            try {
                Thread thread = Thread.currentThread();
                resultLog = className.concat("[").concat(thread.getName()).concat("]");
                if (!TextUtils.isEmpty(sTag)) {
                    resultLog = resultLog.concat("[").concat(sTag).concat("]");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultLog;
    }
}
