package com.sgf.kcamera.camera.device;


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
     * @return
     */
    Observable<KParams> openCameraDevice(KParams openParam);

    /**
     * Camera 线程调度器，该调度器负责Camera  device ,session 的执行
     * @return
     */
    Scheduler getCameraScheduler();

    /**
     * 获取一个锁
     *
     * 如果 6s 没有获取成功， 会抛出一个异常：Time out waiting to lock camera opening.
     * @throws InterruptedException
     */
    void lock() throws InterruptedException;

    /**
     * 释放获取的锁
     */
    void unlock();

    /**
     * 关闭Camera device
     * @param closeParam ： 关闭 Camera device 参数
     * @return 关闭结果code
     * code 说明：
     * KParams.Value.CLOSE_STATE.DEVICE_NULL 表示device 为 null , 没有进行实际的关闭动作
     * KParams.Value.CLOSE_STATE.DEVICE_CLOSED 表示正常关闭device
     */
    int closeCameraDevice(KParams closeParam);

}
