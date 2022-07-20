package com.sgf.demo.reader

import com.sgf.kcamera.surface.ImageReaderProvider
import com.sgf.kcamera.log.KLog
import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.os.Environment
import android.util.Size
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CapturePNGImageReader(private var listener: ImageDataListener? = null) : ImageReaderProvider(TYPE.CAPTURE) {
    override fun createImageReader(previewSize: Size, captureSize: Size): ImageReader {
        KLog.d("createImageReader: captureSize width:" + captureSize.width + "  captureSize height:" + captureSize.height)
        return ImageReader.newInstance(captureSize.width, captureSize.height, ImageFormat.RAW_SENSOR, 2)
    }

    override fun onImageAvailable(reader: ImageReader) {
        val format = SimpleDateFormat("'/PIC'_yyyyMMdd_HHmmss'.png'", Locale.getDefault())
        val fileName = format.format(Date())
        val filePath = Environment.getExternalStorageDirectory().absoluteFile.path
        KLog.d("createImageReader: pic file path:" + (filePath + fileName))
        ImageSaver(reader.acquireNextImage(), File(filePath + fileName)).run()
    }

    private class ImageSaver internal constructor(
        private val mImage: Image,
        private val mFile: File
    ) : Runnable {
        override fun run() {
            val buffer = mImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer[bytes]
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output.write(bytes)
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