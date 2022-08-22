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
    private var fontImageReader2: PreviewImageReader2? = null
    private var backImageReader: PreviewImageReader? = null
    private var backImageReader2: PreviewImageReader2? = null

    fun getFontFrameCount(): Int {
        return fontImageReader?.getFrameCount() ?: 0
    }

    fun getBackFrameCount(): Int {
        return backImageReader?.getFrameCount() ?: 0
    }

    fun getFont2FrameCount(): Int {
        return fontImageReader?.getFrameCount() ?: 0
    }

    private var fontSize = Size(1280,960)
    fun getFontSize(): Size {
        return fontSize
    }

    private var backSize = Size(1280,960)
    fun getBackSize(): Size {
        return backSize
    }

    fun getFont2Size(): Size {
        return backSize
    }

    fun getFont2Request(provider : PreviewSurfaceProvider,  listener: ImageDataListener): PreviewRequest.Builder {
        return getFont2Request(backSize, provider, listener)
    }


    fun getFont2Request(size: Size, provider : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder {
        backSize = size
        fontImageReader = PreviewImageReader(listener)
        fontImageReader2 = PreviewImageReader2(listener)
        val previewSize = Size(size.width, size.height)
        val picSize = Size(size.width, size.height)
        val builder = PreviewRequest.createBuilder()
            .setPreviewSize(previewSize)
            .openCustomCamera(CameraID("2"))
            .addPreviewSurfaceProvider(provider)
            .setPictureSize(picSize, ImageFormat.YUV_420_888)
            .setFlash(FlashState.OFF)
            .addSurfaceProvider(fontImageReader)
//            .addSurfaceProvider(fontImageReader2)

        setTakeBuild(builder, listener)

        return builder

    }

    fun getBackRequest2(provider : PreviewSurfaceProvider,providerpr1 : PreviewSurfaceProvider,  listener: ImageDataListener): PreviewRequest.Builder {
        return getBackRequest2(backSize, provider,providerpr1, listener)
    }


    fun getBackRequest2(size: Size, provider : PreviewSurfaceProvider,provider1 : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder {
        backSize = size
        backImageReader = PreviewImageReader(listener)
        backImageReader2 = PreviewImageReader2(listener)
        val previewSize = Size(size.width, size.height)
        val picSize = Size(size.width, size.height)
        val builder = PreviewRequest.createBuilder()
            .setPreviewSize(previewSize)
            .openBackCamera()
            .addPreviewSurfaceProvider(provider)
            .addPreviewSurfaceProvider(provider1)
            .setPictureSize(picSize, ImageFormat.YUV_420_888)
            .setFlash(FlashState.OFF)
            .addSurfaceProvider(backImageReader)
//            .addSurfaceProvider(backImageReader2)

        setTakeBuild(builder, listener)

        return builder

    }


    fun getBackRequest(provider : PreviewSurfaceProvider,  listener: ImageDataListener): PreviewRequest.Builder {
        return getBackRequest(backSize, provider, listener)
    }


    fun getBackRequest(size: Size, provider : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder {
        backSize = size
        backImageReader = PreviewImageReader(listener)
        backImageReader2 = PreviewImageReader2(listener)
        val previewSize = Size(size.width, size.height)
        val picSize = Size(size.width, size.height)
        val builder = PreviewRequest.createBuilder()
            .setPreviewSize(previewSize)
            .openBackCamera()
            .addPreviewSurfaceProvider(provider)
            .setPictureSize(picSize, ImageFormat.YUV_420_888)
            .setFlash(FlashState.OFF)
            .addSurfaceProvider(backImageReader)
            .addSurfaceProvider(backImageReader2)

        setTakeBuild(builder, listener)

        return builder

    }

    fun getFontRequest(provider : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder{
        return getFontRequest(fontSize, provider, listener)
    }

    fun getFontRequest(size: Size, provider : PreviewSurfaceProvider, listener: ImageDataListener): PreviewRequest.Builder {
        fontSize = size
        fontImageReader = PreviewImageReader(listener)
        fontImageReader2 = PreviewImageReader2(listener)
        val previewSize = Size(size.width, size.height)
        val picSize = Size(size.width, size.height)
        val builder = PreviewRequest.createBuilder()
            .setPreviewSize(previewSize)
            .openFontCamera()
            .addPreviewSurfaceProvider(provider)
            .setPictureSize(picSize, ImageFormat.JPEG)
            .setFlash(FlashState.OFF)
            .addSurfaceProvider(fontImageReader)
            .addSurfaceProvider(fontImageReader2)

        setTakeBuild(builder, listener)
        return builder
    }

    private fun setTakeBuild(builder: PreviewRequest.Builder,listener: ImageDataListener) {
        if (ConfigKey.getBoolean(ConfigKey.TAKE_JPEG_PIC, false)) {
            builder.addSurfaceProvider(CaptureJPEGImageReader(listener))
        }

        if (ConfigKey.getBoolean(ConfigKey.TAKE_YUV_TO_JPEG_PIC, false)) {
            builder.addSurfaceProvider(CaptureYUVImageReader(listener))
        }
        if (ConfigKey.getBoolean(ConfigKey.TAKE_PNG_PIC, false)) {
            builder.addSurfaceProvider(CapturePNGImageReader(listener))
        }
    }
}