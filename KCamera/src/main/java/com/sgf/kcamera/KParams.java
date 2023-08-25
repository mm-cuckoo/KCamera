package com.sgf.kcamera;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.util.Pair;
import android.util.Size;

import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.surface.SurfaceManager;
import com.sgf.kcamera.surface.SurfaceProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.annotations.NonNull;

public class KParams {
    private final Map<Key<?>, Object> mMapObject = new HashMap<>();

    public KParams(){}
    public KParams(KParams params) {
        params.copyTo(this);
    }

    /**
     * 拷贝当前 KParams 中的内容到新的 KParams 中
     * @param params ： 需要拷贝到目标的 KParams
     */
    void copyTo(KParams params) {
        for (Map.Entry<Key<?>, Object> entry: mMapObject.entrySet()) {
            params.put((Key)entry.getKey() , entry.getValue());
        }
    }

    public <T> void put(Key<T> key, T value) {
        mMapObject.put(key,value);
    }


    public <T> T get(Key<T> key) {
        if (mMapObject.containsKey(key) ) {
            return (T) mMapObject.get(key);
        }
        return null;
    }

    public <T> T get(Key<T> key, T def) {
        if (mMapObject.containsKey(key) ) {
            return (T) mMapObject.get(key);
        }
        return def;
    }

    @NonNull
    @Override
    public String toString() {
        String result;
        if (KLog.isDebug()) {
            StringBuilder buffer = new StringBuilder("\n");
            buffer.append("================ Params ======================================================= ");
            buffer.append("\n");
            for (Map.Entry<Key<?>, Object> entry : mMapObject.entrySet()) {
                buffer.append("== ").append(entry.getKey().NAME).append(":").append(entry.getValue()).append("\n");
            }
            buffer.deleteCharAt(buffer.length() -1);
            result = buffer.toString();
        } else {
            result = super.toString();
        }
        return result;
    }
    public interface Value {
        /**
         * 闪光灯配置参数
         */
        interface FLASH_STATE {
//            int TORCH       = 1;
            int OFF         = 2; // 关闭
            int AUTO        = 3; // 自动
            int ON          = 4; // 打开
        }
        String OK = "ok";
        String CAMERA_OPEN_SUCCESS = "camera_open_success";
        String CAMERA_DISCONNECTED = "camera_disconnected";
//        String CAMERA_OPEN_ERROR = "camera_open_error";
        String CAMERA_DEVICE_CLOSE = "camera_device_close";
        String CAMERA_OPEN_EXCEPTION = "camera_open_Exception";
        String CAMERA_OPEN_REQUEST_LOCK_FAIL = "camera_open_request_lock_fail";

        interface CAPTURE_STATE {
            int CAPTURE_START = 1;
            int CAPTURE_COMPLETED = 2;
            int CAPTURE_FAIL = 3;
        }

        interface CLOSE_STATE {
            /**
             * 关闭时 device 为 null
             */
            int DEVICE_NULL = 0;
            /**
             * 成功关闭了一个 device
             */
            int DEVICE_CLOSED = 1;
            /**
             * 关闭所有的device
             */
            int DEVICE_CLOSED_ALL_DEVICE  = 2;
            /**
             * 关闭前校验打开sign 失败
             */
            int DEVICE_CLOSED_CHECK_SIGN_FAIL = 3;
            /**
             * 在打开队列中发现一个没有执行打开camera 的任务并移除
             */
            int DEVICE_CLOSED_REMOVE_OPEN_RUNNABLE = 4;
            /**
             * 在打开camera之前执行的关闭任务
             */
            int DEVICE_CLOSED_RUNNABLE_PUSH_HANDLER = 5;
            /**
             * 关闭camera时遇到的异常
             */
            int DEVICE_CLOSED_EXCEPTION = 6;
        }
    }


    public final static class Key<T> {
        public static final Key<SurfaceManager> SURFACE_MANAGER = new Key<>("surface_manager");
        public static final Key<List<SurfaceProvider>> IMAGE_READER_PROVIDERS = new Key<>("image_reader_providers");
        public static final Key<String> CAMERA_ID = new Key<>("camera_id");
        public static final Key<CameraDevice> CAMERA_DEVICE = new Key<>("camera_device");
        public static final Key<String> OPEN_CAMERA_STATE = new Key<>("open_camera_state");
        public static final Key<CaptureRequest.Builder> REQUEST_BUILDER = new Key<>("request_builder");
        public static final Key<Size> PIC_SIZE = new Key<>("pic_size");
        public static final Key<Integer> PIC_ORIENTATION = new Key<>("pic_orientation");
        public static final Key<Size> PREVIEW_SIZE = new Key<>("preview_size");
        public static final Key<Integer> AF_STATE = new Key<>("af_state");
        public static final Key<Integer> CAPTURE_STATE = new Key<>("capture_state");
        public static final Key<String> PREVIEW_FIRST_FRAME = new Key<>("preview_first_frame");
        public static final Key<CameraCaptureSession.CaptureCallback> CAPTURE_CALLBACK = new Key<>("capture_callback");

        public static final Key<Float> ZOOM_VALUE = new Key<>("zoom_value");
        public static final Key<Integer> FLASH_STATE = new Key<>("camera_flash_value");
        public static final Key<Integer> EV_SIZE = new Key<>("ev_size");
        public static final Key<Float> FOCAL_LENGTH = new Key<>("focal_length");
        public static final Key<Pair<Float, Float>> AF_TRIGGER = new Key<>("af_trigger");// Pair<X, Y>
        public static final Key<Boolean> RESET_FOCUS = new Key<>("reset_focus");

        public static final Key<Long> OPEN_CAMERA_SIGN = new Key<>("open_camera_sign");
        public static final Key<Integer> CLOSE_CAMERA_STATUS = new Key<>("close_camera_status"); // 1 close device , 0 device null
        public static final Key<Boolean> CAPTURE_CAN_TRIGGER_AF = new Key<>("capture_can_trigger_af"); // 1 close device , 0 device null

        public static final Key<KCustomerRequestStrategy> CUSTOMER_REQUEST_STRATEGY = new Key<>("customer_request_strategy");

        public final String NAME;
        public Key(String name) {
            this.NAME = name;
        }
    }
}
