package com.sgf.kcamera.camera.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Message;

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
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;

/**
 * 在这里维护所有已经开启的 open 的 camera device
 * 只负责 camera device 的开启和 关闭
 * */
public class KCameraDeviceImpl implements KCameraDevice {

    private static final Semaphore CAMERA_LOCK = new Semaphore(1, true);
    private static final long CAMERA_LOCK_TIMEOUT = 6000;

    private volatile static KCameraDevice sInstance;
    private final CameraManager mCameraManager;
    /**
     * 保存已经打开的camera device
     */
    private final Map<String, CameraDevice> mDeviceMap = new HashMap<>();
    private final Map<String, Long> mOpenSignMap = new HashMap<>();
    private final Scheduler mCameraScheduler;
    /**
     * 管理camera 打开和关闭队列的
     */
    private final Handler mCameraHandler;
    /**
     * 执行camera动作时使用，目前在打开open camera 时，在线程中释放 mCameraHandler 线程中的锁
     */
    private final Handler mCameraRunner;
    private static final AtomicBoolean isLock = new AtomicBoolean(false);

    private KCameraDeviceImpl(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mCameraScheduler = WorkerHandlerManager.getScheduler(WorkerHandlerManager.Tag.T_TYPE_CAMERA_SCHEDULER);
        mCameraHandler = WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_CAMERA_HANDLER);
        mCameraRunner = WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_CAMERA_RUNNER);
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

    private static boolean tryRequestLock(String from) throws InterruptedException {
        KLog.i( "requestLock with timeout ====>from:" + from);
        if (!CAMERA_LOCK.tryAcquire(CAMERA_LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
            KLog.e("Time out waiting to lock camera opening");
            return false;
        }
        KLog.i( "requestLock with timeout ====>end:from:" + from);
        isLock.set(true);
        return true;
    }

    private static void requestLock(String from) throws InterruptedException {
        KLog.i( "requestLock====>from:" + from);
        CAMERA_LOCK.acquire();
        isLock.set(true);
    }

    private static synchronized void releaseLock(String from) {
        KLog.i( "releaseLock====>from:" + from + " isLock:" + isLock.get());
        if (isLock.get()) {
            isLock.set(false);
            CAMERA_LOCK.release();
            KLog.i( "releaseLock====>from:" + from + " Permits:" + CAMERA_LOCK.availablePermits());
        }
    }

    @Override
    public Scheduler getCameraScheduler() {
        return mCameraScheduler;
    }

    @SuppressLint("MissingPermission")
    @Override
    public Observable<KParams> openCameraDevice(final KParams openCameraParams) {
        return Observable.create((ObservableOnSubscribe<KParams>) emitter -> {
            KParams ocp = new KParams(openCameraParams);
            final String cameraId = ocp.get(KParams.Key.CAMERA_ID);
            Long openSign = ocp.get(KParams.Key.OPEN_CAMERA_SIGN, 0L);
            //每次打开的任务交给handler任务队列管理
            Message message = Message.obtain(mCameraHandler, new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean isLock = tryRequestLock("open_camera_start");
                        if (isLock) {
                            mOpenSignMap.put(cameraId, openSign);
                            KLog.i( "handler open camera , " +
                                    "  camera id :" + cameraId +
                                    "  sign:" + openSign +
                                    "  params:" + ocp);
                            long openStartTime = System.currentTimeMillis();
                            mCameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                                @Override
                                public void onOpened(@NonNull CameraDevice camera) {
                                    long openUseTime = System.currentTimeMillis() - openStartTime;
                                    KLog.i("time:open camera use time :" + openUseTime);
                                    // 保存当前打开的camera device
                                    mDeviceMap.put(camera.getId(), camera);
                                    KLog.i( "camera opened ==》camera id: "  + camera.getId()  + "  sign:" + openSign + "  " + camera.hashCode());
                                    ocp.put(KParams.Key.CAMERA_DEVICE, camera);
                                    ocp.put(KParams.Key.OPEN_CAMERA_STATE, KParams.Value.CAMERA_OPEN_SUCCESS);
                                    ocp.put(KParams.Key.OPEN_CAMERA_SIGN, openSign);
                                    emitter.onNext(ocp);
                                    releaseLock("onOpened");
                                }

                                @Override
                                public void onDisconnected(@NonNull CameraDevice camera) {
                                    KLog.e("onDisconnected: sign:" + openSign);
                                    camera.close();
                                    releaseLock("onDisconnected");
                                    emitter.onError(new KException("camera onDisconnected:", KException.CODE_CAMERA_DISCONNECTED));
                                }

                                @Override
                                public void onError(@NonNull CameraDevice camera, int error) {
                                    KLog.e("onError: code:" + error);
                                    camera.close();
                                    releaseLock("onError");
                                    emitter.onError(new KException("camera onError code:", error));
                                }

                                @Override
                                public void onClosed(@NonNull CameraDevice camera) {
                                    KLog.e("camera closed"+ camera.hashCode());
                                }
                            }, mCameraRunner);

                            /*
                             * 获取锁，目的时阻塞当前任务，不让 mCameraHandler 中的后面任务执行
                             * 在 openCamera 执行完毕后在 mCameraRunner 中释放该锁，结束 mCameraHandler 中的一个任务
                             */
                            requestLock("open_camera_end");
                            // 释放刚刚获取到的锁
                            releaseLock("open_camera_end");
                        } else  {
                            emitter.onError(new KException("camera request lock fail",KException.CODE_CAMERA_REQUEST_LOCK_FAIL));
                        }
                    } catch (Exception e) {
                        releaseLock("open_camera_exception");
                        KLog.e("open camera Exception :" + Arrays.toString(e.getStackTrace()));
                    } finally {
                        KLog.i("open camera method end");
                    }
                }
            });
            message.what = openSign.intValue();
            mCameraHandler.sendMessage(message);
            KLog.i("push open camera runnable to handler what:" + message.what + "  camera id:" + cameraId + "  sign:" + openSign);
        });
    }

    @Override
    public CameraDevice getCameraDevice(KParams params) {
        final String cameraId = params.get(KParams.Key.CAMERA_ID);
        CameraDevice device = mDeviceMap.get(cameraId);
        KLog.i("getCameraDevice ===> camera id:" + cameraId + "  " + device);
        return device;
    }

    @Override
    public Observable<KParams> closeCameraDevice(final KParams closeParam) {
        return Observable.create((ObservableOnSubscribe<KParams>) emitter -> {
            KParams cp = new KParams(closeParam);
            Long closeCameraSign = cp.get(KParams.Key.OPEN_CAMERA_SIGN, 0L);

           // 当 openCameraSign 为 0 时会清掉打开相机中所有准备执行打开相机的任务
            if (closeCameraSign == 0L) {
                KLog.i("remove all handler open camera runnable");
                mCameraHandler.removeCallbacksAndMessages(null);
            }

            int handlerWhat = closeCameraSign.intValue();
            if (mCameraHandler.hasMessages(handlerWhat)) {
                KLog.e("remove handler open camera runnable what:" + handlerWhat);
                mCameraHandler.removeMessages(handlerWhat);
                KParams param = new KParams();
                param.put(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_CLOSED_REMOVE_OPEN_RUNNABLE);
                emitter.onNext(param);
            } else {
                Message message = Message.obtain(mCameraHandler, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            requestLock("close_camera");
                            String cameraId = cp.get(KParams.Key.CAMERA_ID, null);
                            Long openCameraSign = mOpenSignMap.get(cameraId);
                            if (openCameraSign == null) {
                                openCameraSign = 0L;
                            }
                            KLog.i("start close" +
                                    "  camera id " + cameraId +
                                    "  close sign:" + closeCameraSign +
                                    "  open sign:" + openCameraSign +
                                    "  params:" + cp);

                            if (closeCameraSign == 0L && cameraId == null) {
                                KLog.i("close all device start , size:" + mDeviceMap.size());
                                // 关闭所有已经打开的device
                                for (CameraDevice device : mDeviceMap.values()) {
                                    device.close();
                                }
                                mDeviceMap.clear();
                                mOpenSignMap.clear();
                                KParams param = new KParams();
                                param.put(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_CLOSED_ALL_DEVICE);
                                emitter.onNext(param);
                                KLog.i("close all device end");
                            } else if (closeCameraSign != 0L && !closeCameraSign.equals(openCameraSign)) {
                                /*
                                 * 每次相机打开都会生成一个sign, 关闭的时候会使用打开的时候生成的sign和当前打开的相机的sign进行对比，
                                 * 如果两个sign不相同，则认为当前已经打开的device已经是在新的应用中打开的，也就不需要进行关闭
                                 */
                                KLog.e("close camera device check sign fail");
                                KParams param = new KParams();
                                param.put(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_CLOSED_CHECK_SIGN_FAIL);
                                emitter.onNext(param);
                            } else {
                                KLog.i("close single camera device start ++++++ ");
                                int closeResult = KParams.Value.CLOSE_STATE.DEVICE_NULL;
                                CameraDevice cameraDevice = null;
                                if (mDeviceMap.containsKey(cameraId)) {
                                    cameraDevice = mDeviceMap.remove(cameraId);
                                }

                                if (cameraDevice != null) {
                                    cameraDevice.close();
                                    mOpenSignMap.remove(cameraId);
                                    closeResult = KParams.Value.CLOSE_STATE.DEVICE_CLOSED;
                                    KLog.i("close camera device : id :" + cameraId);
                                }

                                KParams param = new KParams();
                                param.put(KParams.Key.CLOSE_CAMERA_STATUS, closeResult);
                                emitter.onNext(param);
                                KLog.i("close single camera device end +++++++");

                            }
                        } catch (Exception e) {
                            KLog.e("close camera device exception =======");
                            e.printStackTrace();
                            KParams param = new KParams();
                            param.put(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_CLOSED_EXCEPTION);
                            emitter.onNext(param);
                        } finally {
                            releaseLock("close_camera_end");
                        }
                        KLog.i("end close camera method ");
                    }
                });
                message.what = 0;
                mCameraHandler.sendMessage(message);
                KLog.d("push close runnable to handler openSign:" + closeCameraSign);
                KParams param = new KParams();
                param.put(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_CLOSED_RUNNABLE_PUSH_HANDLER);
                emitter.onNext(param);
            }
        });
    }
}
