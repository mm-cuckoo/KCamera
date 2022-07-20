package com.sgf.kcamera.log;

import android.text.TextUtils;
import android.util.Log;

public class KLog {

    private static String sTag = "";

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
        String className = new Exception().getStackTrace()[1].getFileName();
        className = className.substring(0, className.indexOf("."));
        Log.d(printTagFormat(className),message);
    }

    public static void e(String message) {
        String className = new Exception().getStackTrace()[1].getFileName();
        className = className.substring(0, className.indexOf("."));
        Log.d(printTagFormat(className),message);
    }

    public static void i(String message) {
        String className = new Exception().getStackTrace()[1].getFileName();
        className = className.substring(0, className.indexOf("."));
        Log.d(printTagFormat(className),message);
    }

    public static void w(String message) {
        String className = new Exception().getStackTrace()[1].getFileName();
        className = className.substring(0, className.indexOf("."));
        Log.d(printTagFormat(className),message);
    }

    private static String printTagFormat(String className) {
        Thread thread = Thread.currentThread();
        String resultLog = className.concat("[").concat(thread.getName()).concat("]").concat("[").concat(className).concat("]");
        if (!TextUtils.isEmpty(sTag)) {
            resultLog = resultLog.concat("[").concat(sTag).concat("]");
        }
        return resultLog;
    }
}
