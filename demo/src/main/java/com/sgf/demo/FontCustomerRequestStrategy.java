package com.sgf.demo;

import android.hardware.camera2.CaptureRequest;
import com.sgf.kcamera.KCustomerRequestStrategy;

public class FontCustomerRequestStrategy implements KCustomerRequestStrategy {
    @Override
    public void onBuildRequest(String cameraId, CaptureRequest.Builder builder) {
//            builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE);
//            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(28, 30));

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            CaptureRequest.Key<Integer> customerKey = new CaptureRequest.Key<>(
//                    "XXXX",
//                    Integer.class);
//            builder.set(customerKey, 1);
//        }
    }
}
