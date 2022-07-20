package com.sgf.kcamera.utils;

import com.sgf.kcamera.log.KLog;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class RetryWithDelay implements Function<Observable<Throwable>, ObservableSource<Long>> {
    private final int retryDelayMillis;
    private final int retryMaxCount;
    private int retryCount;
    public RetryWithDelay(int retryMaxCount, int retryDelayMillis) {
        this.retryMaxCount = retryMaxCount;
        this.retryDelayMillis = retryDelayMillis;
    }

    @Override
    public ObservableSource<Long> apply(Observable<Throwable> throwableObservable) throws Exception {
        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<Long>>() {
            @Override
            public ObservableSource<Long> apply(Throwable throwable) throws Exception {
                KLog.d("RetryWithDelay retry ==retryCount=>" + retryCount + " msg:" + throwable.getMessage());
                if (checkRetry()) {
                    return Observable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
                }
                return Observable.error(throwable);
            }
        });
    }

    private boolean checkRetry() {
        if (retryCount < retryMaxCount) {
            retryCount ++;
            return true;
        }
        return false;
    }
}
