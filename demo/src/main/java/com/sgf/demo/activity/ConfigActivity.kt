package com.sgf.demo.activity

import android.os.Bundle
import android.util.Size
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sgf.demo.R
import com.sgf.demo.config.ConfigKey
import com.sgf.demo.config.SizeSelectDialog

class ConfigActivity : AppCompatActivity() {

    private lateinit var fontPreviewSize : TextView
    private lateinit var fontYuvSize : TextView
    private lateinit var fontPicSize : TextView
    private lateinit var backPreviewSize : TextView
    private lateinit var backYuvSize : TextView
    private lateinit var backPicSize : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        fontPreviewSize = findViewById(R.id.font_preview_size)
        fontYuvSize = findViewById(R.id.font_preview_yuv_size)
        fontPicSize = findViewById(R.id.font_pic_size)
        backPreviewSize = findViewById(R.id.back_preview_size)
        backYuvSize = findViewById(R.id.back_preview_yuv_size)
        backPicSize = findViewById(R.id.back_pic_size)

        val defFontPreviewSize = ConfigKey.getSize(ConfigKey.FONT_PREVIEW_SIZE, ConfigKey.DEF_FONT_PREVIEW_SIZE)
        fontPreviewSize.text = "预览 : ${defFontPreviewSize.width} * ${defFontPreviewSize.height}"

        val defFontYuvSize = ConfigKey.getSize(ConfigKey.FONT_YUV_SIZE, ConfigKey.DEF_FONT_YUV_SIZE)
        fontYuvSize.text = "YUV : ${defFontYuvSize.width} * ${defFontYuvSize.height}"

        val defPicYuvSize = ConfigKey.getSize(ConfigKey.FONT_PIC_SIZE, ConfigKey.DEF_FONT_PIC_SIZE)
        fontPicSize.text = "拍照 : ${defPicYuvSize.width} * ${defPicYuvSize.height}"

        val defBackPreviewSize = ConfigKey.getSize(ConfigKey.BACK_PREVIEW_SIZE, ConfigKey.DEF_BACK_PREVIEW_SIZE)
        backPreviewSize.text = "预览 : ${defBackPreviewSize.width} * ${defBackPreviewSize.height}"

        val defBackYuvSize = ConfigKey.getSize(ConfigKey.BACK_PREVIEW_SIZE, ConfigKey.DEF_BACK_PREVIEW_SIZE)
        backYuvSize.text = "YUV: ${defBackYuvSize.width} * ${defBackYuvSize.height}"

        val defBackPicSize = ConfigKey.getSize(ConfigKey.BACK_PREVIEW_SIZE, ConfigKey.DEF_BACK_PREVIEW_SIZE)
        backPicSize.text = "拍照 : ${defBackPicSize.width} * ${defBackPicSize.height}"


