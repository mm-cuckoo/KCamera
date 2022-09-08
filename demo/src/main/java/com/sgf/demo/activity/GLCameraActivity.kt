package com.sgf.demo.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sgf.demo.*
import com.sgf.demo.config.SizeSelectDialog
import com.sgf.demo.reader.ImageByteArrayWithLock
import com.sgf.demo.reader.ImageDataListener
import com.sgf.kcamera.CameraStateListener
import com.sgf.kcamera.KCamera
import com.sgf.kcamera.surface.PreviewSurfaceProvider
import com.sgf.kgl.camera.CameraGLView
import com.sgf.kgl.camera.video.VideoRecordManager
import java.text.SimpleDateFormat
import java.util.*

class GLCameraActivity : AppCompatActivity(), ImageDataListener {

    private lateinit var preview : CameraGLView
    private lateinit var cameraInfo: TextView
    private lateinit var previewProvider : PreviewSurfaceProvider
    private var cameraEnable: Boolean = false

    private lateinit var kCamera: KCamera
    private lateinit var cameraRequest : GLCameraRequest
    private lateinit var videoRecordManager : VideoRecordManager

    private fun getVideoPath() : String {
        val format = SimpleDateFormat("'/video'_yyyyMMdd_HHmmss'.mp4'", Locale.getDefault())
        val fileName = format.format(Date())
        val filePath = Environment.getExternalStorageDirectory().absoluteFile.path
        return filePath + fileName
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_camera)
        preview = findViewById(R.id.preview)
        cameraInfo = findViewById(R.id.camera_info)
        kCamera = KCamera(this)
        cameraRequest = GLCameraRequest(this)
        videoRecordManager = VideoRecordManager(preview)


        previewProvider = GLViewProvider(preview)


        findViewById<Button>(R.id.btn_close_page).setOnClickListener {
            finish()
        }


        findViewById<Button>(R.id.btn_back_camera).setOnClickListener {
            if (cameraEnable) {
                cameraEnable = false
                cameraInfo.text = "分辨率：${cameraRequest.getBackSize().width} x ${cameraRequest.getBackSize().height}"
                preview.setAspectRatio(cameraRequest.getBackSize().width, cameraRequest.getBackSize().height)
                kCamera.openCamera(cameraRequest.getBackRequest(previewProvider,this).builder(), cameraListener)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_font_camera).setOnClickListener {
            if (cameraEnable) {
                cameraEnable = false
                cameraInfo.text = "分辨率：${cameraRequest.getBackSize().width} x ${cameraRequest.getBackSize().height}"
                preview.setAspectRatio(cameraRequest.getFontSize().width, cameraRequest.getFontSize().height)
                kCamera.openCamera(cameraRequest.getFontRequest(previewProvider,this).builder(), cameraListener)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }


        findViewById<Button>(R.id.btn_camera_size).setOnClickListener {
            if (cameraEnable) {
                showDialog(kCamera.cameraId)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_start_record).setOnClickListener {
            if (cameraEnable) {
                kCamera.cameraId?.let { id->
                    if (id == "0") {
                        preview.setVideoSize(cameraRequest.getBackSize().width, cameraRequest.getBackSize().height)
                    } else {
                        preview.setVideoSize(cameraRequest.getFontSize().width, cameraRequest.getFontSize().height)
                    }
                    videoRecordManager.startRecording(getVideoPath())
                }

            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_stop_record).setOnClickListener {
            if (cameraEnable) {
                videoRecordManager.stopRecording()
                Toast.makeText(this, "vdeo path: ${getVideoPath()} ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_camera_size).setOnClickListener {
            if (cameraEnable) {
                showDialog(kCamera.cameraId)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preview.onResume()
        cameraInfo.text = "分辨率：${cameraRequest.getBackSize().width} x ${cameraRequest.getBackSize().height}"
        preview.setAspectRatio(cameraRequest.getBackSize().width, cameraRequest.getBackSize().height)
        kCamera.openCamera(cameraRequest.getBackRequest(previewProvider,this).builder(), cameraListener)

    }

    override fun onPause() {
        super.onPause()
        kCamera.closeCamera()
        preview.onPause()

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
            cameraInfo.text = "分辨率：${it.width} x ${it.height}"
            if (cameraId == "0") {
                preview.setAspectRatio(it.width, it.height)
                kCamera.openCamera(
                    cameraRequest.getBackRequest(it, previewProvider, this).builder(),
                    cameraListener
                )
            } else if (cameraId == "2") {
                kCamera.openCamera(
                    cameraRequest.getFont2Request(it, previewProvider, this).builder(),
                    cameraListener
                )
            } else {
                preview.setAspectRatio(it.width, it.height)
                kCamera.openCamera(
                    cameraRequest.getFontRequest(it, previewProvider, this).builder(),
                    cameraListener
                )
            }
        }
        newFragment.show(ft, "dialog")
    }

    private val cameraListener = object: CameraStateListener {
        override fun onFirstFrameCallback() {
            cameraEnable = true
            preview.setMirrorView(true)
        }

        override fun onCameraClosed(closeCode: Int) {

        }

        override fun onFocusStateChange(state: Int) {

        }
    }

    override fun onPreImageByteArray(
        byteArrayWithLock: ImageByteArrayWithLock,
        width: Int,
        height: Int
    ) {

    }

    override fun onPreImageByteArray2(
        byteArrayWithLock: ImageByteArrayWithLock,
        width: Int,
        height: Int
    ) {

    }
    override fun onCaptureBitmap(picType: Int, picBitmap: Bitmap?, savePath: String, captureTime: Long) {

    }

}