package com.sgf.demo.utils

import android.os.Environment
import java.io.File

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

}