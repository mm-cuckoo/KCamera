package com.sgf.demo.utils

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FilePathUtils {

    fun getRootPath() : String {
        return Environment.getExternalStorageDirectory().absoluteFile.path + "/KCamera"
    }

    fun checkFolder(filePath : String) {
        val file = File(filePath)
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    fun getVideoPath() : String {
        val format = SimpleDateFormat("'/video'_yyyyMMdd_HHmmss'.mp4'", Locale.getDefault())
        val fileName = format.format(Date())
        val filePath = getRootPath() + "/video"
        checkFolder(filePath)
        return filePath + fileName
    }

}