package com.sgf.kcamera.camera.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import com.sgf.kcamera.KException;
import com.sgf.kcamera.KParams;
import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.utils.RetryWithDelay;
import com.sgf.kcamera.utils.WorkerHandlerManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;

/**
 * 在这里维护所有已经开启的 open 的 camera device
 * 只负责 camera device 的开启和 关闭
 * */
public class KCameraDeviceImpl implements KCameraDevice {

    private volatile static KCameraDevice sInstance;
    /**
     * 每次打开Camera 的签名，用于关闭时校验使用
     */
    private volatile Long mOpenCameraSign = 0L;
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private final CameraManager mCameraManager;
    /**
     * 保存已经打开的camera device
     */
    private final Map<String, CameraDevice> mDeviceMap = new HashMap<>();
    private final Scheduler mCameraScheduler;

    private KCameraDeviceImpl(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mCameraScheduler = WorkerHandlerManager.getScheduler(WorkerHandlerManager.Tag.T_TYPE_CAMERA);
    }

    public static KCameraDevice getsInstance(Context context) {
        if (sInstance == null) {
            synchronized (KCameraDeviceImpl.class) {
                if (sInstance == null) {
                    sInstance = new KCameraDeviceImpl(context);
                }
            }
        }
        return sInstance;
    }

    @Override
    public Scheduler getCameraScheduler() {
        return mCameraScheduler;
    }

    @Override
    public void lock() throws InterruptedException {
        KLog.i( "lock====>");
        if (!mCameraOpenCloseLock.tryAcquire(6000, TimeUnit.MILLISECONDS)) {
            KLog.e("Time out waiting to lock camera opening");
            throw new RuntimeException("Time out waiting to lock camera opening.");
        }
        KLog.i( "lock====>end");
    }

    @Override
    public void unlock() {
        KLog.i( "unlock====>");
        mCameraOpenCloseLock.release();
    }

    @SuppressLint("MissingPermission")
    @Override
    public Observable<KParams> openCameraDevice(final KParams openCameraParams) {
        final String cameraId = openCameraParams.get(KParams.Key.CAMERA_ID);
        return Observable.create((ObservableOnSubscribe<KParams>) emitter -> {
            try {
                lock();
                mOpenCameraSign = System.currentTimeMillis(); // 使用时间戳生成打开签名
                KLog.i( "open camera==> mOpenCameraSign：" + mOpenCameraSign + "params:" + openCameraParams);
                mCameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        // 保存当前打开的camera device
                        mDeviceMap.put(camera.getId(), camera);
                        KLog.i( "camera opened ==》camera id: "  + camera.getId()  + "  sign:" + mOpenCameraSign);
                        openCameraParams.put(KParams.Key.CAMERA_DEVICE, camera);
                        openCameraParams.put(KParams.Key.OPEN_CAMERA_STATE, KParams.Value.CAMERA_OPEN_SUCCESS);
                        openCameraParams.put(KParams.Key.OPEN_CAMERA_SIGN, mOpenCameraSign);
                        emitter.onNext(openCameraParams);
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        KLog.e("onDisconnected: ");
                        camera.close();
                        KParams resultParams = new KParams();
                        resultParams.put(KParams.Key.OPEN_CAMERA_STATE, KParams.Value.CAMERA_DISCONNECTED);
                        emitter.onNext(resultParams);
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        KLog.e("onError: code:" + error);
                        camera.close();
                        emitter.onError(new KException("camera onError code:", error));
                    }

                    @Override
                    public void onClosed(@NonNull CameraDevice camera) {
                        KLog.d("camera closed");
                    }
                }, null);
            } catch (CameraAccessException | InterruptedException e) {
                KLog.e("open camera Exception :" + Arrays.toString(e.getStackTrace()));
                KParams resultParams = new KParams();
                resultParams.put(KParams.Key.OPEN_CAMERA_STATE, KParams.Value.CAMERA_OPEN_EXCEPTION);
                emitter.onNext(resultParams);
            } finally {
                unlock();
            }
        });
    }

    @Override
    public int closeCameraDevice(final KParams closeParam) {
        int closeResult = KParams.Value.CLOSE_STATE.DEVICE_NULL;
        try {
            lock();
            Long openCameraSign = closeParam.get(KParams.Key.OPEN_CAMERA_SIGN, 0L);
            String cameraId = closeParam.get(KParams.Key.CAMERA_ID, null);
            KLog.i("start close camera id " + cameraId + " close sign:" + openCameraSign  + "  open sign:" + mOpenCameraSign);
            if (openCameraSign != 0L && !openCameraSign.equals(mOpenCameraSign)) {
                KLog.e("close camera device check sign fail");
                return closeResult;
            }

            CameraDevice cameraDevice = null;
            if (mDeviceMap.containsKey(cameraId)) {
                cameraDevice = mDeviceMap.remove(cameraId);
            }


            if (cameraDevice != null) {
                cameraDevice.close();
                closeResult = KParams.Value.CLOSE_STATE.DEVICE_CLOSED;
                KLog.i("close camera device : id :" + cameraId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            KLog.i("end close camera method ");
            unlock();
        }
        return closeResult;
    }
}
