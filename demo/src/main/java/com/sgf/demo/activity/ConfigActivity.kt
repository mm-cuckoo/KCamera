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
    private lateinit var backPreviewSize : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        fontPreviewSize = findViewById(R.id.font_preivew_size)
        backPreviewSize = findViewById(R.id.back_preivew_size)

        val defFontSize = ConfigKey.getSize(ConfigKey.FONT_PREVIEW_SIZE, ConfigKey.DEF_FONT_PREVIEW_SIZE)
        fontPreviewSize.text = "预览,YUV,拍照 : ${defFontSize.width} * ${defFontSize.height}"

        val defBackSize = ConfigKey.getSize(ConfigKey.BACK_PREVIEW_SIZE, ConfigKey.DEF_BACK_PREVIEW_SIZE)
        backPreviewSize.text = "预览,YUV,拍照 : ${defBackSize.width} * ${defBackSize.height}"


        findViewById<Button>(R.id.btn_ok).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_change_font_preview_size).setOnClickListener {
            showDialog("1")
        }

        findViewById<Button>(R.id.btn_change_back_preview_size).setOnClickListener {
            showDialog("0")
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
//                val rbJpeg = showGroup.findViewById<RadioButton>(R.id.rb_show_jpeg)
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
            }
        }
    }

    private fun showDialog(cameraId: String) {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        // Create and show the dialog.
        val newFragment = SizeSelectDialog(cameraId) {
            if (cameraId == "0") {
                backPreviewSize.text = "预览,YUV,拍照 : ${it.width} * ${it.height}"
                ConfigKey.putSize(ConfigKey.BACK_PREVIEW_SIZE, it)
            } else if (cameraId == "1") {
                fontPreviewSize.text = "预览,YUV,拍照 : ${it.width} * ${it.height}"
                ConfigKey.putSize(ConfigKey.FONT_PREVIEW_SIZE, it)
            }

        }
        newFragment.show(ft, "dialog")
    }

}