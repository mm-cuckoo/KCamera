package com.sgf.kcamera.camera.device;


import android.hardware.camera2.CameraDevice;

import com.sgf.kcamera.KParams;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

/**
 * Camera Device 控制
 */
public interface KCameraDevice {

    /**
     * 打开 camera device
     * @param openParam : 打开 camera device 参数
     */
    Observable<KParams> openCameraDevice(KParams openParam);

    CameraDevice getCameraDevice(KParams params);

    /**
     * Camera 线程调度器，该调度器负责Camera  device ,session 的执行
     */
    Scheduler getCameraScheduler();


    /**
     * 关闭Camera device
     * @param closeParam ： 关闭 Camera device 参数
     *                   在关闭camera device 时要注意一下几种情况
     *                   1. sign = 0 和 cameraId = null时
     *                   关闭所有已经打开的camera device
     *                   2. sign = 0 , cameraId 不为null 时
     *                   关闭指定cameraId 的camera device
     *                   3. sign 不为 0 ， cameraId 不为null 时
     *                   如果当前已经打开的camera sign 和需要关闭camera id 对应的sign 相同，
     *                   则关闭对应的cameraId 的 device
     */
    Observable<KParams> closeCameraDevice(KParams closeParam);


}
