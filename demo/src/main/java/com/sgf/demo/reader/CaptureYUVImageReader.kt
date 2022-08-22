package com.sgf.demo.reader

import com.sgf.kcamera.surface.ImageReaderProvider
import com.sgf.kcamera.log.KLog
import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.os.Environment
import android.util.Size
import com.sgf.demo.config.ConfigKey
import com.sgf.demo.utils.FilePathUtils
import com.sgf.demo.utils.ImageUtil
import com.sgf.demo.utils.ImageUtil.yuv420888ToNV21
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CaptureYUVImageReader(private var listener: ImageDataListener? = null) : ImageReaderProvider(TYPE.CAPTURE) {
    override fun createImageReader(previewSize: Size, captureSize: Size): ImageReader {
        KLog.d("createImageReader: captureSize width:" + captureSize.width + "  captureSize height:" + captureSize.height)
        return ImageReader.newInstance(captureSize.width, captureSize.height, ImageFormat.YUV_420_888, 2)
    }

    override fun onImageAvailable(reader: ImageReader) {
        val format = SimpleDateFormat("'/PIC_YUV420_888'_yyyyMMdd_HHmmss'.jpeg'", Locale.getDefault())
        val fileName = format.format(Date())
        val filePath = FilePathUtils.getRootPath()
        FilePathUtils.checkFolder(filePath)
        KLog.d("createImageReader: pic file path:" + (filePath + fileName))
        ImageSaver(reader.acquireNextImage(), File(filePath + fileName), listener).run()
    }

    private class ImageSaver(
        private val mImage: Image,
        private val mFile: File,
        private val listener: ImageDataListener?
    ) : Runnable {
        override fun run() {
            val nv21 = mImage.yuv420888ToNV21()
            val jpeg = ImageUtil.nv21ToJPEG(nv21, mImage.width, mImage.height)
            val bitmap = ImageUtil.getPicFromBytes(jpeg)
            listener?.onCaptureBitmap(ConfigKey.SHOW_YUV_OT_JPEG_VALUE, bitmap, mFile.path)
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output.write(jpeg)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImage.close()
                if (null != output) {
                    try {
                        output.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}