        findViewById<Button>(R.id.btn_ok).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_change_font_preview_size).setOnClickListener {
            showDialog("1") {
                fontPreviewSize.text = "预览 : ${it.width} * ${it.height}"
                ConfigKey.putSize(ConfigKey.FONT_PREVIEW_SIZE, it)
            }
        }

        findViewById<Button>(R.id.btn_change_font_preview_yuv_size).setOnClickListener {
            showDialog("1") {
                fontYuvSize.text = "YUV : ${it.width} * ${it.height}"
                ConfigKey.putSize(ConfigKey.FONT_YUV_SIZE, it)
            }
        }

        findViewById<Button>(R.id.btn_change_font_pic_size).setOnClickListener {
            showDialog("1") {
                fontPicSize.text = "拍照 : ${it.width} * ${it.height}"
                ConfigKey.putSize(ConfigKey.FONT_PIC_SIZE, it)
            }
        }

        findViewById<Button>(R.id.btn_change_back_preview_size).setOnClickListener {
            showDialog("0") {
                backPreviewSize.text = "预览 : ${it.width} * ${it.height}"
                ConfigKey.putSize(ConfigKey.BACK_PREVIEW_SIZE, it)
            }
        }

        findViewById<Button>(R.id.btn_change_back_preview_yuv_size).setOnClickListener {
            showDialog("0") {
                backYuvSize.text = "YUV : ${it.width} * ${it.height}"
                ConfigKey.putSize(ConfigKey.BACK_YUV_SIZE, it)
            }
        }

        findViewById<Button>(R.id.btn_change_back_pic_size).setOnClickListener {
            showDialog("0") {
                backPreviewSize.text = "拍照 : ${it.width} * ${it.height}"
                ConfigKey.putSize(ConfigKey.BACK_PIC_SIZE, it)
            }
        }

        val preYuv = findViewById<CheckBox>(R.id.cb_show_pre_yuv)
        preYuv.isChecked = ConfigKey.getBoolean(ConfigKey.SHOW_PRE_YUV, false)
        preYuv.setOnCheckedChangeListener { buttonView, isChecked ->
            ConfigKey.pushBoolean(ConfigKey.SHOW_PRE_YUV, isChecked)
        }


        val jpegPic = findViewById<CheckBox>(R.id.cb_jpeg_pic)
        jpegPic.isChecked = ConfigKey.getBoolean(ConfigKey.TAKE_JPEG_PIC, false)
        jpegPic.setOnCheckedChangeListener { buttonView, isChecked ->
            ConfigKey.pushBoolean(ConfigKey.TAKE_JPEG_PIC, isChecked)
        }

        val yuvToJpeg = findViewById<CheckBox>(R.id.cb_yuv_to_jpeg_pic)
        yuvToJpeg.isChecked = ConfigKey.getBoolean(ConfigKey.TAKE_YUV_TO_JPEG_PIC, false)
        yuvToJpeg.setOnCheckedChangeListener { buttonView, isChecked ->
            ConfigKey.pushBoolean(ConfigKey.TAKE_YUV_TO_JPEG_PIC, isChecked)
        }

        val pngPic = findViewById<CheckBox>(R.id.cb_png_pic)
        pngPic.isChecked = ConfigKey.getBoolean(ConfigKey.TAKE_PNG_PIC, false)
        findViewById<CheckBox>(R.id.cb_png_pic).setOnCheckedChangeListener { buttonView, isChecked ->
            ConfigKey.pushBoolean(ConfigKey.TAKE_PNG_PIC, isChecked)
        }
        val savePreImage = findViewById<CheckBox>(R.id.cb_save_pre_image)
        savePreImage.isChecked = ConfigKey.getBoolean(ConfigKey.SAVE_PRE_TO_JPEG, false)
        savePreImage.setOnCheckedChangeListener { buttonView, isChecked ->
            ConfigKey.pushBoolean(ConfigKey.SAVE_PRE_TO_JPEG, isChecked)
        }

        val zlSensor = findViewById<CheckBox>(R.id.cb_sensor)
        zlSensor.isChecked = ConfigKey.getBoolean(ConfigKey.OPEN_SENSOR, false)
        zlSensor.setOnCheckedChangeListener { buttonView, isChecked ->
            ConfigKey.pushBoolean(ConfigKey.OPEN_SENSOR, isChecked)
        }

        val showGroup = findViewById<RadioGroup>(R.id.rg_show_group)
        when(ConfigKey.getInt(ConfigKey.SHOW_PIC_TYPE)) {
            ConfigKey.SHOW_JPEG_VALUE -> {
                showGroup.check(R.id.rb_show_jpeg)
            }

            ConfigKey.SHOW_YUV_OT_JPEG_VALUE -> {
                showGroup.check(R.id.rb_show_yuv_to_jpeg)
            }

            ConfigKey.SHOW_PNG_VALUE -> {
                showGroup.check(R.id.rb_show_png)
            }

            ConfigKey.SHOW_NONE_VALUE -> {
                showGroup.check(R.id.rb_show_none)
            }

        }

        findViewById<RadioGroup>(R.id.rg_show_group).setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.rb_show_jpeg -> {
                    ConfigKey.pushInt(ConfigKey.SHOW_PIC_TYPE, ConfigKey.SHOW_JPEG_VALUE)
                }

                R.id.rb_show_yuv_to_jpeg -> {
                    ConfigKey.pushInt(ConfigKey.SHOW_PIC_TYPE, ConfigKey.SHOW_YUV_OT_JPEG_VALUE)
                }

                R.id.rb_show_png -> {
                    ConfigKey.pushInt(ConfigKey.SHOW_PIC_TYPE, ConfigKey.SHOW_PNG_VALUE)
                }
                R.id.rb_show_none -> {
                    ConfigKey.pushInt(ConfigKey.SHOW_PIC_TYPE, ConfigKey.SHOW_NONE_VALUE)
                }
            }
        }

        val cameraIdGroup = findViewById<RadioGroup>(R.id.rg_camera_id_group)
        when(ConfigKey.getInt(ConfigKey.CAMERA_ID_TYPE)) {
            ConfigKey.FONT_CAMERA_ID -> {
                cameraIdGroup.check(R.id.rb_front_camera)
            }

            ConfigKey.BACK_CAMERA_ID -> {
                cameraIdGroup.check(R.id.rb_back_camera)
            }
        }

        findViewById<RadioGroup>(R.id.rg_camera_id_group).setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.rb_front_camera -> {
                    ConfigKey.pushInt(ConfigKey.CAMERA_ID_TYPE, ConfigKey.FONT_CAMERA_ID)
                }

                R.id.rb_show_yuv_to_jpeg -> {
                    ConfigKey.pushInt(ConfigKey.CAMERA_ID_TYPE, ConfigKey.BACK_CAMERA_ID)
                }
            }
        }
    }

    private fun showDialog(cameraId: String, selectCall : (Size) -> Unit) {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        val newFragment = SizeSelectDialog(cameraId, selectCall)
        // Create and show the dialog.
//        val newFragment = SizeSelectDialog(cameraId) {
//            if (cameraId == "0") {
//                backPreviewSize.text = "预览,YUV,拍照 : ${it.width} * ${it.height}"
//                ConfigKey.putSize(ConfigKey.BACK_PREVIEW_SIZE, it)
//            } else if (cameraId == "1") {
//                fontPreviewSize.text = "预览,YUV,拍照 : ${it.width} * ${it.height}"
//                ConfigKey.putSize(ConfigKey.FONT_PREVIEW_SIZE, it)
//            }
//
//        }
        newFragment.show(ft, "dialog")
    }

}