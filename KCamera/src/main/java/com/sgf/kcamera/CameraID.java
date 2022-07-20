package com.sgf.kcamera;

import androidx.annotation.NonNull;

/**
 * 相机的ID
 *
 * 可以自定义
 */
public class CameraID {
    public static final CameraID BACK = new CameraID("0");
    public static final CameraID FONT = new CameraID("1");
    public final String ID;
    public CameraID(@NonNull String id) {
        this.ID = id;
    }
}
