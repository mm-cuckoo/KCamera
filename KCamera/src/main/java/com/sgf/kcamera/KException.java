package com.sgf.kcamera;

import androidx.annotation.IntRange;

public class KException extends Exception {
    public final int code;
    public final KParams params;

    private static final int CODE_DEF = -1;
    public static final int CODE_CAMERA_IN_USE = 1;
    public static final int CODE_MAX_CAMERAS_IN_USE = 2;
    public static final int CODE_CAMERA_DISABLED = 3;
    public static final int CODE_CAMERA_DEVICE = 4;
    public static final int CODE_CAMERA_SERVICE = 5;
    public static final int CODE_CAMERA_DISCONNECTED = 6;
    public static final int CODE_CAMERA_REQUEST_LOCK_FAIL = 7;



    public KException(String message) {
        super(message);
        this.code = CODE_DEF;
        this.params = null;
    }

    public KException(String message, @IntRange(from = 0, to = 9999) int code) {
        super(message);
        this.code = code;
        this.params = null;
    }

    public KException(String message, Throwable throwable) {
        super(message, throwable);
        this.code = CODE_DEF;
        this.params = null;
    }

    public KException(String message, int code, Throwable throwable) {
        super(message, throwable);
        this.code = code;
        this.params = null;
    }

    public KException(String message, int code, Throwable throwable, KParams params) {
        super(message, throwable);
        this.code = code;
        this.params = params;
    }

    public KException(String message, KParams params) {
        super(message);
        this.params = params;
        this.code = 0;
    }

    public KException(String message, int code, KParams params) {
        super(message);
        this.params = params;
        this.code = code;
    }

}
