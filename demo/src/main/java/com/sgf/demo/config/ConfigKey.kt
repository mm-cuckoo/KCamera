package com.sgf.demo.config

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Size
import com.sgf.kcamera.log.KLog

@SuppressLint("CommitPrefEdits")
object ConfigKey {

    private var sp : SharedPreferences ? = null

    private const val SP_NAME = "kcamera_config"

    val DEF_FONT_PREVIEW_SIZE = Size(1280,960)
    val DEF_FONT_YUV_SIZE = DEF_FONT_PREVIEW_SIZE
    val DEF_FONT_PIC_SIZE = DEF_FONT_PREVIEW_SIZE
    val DEF_BACK_PREVIEW_SIZE = Size(1280,960)
    val DEF_BACK_YUV_SIZE = DEF_BACK_PREVIEW_SIZE
    val DEF_BACK_PIC_SIZE = DEF_BACK_PREVIEW_SIZE

    const val FONT_PREVIEW_SIZE = "font_preview_size"
    const val FONT_YUV_SIZE = "font_yuv_size"
    const val FONT_PIC_SIZE = "font_pic_size"

    const val BACK_PREVIEW_SIZE = "back_preview_size"
    const val BACK_YUV_SIZE = "back_yuv_size"
    const val BACK_PIC_SIZE = "back_pic_size"

    const val OPEN_SENSOR = "open_sensor"

    const val TAKE_JPEG_PIC = "take_jpeg_pic"
    const val TAKE_YUV_TO_JPEG_PIC = "take_yuv_to_jpeg_pic"
    const val TAKE_PNG_PIC = "take_png_pic"

    const val SAVE_PRE_TO_JPEG = "save_pre_to_jpeg"

    const val SHOW_PRE_YUV = "show_pre_yuv"
    const val SHOW_PIC_TYPE = "show_pic_type"

    const val SHOW_NONE_VALUE = 0
    const val SHOW_JPEG_VALUE = 1
    const val SHOW_YUV_OT_JPEG_VALUE = 2
    const val SHOW_PNG_VALUE = 3

    const val CAMERA_ID_TYPE = "camera_id_type"
    const val BACK_CAMERA_ID = 0
    const val FONT_CAMERA_ID = 1


    fun init(context: Context) {
        sp = context.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE)

        var takeType = getBoolean(TAKE_JPEG_PIC, false)
        if (!takeType) {
            takeType = getBoolean(TAKE_YUV_TO_JPEG_PIC, false)
        }

        if (!takeType) {
            takeType = getBoolean(TAKE_PNG_PIC, false)
        }

        if (!takeType) {
            pushBoolean(TAKE_YUV_TO_JPEG_PIC, true)
        }

        val showYUV = getBoolean(SHOW_PRE_YUV, false)
        if (!showYUV) {
            pushBoolean(SHOW_PRE_YUV, true)
        }

        val showPicType = getInt(SHOW_PIC_TYPE, -1)
        if (showPicType < 0) {
            pushInt(SHOW_PIC_TYPE, SHOW_YUV_OT_JPEG_VALUE)
        }

        val cameraIdType = getInt(CAMERA_ID_TYPE, -1)
        if (cameraIdType < 0) {
            pushInt(CAMERA_ID_TYPE, BACK_CAMERA_ID)
        }
    }

    fun pushBoolean(key: String, value : Boolean) {
        KLog.d("key:$key   value:$value")
        sp?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String, def: Boolean) : Boolean {
        return sp?.getBoolean(key, def)?: def
    }

    fun pushInt(key: String, value : Int) {
        sp?.edit()?.putInt(key, value)?.apply()
    }

    fun getInt(key: String, def: Int = 0) : Int {
        return sp?.getInt(key, def)?: def
    }

    fun putSize(key : String, value: Size)  {
        val putValue = "${value.width}*${value.height}"
        sp?.edit()?.putString(key, putValue)?.apply()
    }

    fun getSize(key: String, def:Size) : Size {
        val value = sp?.getString(key, "")
        if (value.isNullOrEmpty()) {
            return def
        } else {
            val valueTmp = value.split("*")
            return Size(valueTmp[0].toInt(), valueTmp[1].toInt())
        }

    }

}