package com.sgf.demo.utils

import android.graphics.*
import android.media.Image
import java.io.*

object ImageUtil {

    fun Image.yuv420888ToNV21(): ByteArray {
        val nv21: ByteArray
        val yBuffer = planes[0].buffer
        val vuBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()
        nv21 = ByteArray(ySize * 3 / 2)
        yBuffer[nv21, 0, ySize]
        vuBuffer[nv21, ySize, vuSize]
        return nv21
    }

    fun Image.yuv420888ToNV21(nv21: ByteArray): ByteArray {
        val yBuffer = planes[0].buffer
        val vuBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()
        yBuffer[nv21, 0, ySize]
        vuBuffer[nv21, ySize, vuSize]
        return nv21
    }

    fun Image.yuv420888ToJPEG(): ByteArray {
        val nv21 = yuv420888ToNV21()
        val out = ByteArrayOutputStream()
        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        yuv.compressToJpeg(Rect(0, 0, width, height), 100, out)
        return out.toByteArray()
    }

    fun nv21ToJPEG(nv21: ByteArray, width: Int, height: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        yuv.compressToJpeg(Rect(0, 0, width, height), 100, out)
        return out.toByteArray()
    }

    fun mirrorNV21(nv21_data: ByteArray, width: Int, height: Int): ByteArray {
        var left: Int
        var right: Int
        var temp: Byte
        var startPos = 0
        // mirror Y
        var i = 0
        while (i < height) {
            left = startPos
            right = startPos + width - 1
            while (left < right) {
                temp = nv21_data[left]
                nv21_data[left] = nv21_data[right]
                nv21_data[right] = temp
                left++
                right--
            }
            startPos += width
            i++
        }
        // mirror U and V
        val offset = width * height
        startPos = 0
        i = 0
        while (i < height / 2) {
            left = offset + startPos
            right = offset + startPos + width - 2
            while (left < right) {
                temp = nv21_data[left]
                nv21_data[left] = nv21_data[right]
                nv21_data[right] = temp
                left++
                right--
                temp = nv21_data[left]
                nv21_data[left] = nv21_data[right]
                nv21_data[right] = temp
                left++
                right--
            }
            startPos += width
            i++
        }
        return nv21_data
    }

    fun rotateYUV270(src: ByteArray, width: Int, height: Int): ByteArray {
        var count = 0
        val uvHeight = height shr 1
        val imgSize = width * height
        val des = ByteArray(imgSize * 3 shr 1)
        //copy y
        for (j in width - 1 downTo 0) {
            for (i in 0 until height) {
                des[count++] = src[width * i + j]
            }
        }
        //u,v
        var j = width - 1
        while (j > 0) {
            for (i in 0 until uvHeight) {
                des[count++] = src[imgSize + width * i + j - 1]
                des[count++] = src[imgSize + width * i + j]
            }
            j -= 2
        }
        return des
    }

    fun rotateYUV180(data: ByteArray, w: Int, h: Int): ByteArray {
        val imgSize = w * h
        val len = imgSize * 3 / 2 //yuv数组长度是图片尺寸的1.5倍
        val yuv = ByteArray(len)
        var i = 0
        var count = 0
        //y
        i = imgSize - 1
        while (i >= 0) {
            yuv[count++] = data[i]
            i--
        }
        //u,v
        i = len - 1
        while (i >= imgSize) {
            yuv[count++] = data[i - 1]
            yuv[count++] = data[i]
            i -= 2
        }
        return yuv
    }

    //特别提醒旋转90和270后宽高要记得对调，不然会花屏
    fun rotateYUV90(src: ByteArray, width: Int, height: Int): ByteArray {
        val wh = width * height
        val yuv = ByteArray(wh * 3 shr 1)
        //旋转Y
        var count = 0
        for (i in 0 until width) {
            for (j in 0 until height) {
                yuv[count++] = src[width * (j + 1) - 1 + i]
            }
        }
        var i = 0
        while (i < width) {
            var j = 0
            val len = height shr 1
            var index = 0
            while (j < len) {
                index = wh + width * j + i
                yuv[count] = src[index]
                yuv[count + 1] = src[index + 1]
                count += 2
                j++
            }
            i += 2
        }
        return yuv
    }

    fun saveNV21File(data: ByteArray, width: Int, height: Int) {
        val image = YuvImage(data, ImageFormat.NV21, width, height, null)
        val outputSteam = ByteArrayOutputStream()
        image.compressToJpeg(Rect(0, 0, image.width, image.height), 70, outputSteam)
        val jpegData = outputSteam.toByteArray() //从outputSteam得到byte数据
        val file = File("/sdcard/pic/camera_" + System.currentTimeMillis() + ".jpg")
        if (!file.exists() && !file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        try {
            val os: OutputStream = FileOutputStream(file)
            os.write(jpegData)
            os.flush()
            os.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getPicFromBytes(
        bytes: ByteArray,
        opts: BitmapFactory.Options? = null
    ): Bitmap? {
        return if (opts != null) {
            BitmapFactory.decodeByteArray(
                bytes, 0, bytes.size,
                opts
            )
        } else {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}