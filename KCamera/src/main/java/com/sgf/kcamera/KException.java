package com.sgf.kcamera;

public class KException extends Exception {
    public final int errorCode;
    public final KParams params;

    public KException(String errorMsg) {
        super(errorMsg);
        this.errorCode = 0;
        this.params = null;
    }

    public KException(String errorMsg, int errorCode) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.params = null;
    }

    public KException(String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
        this.errorCode = 0;
        this.params = null;
    }

    public KException(String errorMsg, int errorCode, Throwable throwable) {
        super(errorMsg, throwable);
        this.errorCode = errorCode;
        this.params = null;
    }

    public KException(String errorMsg, int errorCode, Throwable throwable, KParams params) {
        super(errorMsg, throwable);
        this.errorCode = errorCode;
        this.params = params;
    }

    public KException(String errorMsg, KParams params) {
        super(errorMsg);
        this.params = params;
        this.errorCode = 0;
    }

    public KException(String errorMsg, int errorCode, KParams params) {
        super(errorMsg);
        this.params = params;
        this.errorCode = errorCode;
    }
}
