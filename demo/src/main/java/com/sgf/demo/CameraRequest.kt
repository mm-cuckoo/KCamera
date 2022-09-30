package com.sgf.demo

import android.content.Context
import android.graphics.ImageFormat
import android.util.Size
import com.sgf.demo.config.ConfigKey
import com.sgf.demo.reader.*
import com.sgf.kcamera.CameraID
import com.sgf.kcamera.request.FlashState
import com.sgf.kcamera.request.PreviewRequest
import com.sgf.kcamera.surface.PreviewSurfaceProvider

class CameraRequest(private val ctx: Context) {
    private var fontImageReader: PreviewImageReader? = null
    private var backImageReader: PreviewImageReader? = null

    private var fontPreviewSize = Size(1280,960)
    private var fontYuvSize = Size(1280,960)
    private var fontPicSize = Size(1280,960)

    private var backPreviewSize = Size(1280,960)
    private var backYuvSize = Size(1280,960)
    private var backPicSize = Size(1280,960)
    fun reloadSize() {
        fontPreviewSize = ConfigKey.getSize(ConfigKey.FONT_PREVIEW_SIZE, ConfigKey.DEF_FONT_PREVIEW_SIZE)
        fontYuvSize = ConfigKey.getSize(ConfigKey.FONT_YUV_SIZE, ConfigKey.DEF_FONT_YUV_SIZE)
        fontPicSize = ConfigKey.getSize(ConfigKey.FONT_PIC_SIZE, ConfigKey.DEF_FONT_PIC_SIZE)

        backPreviewSize = ConfigKey.getSize(ConfigKey.BACK_PREVIEW_SIZE, ConfigKey.DEF_BACK_PREVIEW_SIZE)
        backYuvSize = ConfigKey.getSize(ConfigKey.BACK_YUV_SIZE, ConfigKey.DEF_BACK_YUV_SIZE)
        backPicSize = ConfigKey.getSize(ConfigKey.BACK_PIC_SIZE, ConfigKey.DEF_BACK_PIC_SIZE)
    }


    fun getFontFrameCount(): Int {
        return fontImageReader?.getFrameCount() ?: 0
    }

    fun getBackFrameCount(): Int {
        return backImageReader?.getFrameCount() ?: 0
    }

    fun getFont2FrameCount(): Int {
        return fontImageReader?.getFrameCount() ?: 0
    }


    fun getFontPreviewSize(): Size {
        return fontPreviewSize
    }

    fun getFontYuvSize() : Size {
        return fontYuvSize;
    }

    fun getFontPicSize() : Size {
        return fontPicSize;
    }


    fun getBackPreviewSize(): Size {
        return backPreviewSize
    }

    fun getBackYuvSize() : Size {
        return backYuvSize;
    }

    fun getBackPicSize() : Size {
        return backPicSize;
    }


    fun getFont2Size(): Size {
        return fontPreviewSize
    }

    fun getFont2Request(provider : PreviewSurfaceProvider,  listener: ImageDataListener): PreviewRequest.Builder {
        return getFont2Request(backPreviewSize, provider, listener)
    }


