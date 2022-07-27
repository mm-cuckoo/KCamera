package com.sgf.demo.config

import com.sgf.kcamera.log.KLog

object ConfigKey {


    const val OPEN_SENSOR = "open_sensor"

    const val TAKE_JPEG_PIC = "take_jpeg_pic"
    const val TAKE_YUV_TO_JPEG_PIC = "take_yuv_to_jpeg_pic"
    const val TAKE_PNG_PIC = "take_png_pic"

    const val SHOW_PRE_YUV = "show_pre_yuv"
    const val SHOW_PIC_TYPE = "show_pic_type"

    const val SHOW_NONE_VALUE = 0
    const val SHOW_JPEG_VALUE = 1
    const val SHOW_YUV_OT_JPEG_VALUE = 2
    const val SHOW_PNG_VALUE = 3


    private val configMap = mutableMapOf<String, Any>()

    init {
        configMap[SHOW_PRE_YUV] = true
    }


    fun pushBoolean(key: String, value : Boolean) {
        KLog.d("key:$key   value:$value")
        configMap[key] = value
    }

    fun getBoolean(key: String, def: Boolean) : Boolean {
        return  configMap[key]?.let { it as Boolean } ?: def
    }

    fun pushInt(key: String, value : Int) {
        configMap[key] = value
    }

    fun getInt(key: String, def: Int = 0) : Int {
        return  configMap[key]?.let { it as Int } ?: def
    }

    fun remove(key: String) {
        configMap.remove(key)
    }

}