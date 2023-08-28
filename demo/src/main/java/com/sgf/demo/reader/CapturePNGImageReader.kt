package com.sgf.demo.reader

import android.graphics.Bitmap
import com.sgf.kcamera.surface.ImageReaderProvider
import com.sgf.kcamera.log.KLog
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Environment
import android.util.Size
import androidx.annotation.RequiresApi
import com.sgf.demo.AppApplication
import com.sgf.demo.config.ConfigKey
import com.sgf.demo.utils.FilePathUtils
import com.sgf.demo.utils.ImageUtil
import com.sgf.demo.utils.ImageUtil.yuv420888ToNV21
import com.sgf.demo.utils.NV21ToBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CapturePNGImageReader(private var listener: ImageDataListener? = null) : ImageReaderProvider(TYPE.CAPTURE) {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun createImageReader(previewSize: Size, captureSize: Size): ImageReader {
        KLog.d("createImageReader: captureSize width:" + captureSize.width + "  captureSize height:" + captureSize.height)
        return ImageReader.newInstance(captureSize.width, captureSize.height, ImageFormat.YUV_420_888, 2)
    }

    override fun onImageAvailable(reader: ImageReader) {
        val captureTime = System.currentTimeMillis()
        val format = SimpleDateFormat("'/PIC_PNG'_yyyyMMdd_HHmmss'.png'", Locale.getDefault())
        val fileName = format.format(Date())
        val filePath = FilePathUtils.getRootPath()
        FilePathUtils.checkFolder(filePath)
        KLog.d("createImageReader: pic file path:" + (filePath + fileName))
        ImageSaver(reader.acquireNextImage(), File(filePath + fileName), captureTime,listener).run()
    }

    private class ImageSaver(
        private val mImage: Image,
        private val mFile: File,
        private val captureTime : Long,
        private val listener: ImageDataListener? = null

    ) : Runnable {
        override fun run() {

            val nv21 = mImage.yuv420888ToNV21()
            val bitmap = NV21ToBitmap(AppApplication.context).nv21ToBitmap(nv21, mImage.width, mImage.height)

            mImage.close()

            ByteArrayOutputStream().use { baos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                mFile.writeBytes(baos.toByteArray())
            }
            listener?.onCaptureBitmap(ConfigKey.SHOW_PNG_VALUE, bitmap, mFile.path,captureTime)
        }

    }



}