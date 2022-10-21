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

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;

/**
 * 在这里维护所有已经开启的 open 的 camera device
 * 只负责 camera device 的开启和 关闭
 * */
public class KCameraDeviceImpl implements KCameraDevice {

    private volatile static KCameraDevice sInstance;
    private final CameraManager mCameraManager;
    /**
     * 保存已经打开的camera device
     */
    private final Map<String, CameraDevice> mDeviceMap = new HashMap<>();
    private final Map<String, Long> mOpenSignMap = new HashMap<>();
    private final Scheduler mCameraScheduler;
    private final Handler runHandler ;

    private KCameraDeviceImpl(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mCameraScheduler = WorkerHandlerManager.getScheduler(WorkerHandlerManager.Tag.T_TYPE_CAMERA);
        runHandler = WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_CAMERA_RUNNER);
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

    @SuppressLint("MissingPermission")
    @Override
    public Observable<KParams> openCameraDevice(final KParams openCameraParams) {
        final String cameraId = openCameraParams.get(KParams.Key.CAMERA_ID);
        return Observable.create((ObservableOnSubscribe<KParams>) emitter -> {
            Long openSign = openCameraParams.get(KParams.Key.OPEN_CAMERA_SIGN, 0L);
            //每次打开的任务交给handler任务队列管理
            Message message = Message.obtain(runHandler, new Runnable() {
                @Override
                public void run() {
                    try {
                        mOpenSignMap.put(cameraId, openSign);
                        KLog.i( "handler open camera , " +
                                "  camera id :" + cameraId +
                                "  sign:" + openSign +
                                "   params:" + openCameraParams);
                        mCameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                            @Override
                            public void onOpened(@NonNull CameraDevice camera) {
                                // 保存当前打开的camera device
                                mDeviceMap.put(camera.getId(), camera);
                                KLog.i( " camera opened ==》camera id: "  + camera.getId()  + "  sign:" + openSign);

                                openCameraParams.put(KParams.Key.CAMERA_DEVICE, camera);
                                openCameraParams.put(KParams.Key.OPEN_CAMERA_STATE, KParams.Value.CAMERA_OPEN_SUCCESS);
                                openCameraParams.put(KParams.Key.OPEN_CAMERA_SIGN, openSign);
                                emitter.onNext(openCameraParams);
                            }

                            @Override
                            public void onDisconnected(@NonNull CameraDevice camera) {
                                KLog.e("onDisconnected: ");
                                camera.close();
                                emitter.onError(new KException("camera onDisconnected:", KException.CODE_CAMERA_DISCONNECTED));
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
                    } catch (Exception  e) {
                        KLog.e("open camera Exception :" + Arrays.toString(e.getStackTrace()));
                    } finally {
                        KLog.i("open camera method end");
                    }
                }
            });
            message.what = openSign.intValue();
            runHandler.sendMessage(message);
            KLog.i("push open camera runnable to handler what:" + message.what + "  camera id:" + cameraId + "  sign:" + openSign);
        });
    }

    @Override
    public Observable<KParams> closeCameraDevice(final KParams closeParam) {
        return Observable.create((ObservableOnSubscribe<KParams>) emitter -> {
            Long closeCameraSign = closeParam.get(KParams.Key.OPEN_CAMERA_SIGN, 0L);

           // 当 openCameraSign 为 0 时会清掉打开相机中所有准备执行打开相机的任务
            if (closeCameraSign == 0L) {
                KLog.i("remove all handler open camera runnable");
                runHandler.removeCallbacksAndMessages(null);
            }

            int handlerWhat = closeCameraSign.intValue();
            if (runHandler.hasMessages(handlerWhat)) {
                KLog.e("remove handler open camera runnable what:" + handlerWhat);
                runHandler.removeMessages(handlerWhat);
                KParams param = new KParams();
                param.put(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_CLOSED_REMOVE_OPEN_RUNNABLE);
                emitter.onNext(param);
            } else {
                Message message = Message.obtain(runHandler, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String cameraId = closeParam.get(KParams.Key.CAMERA_ID, null);
                            Long openCameraSign = mOpenSignMap.get(cameraId);
                            if (openCameraSign == null) {
                                openCameraSign = 0L;
                            }
                            KLog.i("start close camera id " + cameraId + " close sign:" + closeCameraSign  + "  open sign:" + openCameraSign);

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
                                    KLog.d("close camera device : id :" + cameraId);
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
                        }
                        KLog.i("end close camera method ");
                    }
                });
                message.what = 0;
                runHandler.sendMessage(message);
                KLog.d("push close runnable to handler openSign:" + closeCameraSign + "  ppp:" +closeParam);
                KParams param = new KParams();
                param.put(KParams.Key.CLOSE_CAMERA_STATUS, KParams.Value.CLOSE_STATE.DEVICE_CLOSED_RUNNABLE_PUSH_HANDLER);
                emitter.onNext(param);
            }
        });
    }
}
