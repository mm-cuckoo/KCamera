package com.sgf.kcamera.camera.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import androidx.annotation.NonNull;

import com.sgf.kcamera.KException;
import com.sgf.kcamera.KParams;
import com.sgf.kcamera.log.KLog;
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
    private final Scheduler mCameraCloseScheduler;


    private KCameraDeviceImpl(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mCameraScheduler = WorkerHandlerManager.getScheduler(WorkerHandlerManager.Tag.T_TYPE_CAMERA);
        mCameraCloseScheduler = WorkerHandlerManager.getScheduler(WorkerHandlerManager.Tag.T_TYPE_CLOSE_CAMERA);
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
    public Scheduler getCameraScheduler(int schedulerType) {
        if (schedulerType == KCameraDevice.SCHEDULER_TYPE_CLOSE_CAMERA) {
            //当close camera 时使用单独的 Scheduler 防止在相同线程中在tryAcquire时发生阻塞情况
            return mCameraCloseScheduler;
        }
        return mCameraScheduler;
    }

    @Override
    public boolean requestLock() throws InterruptedException {
        KLog.i( "lock====>");
        if (!mCameraOpenCloseLock.tryAcquire(6000, TimeUnit.MILLISECONDS)) {
            KLog.e("Time out waiting to lock camera opening");
            return false;
        }
        KLog.i( "lock====>end");
        return true;
    }

    @Override
    public void releaseLock() {
        KLog.i( "unlock====>");
        mCameraOpenCloseLock.release();
    }

    @SuppressLint("MissingPermission")
    @Override
    public Observable<KParams> openCameraDevice(final KParams openCameraParams) {
        final String cameraId = openCameraParams.get(KParams.Key.CAMERA_ID);
        return Observable.create((ObservableOnSubscribe<KParams>) emitter -> {
            try {
                boolean isGetLock = requestLock();
                if (isGetLock) {
                    mOpenCameraSign = System.currentTimeMillis(); // 使用时间戳生成打开签名
                    KLog.i( "open camera==> mOpenCameraSign：" + mOpenCameraSign + "params:" + openCameraParams);
                    mCameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            // 保存当前打开的camera device
                            mDeviceMap.put(camera.getId(), camera);
                            KLog.i( "camera opened ==》camera id: "  + camera.getId()  + "  sign:" + mOpenCameraSign);
                            releaseLock();
                            openCameraParams.put(KParams.Key.CAMERA_DEVICE, camera);
                            openCameraParams.put(KParams.Key.OPEN_CAMERA_STATE, KParams.Value.CAMERA_OPEN_SUCCESS);
                            openCameraParams.put(KParams.Key.OPEN_CAMERA_SIGN, mOpenCameraSign);
                            emitter.onNext(openCameraParams);
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            KLog.e("onDisconnected: ");
                            camera.close();
                            releaseLock();
                            KParams resultParams = new KParams();
                            resultParams.put(KParams.Key.OPEN_CAMERA_STATE, KParams.Value.CAMERA_DISCONNECTED);
                            emitter.onNext(resultParams);
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            KLog.e("onError: code:" + error);
                            camera.close();
                            releaseLock();
                            emitter.onError(new KException("camera onError code:", error));
                        }

                        @Override
                        public void onClosed(@NonNull CameraDevice camera) {
                            KLog.d("camera closed");
                        }
                    }, null);
                } else  {
                    // 如果没有获取到lock拍抛出异常执行重试逻，如果重试之后依然失败，请在回调中处理异常，处理异常去吧
                    emitter.onError(new KException("camera request lock fail"));
                }
            } catch (CameraAccessException | InterruptedException e) {
                if (e instanceof CameraAccessException) {
                    releaseLock();
                }
                KLog.e("open camera Exception :" + Arrays.toString(e.getStackTrace()));
            } finally {
                KLog.i("open camera method end");
            }
        });
    }

    @Override
    public int closeCameraDevice(final KParams closeParam) {
        int closeResult = KParams.Value.CLOSE_STATE.DEVICE_NULL;
        try {
            boolean isGetLock = requestLock();
            if (isGetLock) {
                Long openCameraSign = closeParam.get(KParams.Key.OPEN_CAMERA_SIGN, 0L);
                String cameraId = closeParam.get(KParams.Key.CAMERA_ID, null);
                KLog.i("start close camera id " + cameraId + " close sign:" + openCameraSign  + "  open sign:" + mOpenCameraSign);
                if (openCameraSign != 0L && !openCameraSign.equals(mOpenCameraSign)) {
                    KLog.e("close camera device check sign fail");
                    closeResult = KParams.Value.CLOSE_STATE.DEVICE_CLOSED_CHECK_SIGN_FAIL;
                    return closeResult;
                }

                CameraDevice cameraDevice = null;
                if (mDeviceMap.containsKey(cameraId)) {
                    cameraDevice = mDeviceMap.remove(cameraId);
                }


                if (cameraDevice != null) {
                    cameraDevice.close();
                    closeResult = KParams.Value.CLOSE_STATE.DEVICE_CLOSED;
                    mOpenCameraSign = 0L;
                    KLog.i("close camera device : id :" + cameraId);
                }
                releaseLock();
            } else {
                closeResult = KParams.Value.CLOSE_STATE.DEVICE_CLOSED_REQ_LOCK_FAIL;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            KLog.i("end close camera method ");
        }
        return closeResult;
    }
}
