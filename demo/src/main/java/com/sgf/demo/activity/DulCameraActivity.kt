package com.sgf.demo.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sgf.demo.AutoFitTextureView
import com.sgf.demo.CameraRequest
import com.sgf.demo.PreviewSurfaceProviderImpl
import com.sgf.demo.R
import com.sgf.demo.config.SizeSelectDialog
import com.sgf.kcamera.CameraStateListener
import com.sgf.kcamera.CaptureStateListener
import com.sgf.kcamera.KDulCamera
import com.sgf.kcamera.log.KLog
import com.sgf.kcamera.surface.PreviewSurfaceProvider

class DulCameraActivity : AppCompatActivity(), CaptureStateListener {

    private lateinit var preview : AutoFitTextureView
    private lateinit var previewFont : AutoFitTextureView
    private lateinit var previewBackProvider : PreviewSurfaceProvider
    private lateinit var previewFontProvider : PreviewSurfaceProvider
    private lateinit var kCamera: KDulCamera
    private lateinit var fontCameraInfo: TextView
    private lateinit var backCameraInfo: TextView
    private lateinit var cameraRequest : CameraRequest

    private var fontBtnEnable = false
    private var backBtnEnable = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dul_camera)
        cameraRequest = CameraRequest(this)
        preview = findViewById(R.id.preview)
        previewFont = findViewById(R.id.font_preview)
        fontCameraInfo = findViewById(R.id.font_camera_info)
        backCameraInfo = findViewById(R.id.back_camera_info)

        previewBackProvider = PreviewSurfaceProviderImpl(preview)
        previewFontProvider = PreviewSurfaceProviderImpl(previewFont)
        kCamera = KDulCamera(this, null, null)

        findViewById<Button>(R.id.btn_open_page).setOnClickListener {
            val intent = Intent(this, DulCameraActivity::class.java)
            this.startActivity(intent)
        }

        findViewById<Button>(R.id.btn_close_page).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_font_close).setOnClickListener {
            kCamera.closeFontCamera()
        }

        findViewById<Button>(R.id.btn_back_close).setOnClickListener {
            kCamera.closeBackCamera()
        }

        findViewById<Button>(R.id.btn_back_camera).setOnClickListener {
//            kCamera.openBackCamera(cameraRequest.getBackRequest(previewBackProvider, ).builder(), backListener)

//            if (backBtnEnable) {
//                backBtnEnable = false
//                kCamera.backCamera(kCamera.backSize, previewProvider, backListener)
//            } else {
//                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
//            }
        }

        findViewById<Button>(R.id.btn_font_camera).setOnClickListener {
//            kCamera.fontCamera(kCamera.fontSize, previewProvider, fontListener)
//            kCamera.openFontCamera(cameraRequest.getFontRequest(previewFontProvider, this).builder(), fontListener)

//            if (fontBtnEnable) {
//                fontBtnEnable = false
//                kCamera.fontCamera(kCamera.fontSize, previewProviderFont, fontListener)
//            } else {
//                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
//            }
        }

        findViewById<Button>(R.id.btn_back_take_pic).setOnClickListener {
            if (backBtnEnable) {
                kCamera.takeBackPic(this)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_font_take_pic).setOnClickListener {
            if (fontBtnEnable) {
                kCamera.takeFontPic(this)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_font_size).setOnClickListener {
            if (fontBtnEnable) {
                showDialog("1")
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_back_size).setOnClickListener {
            if (backBtnEnable) {
                showDialog("0")
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preview.visibility = View.VISIBLE
//        kCamera.openBackCamera(cameraRequest.getBackRequest(previewBackProvider).builder(), backListener)
        handler.postDelayed(frameCountRunner, 1000)
        handler.postDelayed({
//            kCamera.openFontCamera(cameraRequest.getFontRequest(previewFontProvider).builder(), fontListener)
        }, 500)
    }

    override fun onPause() {
        super.onPause()
        kCamera.stopCamera()
    }

    private val fontListener = object: CameraStateListener {
        override fun onFirstFrameCallback() {
            fontBtnEnable = true
        }

        override fun onCameraClosed(closeCode: Int) {

        }

        override fun onFocusStateChange(state: Int) {}
    }

    private val backListener = object: CameraStateListener {
        override fun onFirstFrameCallback() {
            backBtnEnable = true
        }

        override fun onCameraClosed(closeCode: Int) {

        }

        override fun onFocusStateChange(state: Int) {}
    }

    private val frameCountRunner = object : Runnable {
        override fun run() {
            handler.postDelayed(this, 1000)
            fontCameraInfo.post {
                KLog.d("get camera frame ===>")
                fontCameraInfo.text = "贞率:${cameraRequest.getFontFrameCount()} \n分辨率:${cameraRequest.getFontSize().width} x ${cameraRequest.getFontSize().height}"
                backCameraInfo.text = "贞率:${cameraRequest.getBackFrameCount()} \n分辨率:${cameraRequest.getBackSize().width} x ${cameraRequest.getBackSize().height}"
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
//                kCamera.openBackCamera(cameraRequest.getBackRequest(it,previewBackProvider).builder(), backListener)
            } else {
//                kCamera.openFontCamera(cameraRequest.getFontRequest(it,previewFontProvider).builder(), fontListener)
            }
        }
        newFragment.show(ft, "dialog")
    }

    override fun onCaptureStarted() {
        KLog.d("onCaptureStarted===>")
    }

    override fun onCaptureCompleted() {
        KLog.d("onCaptureCompleted===>")
    }

    override fun onCaptureFailed() {
        KLog.d("onCaptureFailed===>")
    }

}