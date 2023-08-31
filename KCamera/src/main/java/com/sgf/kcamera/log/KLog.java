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

    public static void d(String tag, String message) {
        if (!debug) {
            return;
        }
        Log.d(printTagFormat(tag), message);
    }

    public static void e(String tag, String message) {
        Log.e(printTagFormat(tag),message);
    }

    public static void i(String tag, String message) {
        Log.i(printTagFormat(tag),message);
    }

    public static void w(String tag, String message) {
        Log.w(printTagFormat(tag),message);
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
        } else {
            try {
                Thread thread = Thread.currentThread();
                resultLog = "".concat("[").concat(thread.getName()).concat("]");
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
