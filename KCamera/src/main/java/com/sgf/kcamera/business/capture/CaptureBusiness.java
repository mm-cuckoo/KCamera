package com.sgf.kcamera.business.capture;



import com.sgf.kcamera.KParams;

import io.reactivex.Observable;

/**
 * Camera 捕获业务
 */
public interface CaptureBusiness {


    /**
     * 打开Camera
     * @param openParams
     * @return
     */
    Observable<KParams> openCamera(KParams openParams);

    Observable<KParams> configCamera(KParams configParams);

    Observable<KParams> capture(KParams captureParams);

    Observable<KParams> closeCamera(KParams closeParams);

}
