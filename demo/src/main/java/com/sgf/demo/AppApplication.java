package com.sgf.demo;

import android.app.Application;
import android.content.Context;

import com.sgf.demo.config.ConfigKey;
import com.sgf.demo.utils.OrientationSensorManager;
import com.sgf.kcamera.camera.info.CameraInfoHelper;
import com.sgf.kcamera.log.KLog;
import com.sgf.kcamera.utils.WorkerHandlerManager;


public class AppApplication extends Application {


    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        KLog.setPrintTag("KCamera");
        KLog.setDebug(true);
        ConfigKey.INSTANCE.init(this);
        CameraInfoHelper.getInstance().load(this, WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_DATA));
        OrientationSensorManager.getInstance().init(this);
        context = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }
}
