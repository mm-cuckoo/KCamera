package com.sgf.demo.reader

import com.sgf.kcamera.surface.ImageReaderProvider
import com.sgf.kcamera.log.KLog
import android.graphics.ImageFormat
import android.media.ImageReader
import android.util.Size
import com.sgf.demo.config.ConfigKey
import com.sgf.kcamera.CameraID
import com.sgf.kcamera.utils.WorkerHandlerManager
import java.lang.Exception

class PreviewImageReader2(private var listener: ImageDataListener? = null) : ImageReaderProvider(TYPE.PREVIEW) {
    var cameraId : CameraID = CameraID.BACK

    private var frameCount = 0
    fun getFrameCount(): Int {
        val frameCountTmp = frameCount
        frameCount = 0
        return frameCountTmp
    }
    private val handler = WorkerHandlerManager.getHandler(WorkerHandlerManager.Tag.T_TYPE_DATA)

    @Volatile
    private var imageByteArrayWithLock : ImageByteArrayWithLock? = null

    override fun createImageReader(previewSize: Size, captureSize: Size): ImageReader {
        imageByteArrayWithLock = ImageByteArrayWithLock(previewSize.width * previewSize.height * 3 / 2)
        return ImageReader.newInstance(
            previewSize.width,
            previewSize.height,
            ImageFormat.YUV_420_888,
            3
        )
    }

    override fun onImageAvailable(reader: ImageReader) {
        frameCount++
        val image = reader.acquireLatestImage()
        if (!ConfigKey.getBoolean(ConfigKey.SHOW_PRE_YUV, false)) {
            image?.close()
            return
        }
//        else {
//            image?.close()
//        }


        image?.let {
            imageByteArrayWithLock?.let { lock ->
                try {
                    if (listener != null && lock.requestLock()) {
                        lock.putImageByte(it, cameraId == CameraID.FONT)
                        it.close()
                        handler.post {
                            listener?.onPreImageByteArray2(lock, lock.width, lock.height)
                        }
                    } else {
                        it.close()
                    }
                } catch (e: Exception) {
                    it.close()
                    KLog.e("get image data exception")
                }

            } ?: kotlin.run {
                it.close()
            }
        }

    }

    override fun onRelease() {
        imageByteArrayWithLock?.unLockByteArray()
        imageByteArrayWithLock = null
        listener = null
    }
}