    fun getFont2Request(size: Size, provider : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder {
        backPreviewSize = size
        fontImageReader = PreviewImageReader(fontYuvSize,listener)
        val previewSize = Size(size.width, size.height)
        val picSize = Size(size.width, size.height)
        val builder = PreviewRequest.createBuilder()
            .setPreviewSize(previewSize)
            .openCustomCamera(CameraID("2"))
            .addPreviewSurfaceProvider(provider)
            .setPictureSize(picSize, ImageFormat.YUV_420_888)
            .setFlash(FlashState.OFF)
        if (ConfigKey.getBoolean(ConfigKey.SHOW_PRE_YUV, false)) {
            builder.addSurfaceProvider(fontImageReader)
        }

        setTakeBuild(fontPicSize, builder, listener)

        return builder

    }

    fun getBackRequest2(provider : PreviewSurfaceProvider,providerpr1 : PreviewSurfaceProvider,  listener: ImageDataListener): PreviewRequest.Builder {
        return getBackRequest2(backPreviewSize, provider,providerpr1, listener)
    }


    fun getBackRequest2(size: Size, provider : PreviewSurfaceProvider,provider1 : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder {
        backPreviewSize = size
        backImageReader = PreviewImageReader(backYuvSize, listener)
        val previewSize = Size(size.width, size.height)
        val picSize = Size(size.width, size.height)
        val builder = PreviewRequest.createBuilder()
            .setPreviewSize(previewSize)
            .openBackCamera()
            .addPreviewSurfaceProvider(provider)
            .addPreviewSurfaceProvider(provider1)
            .setPictureSize(picSize, ImageFormat.YUV_420_888)
            .setFlash(FlashState.OFF)
        if (ConfigKey.getBoolean(ConfigKey.SHOW_PRE_YUV, false)) {
            builder.addSurfaceProvider(backImageReader)
        }

        setTakeBuild(backPicSize, builder, listener)

        return builder

    }


    fun getBackRequest(provider : PreviewSurfaceProvider,  listener: ImageDataListener): PreviewRequest.Builder {
        return getBackRequest(backPreviewSize, provider, listener)
    }


    fun getBackRequest(size: Size, provider : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder {
        backPreviewSize = size
        backImageReader = PreviewImageReader(backYuvSize, listener)
        val previewSize = Size(size.width, size.height)
        val picSize = Size(size.width, size.height)
        val builder = PreviewRequest.createBuilder()
            .setPreviewSize(previewSize)
            .openBackCamera()
            .addPreviewSurfaceProvider(provider)
            .setPictureSize(picSize, ImageFormat.YUV_420_888)
            .setFlash(FlashState.OFF)
            .setCustomerRequestStrategy(BackCustomerRequestStrategy())
        if (ConfigKey.getBoolean(ConfigKey.SHOW_PRE_YUV, false)) {
            builder.addSurfaceProvider(backImageReader)
        }

        setTakeBuild(backPicSize,builder, listener)

        return builder

    }

    fun getFontRequest(provider : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder{
        return getFontRequest(fontPreviewSize, provider, listener)
    }

    fun getFontRequest(size: Size, provider : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder {
        fontPreviewSize = size
        fontImageReader = PreviewImageReader(fontYuvSize, listener)
        val previewSize = Size(size.width, size.height)
        val picSize = Size(size.width, size.height)
        val builder = PreviewRequest.createBuilder()
            .setPreviewSize(previewSize)
            .openFontCamera()
            .addPreviewSurfaceProvider(provider)
            .setPictureSize(picSize, ImageFormat.JPEG)
            .setFlash(FlashState.OFF)
            .setCustomerRequestStrategy(FontCustomerRequestStrategy())
        if (ConfigKey.getBoolean(ConfigKey.SHOW_PRE_YUV, false)) {
            builder.addSurfaceProvider(fontImageReader)
        }

        setTakeBuild(fontPicSize, builder, listener)
        return builder
    }

    private fun setTakeBuild(picSize: Size , builder: PreviewRequest.Builder,listener: ImageDataListener) {
        if (ConfigKey.getBoolean(ConfigKey.TAKE_JPEG_PIC, false)) {
            builder.addSurfaceProvider(CaptureJPEGImageReader(picSize, listener))
        }

        if (ConfigKey.getBoolean(ConfigKey.TAKE_YUV_TO_JPEG_PIC, false)) {
            builder.addSurfaceProvider(CaptureYUVImageReader(picSize,listener))
        }
        if (ConfigKey.getBoolean(ConfigKey.TAKE_PNG_PIC, false)) {
            builder.addSurfaceProvider(CapturePNGImageReader(picSize,listener))
        }
    }
}