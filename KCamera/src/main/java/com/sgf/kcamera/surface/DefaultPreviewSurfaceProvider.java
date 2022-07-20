package com.sgf.kcamera.surface;

import android.graphics.SurfaceTexture;
import android.util.Size;
import android.view.Surface;

public class DefaultPreviewSurfaceProvider implements  PreviewSurfaceProvider{
    @Override
    public Surface getSurface() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Class<?> getPreviewSurfaceClass() {
        return SurfaceTexture.class;
    }

    @Override
    public void setAspectRatio(Size size) {

    }
}
