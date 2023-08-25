package com.sgf.kcamera.utils

import android.media.Image
import com.sgf.kcamera.log.KLog

class ImageTransform {

    private var yRawSrcBytes : ByteArray? = null
    private var uRawSrcBytes : ByteArray? = null
    private var vRawSrcBytes : ByteArray? = null
    private var ySrcBytes : ByteArray? = null
    private var vSrcBytes : ByteArray? = null

    @Synchronized
    fun onRelease() {
        yRawSrcBytes = null
        uRawSrcBytes = null
        vRawSrcBytes = null
        ySrcBytes = null
        vSrcBytes = null
    }

    @Synchronized
    fun yuv420888ToNV21(image: Image, putNv21: ByteArray) : ByteArray {
        var nv21 = putNv21
        val w: Int = image.width
        val h: Int = image.height
        // size是宽乘高的1.5倍 可以通过ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888)得到
        val i420Size = w * h * 3 / 2
        if (i420Size != putNv21.size) {
            KLog.e("yuv image size != yuv buffer size, image:w : $w, h :$h, size:$i420Size , yuv buffer size:${putNv21.size}")
            nv21 = ByteArray(i420Size)
        }
        val planes: Array<Image.Plane> = image.planes
        //remaining0 = rowStride*(h-1)+w => 27632= 192*143+176 Y分量byte数组的size
        val remaining0 = planes[0].buffer.remaining()
        val remaining1 = planes[1].buffer.remaining()
        //remaining2 = rowStride*(h/2-1)+w-1 =>  13807=  192*71+176-1 V分量byte数组的size
        val remaining2 = planes[2].buffer.remaining()
        //获取pixelStride，可能跟width相等，可能不相等
        val pixelStride = planes[2].pixelStride
        val rowOffest = planes[2].rowStride
        //分别准备三个数组接收YUV分量。
        if (yRawSrcBytes == null || yRawSrcBytes!!.size != remaining0) {
            yRawSrcBytes = ByteArray(remaining0)
        }

        if (uRawSrcBytes == null || uRawSrcBytes!!.size != remaining1) {
            uRawSrcBytes = ByteArray(remaining1)
        }

        if (vRawSrcBytes == null || vRawSrcBytes!!.size != remaining2) {
            vRawSrcBytes = ByteArray(remaining2)
        }

        planes[0].buffer[yRawSrcBytes!!]
        planes[1].buffer[uRawSrcBytes!!]
        planes[2].buffer[vRawSrcBytes!!]
        /**
         * 如果pixelStride 和 width 不相等需要进行字节对齐操作， 如果不对齐图像会出现阶梯横线现象
         * 假如按照64字节对齐，width=720。那么pixelStride=768。其中48位多余的用0x00补齐。这48位就是阶梯型横线出现的原因
         */
        if (pixelStride == w) {
            //两者相等，说明每个YUV块紧密相连，可以直接拷贝
            System.arraycopy(yRawSrcBytes!!, 0, nv21, 0, rowOffest * h)
            System.arraycopy(vRawSrcBytes!!, 0, nv21, rowOffest * h, rowOffest * h / 2 - 1)
        } else {
            //不相等 节对齐操作
            //根据每个分量的size先生成byte数组
            val ySize = w * h
            if (ySrcBytes == null || ySrcBytes!!.size != ySize) {
                ySrcBytes = ByteArray(w * h)
            }
            val vSize = w * h / 2 - 1
            if (vSrcBytes == null || vSrcBytes?.size != vSize) {
                vSrcBytes = ByteArray(w * h / 2 - 1)
            }

//            val uSrcBytes = ByteArray(w * h / 2 - 1)
//            val vSrcBytes = ByteArray(w * h / 2 - 1)
            for (row in 0 until h) {
                //源数组每隔 rowOffest 个bytes 拷贝 w 个bytes到目标数组
                System.arraycopy(yRawSrcBytes!!, rowOffest * row, ySrcBytes!!, w * row, w)
                //y执行两次，uv执行一次
                if (row % 2 == 0) {
                    //最后一行需要减一
                    if (row == h - 2) {
                        System.arraycopy(
                            vRawSrcBytes!!,
                            rowOffest * row / 2,
                            vSrcBytes!!,
                            w * row / 2,
                            w - 1
                        )
                    } else {
                        System.arraycopy(
                            vRawSrcBytes!!,
                            rowOffest * row / 2,
                            vSrcBytes!!,
                            w * row / 2,
                            w
                        )
                    }
                }
            }
            //yuv拷贝到一个数组里面
            System.arraycopy(ySrcBytes!!, 0, nv21, 0, w * h)
            System.arraycopy(vSrcBytes!!, 0, nv21, w * h, w * h / 2 - 1)
        }

        return nv21
    }

}