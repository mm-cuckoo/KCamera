package com.sgf.kcamera.camera.session;

import android.content.Context;

import com.sgf.kcamera.camera.device.KCameraDeviceImpl;

public class CameraSessionManagerImpl implements CameraSessionManager {

    private volatile static CameraSessionManager mSessionManager;
    private final Context mContext;

    private CameraSessionManagerImpl(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static CameraSessionManager getInstance(Context context) {
        if (mSessionManager == null) {
            synchronized (CameraSessionManagerImpl.class) {
                if (mSessionManager == null) {
                    mSessionManager = new CameraSessionManagerImpl(context);
                }
            }
        }
        return mSessionManager;
    }

    @Override
    public CameraSession requestSession() {
        return new CameraSessionImpl(KCameraDeviceImpl.getsInstance(mContext));
    }
}
