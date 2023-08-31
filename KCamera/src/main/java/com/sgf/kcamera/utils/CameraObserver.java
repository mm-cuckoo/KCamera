package com.sgf.kcamera.utils;


import com.sgf.kcamera.BuildConfig;
import com.sgf.kcamera.log.KLog;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class CameraObserver<T> implements Observer<T> {
    private static final String TAG = "CameraObserver";
    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(@NonNull T t) {

    }

    @Override
    public void onError(@NonNull Throwable e) {
        KLog.e(TAG, e.getMessage());
        e.printStackTrace();
        if (BuildConfig.DEBUG) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onComplete() {

    }
}
