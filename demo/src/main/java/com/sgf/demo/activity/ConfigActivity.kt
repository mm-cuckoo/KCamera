package com.sgf.demo.activity

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.sgf.demo.R
import com.sgf.demo.config.ConfigKey

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        findViewById<Button>(R.id.btn_ok).setOnClickListener {
            finish()
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
}