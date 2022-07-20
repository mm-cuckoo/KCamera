package com.sgf.demo.reader

import android.media.Image
import com.sgf.demo.utils.ImageUtil.yuv420888ToNV21

class ImageByteArrayWithLock(byteSize : Int) {

    @Volatile
    private var isLock = false
    private var imageBuffer : ByteArray = ByteArray(byteSize)
//    private var swapBuffer : ByteArray = ByteArray(byteSize)

    var width : Int = 0
    var height : Int = 0

    @Synchronized
    fun requestLock() : Boolean {
        return if (isLock) {
            false
        } else {
            isLock = true
            true
        }
    }

    fun getImageByteArray() : ByteArray {
        return imageBuffer
    }

    @Synchronized
    fun putImageByte(image: Image, isNeedRotate : Boolean) {
        width = image.width
        height = image.height
        imageBuffer = image.yuv420888ToNV21()
    }

    @Synchronized
    fun unLockByteArray() {
        isLock = false
    }
}