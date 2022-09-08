package com.sgf.demo.reader

import android.graphics.Bitmap

interface ImageDataListener {
    /**
     * 预览数据回调
     */
    fun onPreImageByteArray(byteArrayWithLock: ImageByteArrayWithLock, width : Int, height: Int)
    fun onPreImageByteArray2(byteArrayWithLock: ImageByteArrayWithLock, width : Int, height: Int)

    fun onCaptureBitmap(picType : Int, picBitmap: Bitmap?, savePath : String, captureTime : Long)
}