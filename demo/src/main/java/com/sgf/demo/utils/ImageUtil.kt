package com.sgf.demo.utils

import android.graphics.*
import android.media.Image
import java.io.*


object ImageUtil {

    fun Image.yuv420888ToNV21(): ByteArray {
        val w: Int = width
        val h: Int = height
        // size是宽乘高的1.5倍 可以通过ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888)得到
        val i420Size = w * h * 3 / 2
        return yuv420888ToNV21(ByteArray(i420Size))
    }

    fun Image.yuv420888ToNV21(putNv21: ByteArray): ByteArray {
        var nv21 = putNv21
        val w: Int = width
        val h: Int = height
        // size是宽乘高的1.5倍 可以通过ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888)得到
        val i420Size = w * h * 3 / 2
        if (i420Size != putNv21.size) {
            nv21 = ByteArray(i420Size)
        }
        val planes: Array<Image.Plane> = getPlanes()
        //remaining0 = rowStride*(h-1)+w => 27632= 192*143+176 Y分量byte数组的size
        val remaining0 = planes[0].buffer.remaining()
        val remaining1 = planes[1].buffer.remaining()
        //remaining2 = rowStride*(h/2-1)+w-1 =>  13807=  192*71+176-1 V分量byte数组的size
        val remaining2 = planes[2].buffer.remaining()
        //获取pixelStride，可能跟width相等，可能不相等
        val pixelStride = planes[2].pixelStride
        val rowOffest = planes[2].rowStride
        //分别准备三个数组接收YUV分量。
        val yRawSrcBytes = ByteArray(remaining0)
        val uRawSrcBytes = ByteArray(remaining1)
        val vRawSrcBytes = ByteArray(remaining2)
        planes[0].buffer[yRawSrcBytes]
        planes[1].buffer[uRawSrcBytes]
        planes[2].buffer[vRawSrcBytes]
        if (pixelStride == width) {
            //两者相等，说明每个YUV块紧密相连，可以直接拷贝
            System.arraycopy(yRawSrcBytes, 0, nv21, 0, rowOffest * h)
            System.arraycopy(vRawSrcBytes, 0, nv21, rowOffest * h, rowOffest * h / 2 - 1)
        } else {
            //根据每个分量的size先生成byte数组
            val ySrcBytes = ByteArray(w * h)
            val uSrcBytes = ByteArray(w * h / 2 - 1)
            val vSrcBytes = ByteArray(w * h / 2 - 1)
            for (row in 0 until h) {
                //源数组每隔 rowOffest 个bytes 拷贝 w 个bytes到目标数组
                System.arraycopy(yRawSrcBytes, rowOffest * row, ySrcBytes, w * row, w)
                //y执行两次，uv执行一次
                if (row % 2 == 0) {
                    //最后一行需要减一
                    if (row == h - 2) {
                        System.arraycopy(
                            vRawSrcBytes,
                            rowOffest * row / 2,
                            vSrcBytes,
                            w * row / 2,
                            w - 1
                        )
                    } else {
                        System.arraycopy(
                            vRawSrcBytes,
                            rowOffest * row / 2,
                            vSrcBytes,
                            w * row / 2,
                            w
                        )
                    }
                }
            }
            //yuv拷贝到一个数组里面
            System.arraycopy(ySrcBytes, 0, nv21, 0, w * h)
            System.arraycopy(vSrcBytes, 0, nv21, w * h, w * h / 2 - 1)
        }
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