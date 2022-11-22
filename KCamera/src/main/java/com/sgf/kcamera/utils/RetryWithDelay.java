package com.sgf.kcamera.utils;

import com.sgf.kcamera.KException;
import com.sgf.kcamera.log.KLog;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public abstract class RetryWithDelay implements Function<Observable<Throwable>, ObservableSource<Long>> {
    private final int retryDelayMillis;
    private final int retryMaxCount;
    private int retryCount;
    public RetryWithDelay(int retryMaxCount, int retryDelayMillis) {
        this.retryMaxCount = retryMaxCount;
        this.retryDelayMillis = retryDelayMillis;
    }

    @Override
    public ObservableSource<Long> apply(Observable<Throwable> throwableObservable) {
        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<Long>>() {
            @Override
            public ObservableSource<Long> apply(Throwable throwable) {
                KLog.d("RetryWithDelay retry method ");
                if (throwable instanceof KException) {
                    int code = ((KException)throwable).code;
                    KLog.i("RetryWithDelay retry ==retryCount=>" + retryCount + " msg:" + throwable.getMessage() + "  code:" + code);
                    if (checkRetry(code)) {
                        return Observable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
                    }
                }
                return Observable.error(throwable);
            }
        });
    }

    /**
     * 检查重试条件
     * @param code : 错误码
     */
    private boolean checkRetry(int code) {
        boolean isTry = isTry(code);
        if (retryCount < retryMaxCount && isTry) {
            retryCount ++;
            return true;
        }
        return false;
    }

    /**
     * 重试条件
     * @param code ： 错误码
     */
    public abstract boolean isTry(int code);
}
