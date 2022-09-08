package com.sgf.kcamera;

import android.hardware.camera2.CaptureRequest;

/**
 * 自定义Camera Request 参数
 * 使用这个自定义请求参数时要慎重操作 builder
 */
public interface KCustomerRequestStrategy {
    void onBuildRequest(CaptureRequest.Builder builder);
}
