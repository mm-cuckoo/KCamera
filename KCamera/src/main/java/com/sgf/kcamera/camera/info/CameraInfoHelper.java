package com.sgf.kcamera.camera.info;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

/**
 * Camera 信息帮助对象
 * 该对象是单例对象
 * 读取设备Camera 信息并使用CameraInfo 进出管理
 */
public class CameraInfoHelper {
    private static final CameraInfoHelper sCameraInfoHelper = new CameraInfoHelper();

    private final Object obj = new Object();
    private CameraManager mCameraManager;
    private volatile boolean mIsLoadFinish = false;
    private final Map<String, CameraInfo> mCameraInfoMap = new HashMap<String, CameraInfo>();

    public static CameraInfoHelper getInstance() {
        return sCameraInfoHelper;
    }

    private CameraInfoHelper() {
    }

    /**
     * 加载设备中Camera  信息
     * @param context
     * @param handler ： 加载设备信息时运行的线程，如果设置为 null 运行在当前线程中
     */
    public void load(Context context, Handler handler) {
        if (mIsLoadFinish) {
            return;
        }
        mCameraManager = (CameraManager) context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        LoadCameraInfoRunnable runnable = new LoadCameraInfoRunnable();
        if (handler ==null) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    /**
     * 获取指定 Camera id 的信息
     * @param cameraId ： 指定Camera id
     * @return : 指定Camera Id 的  CameraInfo 实力
     */
    public CameraInfo getCameraInfo(String cameraId) {
        checkLoadFinish();
        return mCameraInfoMap.get(cameraId);
    }

    /**
     * 检查是否加载完毕设备中所有 Camera Id 信息
     *
     * 注意：如果没有加载完成，会阻塞执行
     */
    private void checkLoadFinish() {
        if (!mIsLoadFinish) {
            synchronized (obj) {
                if (!mIsLoadFinish) {
                    try {
                        obj.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class LoadCameraInfoRunnable implements Runnable {
        @Override
        public void run() {
            synchronized (obj) {
                try {
                    for (String cameraId : mCameraManager.getCameraIdList()) {
                        CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                        mCameraInfoMap.put(cameraId, new CameraInfo(cameraId, cameraCharacteristics));
                    }
                    mIsLoadFinish = true;
                    obj.notifyAll();
                } catch (CameraAccessException e) {
                    obj.notifyAll();
                    e.printStackTrace();
                }
            }
        }
    }
}
