package com.sgf.kcamera.business.state;

import com.sgf.kcamera.KParams;

import io.reactivex.Observable;

interface IBusinessState {

    Observable<KParams> openCamera(KParams openParams);

    Observable<KParams> startPreview(KParams previewParams);

    Observable<KParams> capture(KParams captureParams);

    Observable<KParams> configCamera(KParams configParams);

    Observable<KParams> closeCamera(KParams closeParams);
}
