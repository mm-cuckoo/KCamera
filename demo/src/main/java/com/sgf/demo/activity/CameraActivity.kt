package com.sgf.demo.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CaptureResult
import android.os.*
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import com.sgf.demo.*
import com.sgf.demo.config.ConfigKey
import com.sgf.demo.config.SizeSelectDialog
import com.sgf.demo.reader.ImageByteArrayWithLock
import com.sgf.demo.reader.ImageDataListener
import com.sgf.demo.utils.FilePathUtils
import com.sgf.demo.utils.ImageUtil
import com.sgf.demo.utils.OrientationFilter
import com.sgf.demo.utils.OrientationSensorManager
import com.sgf.kcamera.CameraStateListener
import com.sgf.kcamera.CaptureStateListener
import com.sgf.kcamera.KCamera
import com.sgf.kcamera.log.KLog
import com.sgf.kcamera.surface.PreviewSurfaceProvider
import com.sgf.kgl.camera.CameraGLView
import com.sgf.kgl.camera.video.VideoRecordManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() , CaptureStateListener,
    ImageDataListener {

    private lateinit var preview : AutoFitTextureView
    private lateinit var glPreview : CameraGLView

//    private lateinit var previewProvider : PreviewSurfaceProvider
//    private lateinit var previewProvider2 : PreviewSurfaceProvider
    private lateinit var glPreviewProvider : PreviewSurfaceProvider

    private lateinit var cameraInfo: TextView

    private lateinit var seekEv : SeekBar
    private lateinit var seekZoom : SeekBar
    private lateinit var focusView : FocusView

    private lateinit var preYuvView : ImageView
    private lateinit var picView : ImageView
    private lateinit var previewTextView : TextView
    private lateinit var picTextView : TextView
    private lateinit var customValue : EditText


    private lateinit var kCamera: KCamera
    private lateinit var cameraRequest : CameraRequest
    private lateinit var orientationFilter : OrientationFilter
    private lateinit var videoRecordManager : VideoRecordManager

    private var cameraEnable = false

    private val handler = Handler(Looper.getMainLooper())

    private var videoPath : String = ""
    private var startPicTime : Long = 0
    private var isMirror = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        cameraRequest = CameraRequest(this)
        val manager: OrientationSensorManager = OrientationSensorManager.getInstance()
        orientationFilter = OrientationFilter(manager)

        preview = findViewById(R.id.preview)
        glPreview = findViewById(R.id.gl_preview)
        picTextView = findViewById(R.id.tv_pic_text)
        cameraInfo = findViewById(R.id.camera_info)
        preYuvView = findViewById(R.id.pre_view)
        previewTextView = findViewById(R.id.tv_pre_view_text)
        picView = findViewById(R.id.pic_view)
        picTextView = findViewById(R.id.tv_pic_text)
        customValue = findViewById(R.id.et_custom_value)

        focusView = FocusView(this)
        focusView.initFocusArea(400, 400)
        focusView.visibility = View.GONE
        findViewById<ConstraintLayout>(R.id.root_view).addView(focusView)
//        previewProvider = PreviewSurfaceProviderImpl(preview)
//        previewProvider2 = PreviewSurfaceProviderImpl(surfaceView1)
        glPreviewProvider = GLViewProvider(glPreview)
        videoRecordManager = VideoRecordManager(glPreview)

        kCamera = KCamera(this)
        preview.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                orientationFilter.setOnceListener {
                    kCamera.resetFocus()
                }
                kCamera.setFocus(event.x, event.y)
                focusView.moveToPosition(event.x, event.y)
            }
            true
        }

        seekEv = findViewById(R.id.seek_ev)
//        seekEv.max = kCamera.evRange.upper
//        seekEv.min = kCamera.evRange.lower
        seekEv.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                kCamera.setEv(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekZoom = findViewById(R.id.seek_focus)
        seekZoom.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                kCamera.setZoom(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        findViewById<Button>(R.id.btn_set_custom).setOnClickListener {
            customValue.text?.let { ed->
                kCamera.setCustomRequest { cameraId, builder ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        val customerKey: CaptureRequest.Key<Int> = CaptureRequest.Key<Int>(
//                            "XXXXXX",
//                            Int::class.java)
//                        crb.set<Int>(customerKey, it.toString().toInt())
                    }
                }
            }
        }

        findViewById<Button>(R.id.btn_hide_or_show).setOnClickListener {
            val btnLayout = findViewById<Group>(R.id.btn_id_group)
            if (btnLayout.visibility == View.VISIBLE) {
                btnLayout.visibility = View.INVISIBLE
                (it as Button).text = "显示操作栏"
            } else {
                btnLayout.visibility = View.VISIBLE
                (it as Button).text = "隐藏操作栏"
            }
        }


        findViewById<Button>(R.id.btn_open_page).setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            this.startActivity(intent)
        }

        findViewById<Button>(R.id.btn_config).setOnClickListener {
            val intent = Intent(this, ConfigActivity::class.java)
            this.startActivity(intent)
        }

        findViewById<Button>(R.id.btn_close_page).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn_custom).setOnClickListener {
            kCamera.openCamera(cameraRequest.getFont2Request(glPreviewProvider,this).builder(), cameraListener)

        }

        findViewById<Button>(R.id.btn_back_camera).setOnClickListener {
            if (cameraEnable) {
                glPreview.onPause()
//                cameraEnable = false
//                glPreview.setAspectRatio(cameraRequest.getBackSize().width, cameraRequest.getBackSize().height)
//                kCamera.openCamera(cameraRequest.getBackRequest(glPreviewProvider,this).builder(), cameraListener)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_font_camera).setOnClickListener {
            if (cameraEnable) {
                glPreview.onResume()
//                cameraEnable = false
//                glPreview.setAspectRatio(cameraRequest.getFontSize().width, cameraRequest.getFontSize().height)
//                kCamera.openCamera(cameraRequest.getFontRequest(glPreviewProvider,this).builder(), cameraListener)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_capture_pic).setOnClickListener {
            if (cameraEnable) {
                startPicTime = System.currentTimeMillis()
                kCamera.takePic(this)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_start_video).setOnClickListener {
            if (cameraEnable) {
                kCamera.cameraId?.let { id->
                    if (id == "0") {
                        glPreview.setVideoSize(cameraRequest.getBackSize().width, cameraRequest.getBackSize().height)
                    } else {
                        glPreview.setVideoSize(cameraRequest.getFontSize().width, cameraRequest.getFontSize().height)
                    }
                    videoPath = FilePathUtils.getVideoPath()
                    videoRecordManager.startRecording(videoPath)
                }
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_stop_video).setOnClickListener {
            if (cameraEnable) {
                videoRecordManager.stopRecording()
                Toast.makeText(this, "video path: $videoPath ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_change_size).setOnClickListener {
            if (cameraEnable) {
                showDialog(kCamera.cameraId)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btn_mirror).setOnClickListener {
            if (cameraEnable) {
                isMirror = !isMirror
                glPreview.setMirrorView(isMirror)
            } else {
                Toast.makeText(this, "设备没有 Ready ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cameraRequest.reloadSize()
        glPreview.onResume()
        picView.setImageBitmap(null)
        preYuvView.setImageBitmap(null)
        if (ConfigKey.getBoolean(ConfigKey.SHOW_PRE_YUV, false)) {
            previewTextView.visibility = View.VISIBLE
            preYuvView.visibility = View.VISIBLE
        } else {
            previewTextView.visibility = View.GONE
            preYuvView.visibility = View.GONE
        }
        if (ConfigKey.getInt(ConfigKey.SHOW_PIC_TYPE, ConfigKey.SHOW_NONE_VALUE) != ConfigKey.SHOW_NONE_VALUE) {
            picTextView.visibility = View.VISIBLE
            picView.visibility = View.VISIBLE
        } else {
            picTextView.visibility = View.GONE
            picView.visibility = View.GONE
        }

        if (ConfigKey.getBoolean(ConfigKey.TAKE_JPEG_PIC, false) ||
            ConfigKey.getBoolean(ConfigKey.TAKE_YUV_TO_JPEG_PIC, false) ||
            ConfigKey.getBoolean(ConfigKey.TAKE_PNG_PIC, false)) {
            findViewById<Button>(R.id.btn_capture_pic).visibility = View.VISIBLE
        } else {
            findViewById<Button>(R.id.btn_capture_pic).visibility = View.GONE
        }

        handler.postDelayed(frameCountRunner, 1000)
        orientationFilter.onResume()

        if (ConfigKey.getInt(ConfigKey.CAMERA_ID_TYPE) == ConfigKey.FONT_CAMERA_ID) {
            glPreview.setAspectRatio(cameraRequest.getFontSize().width, cameraRequest.getFontSize().height)
            kCamera.openCamera(cameraRequest.getFontRequest(glPreviewProvider,this).builder(), cameraListener)
        } else {
            glPreview.setAspectRatio(cameraRequest.getBackSize().width, cameraRequest.getBackSize().height)
            kCamera.openCamera(cameraRequest.getBackRequest(glPreviewProvider,this).builder(), cameraListener)
        }
    }

    override fun onPause() {
        super.onPause()
        videoRecordManager.stopRecording()
        handler.removeCallbacksAndMessages(null)
        orientationFilter.onPause()
        kCamera.closeCamera()
        glPreview.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        orientationFilter.release()
    }

    private val cameraListener = object: CameraStateListener {
        override fun onFirstFrameCallback() {
            glPreview.onResume()
            cameraEnable = true
            kCamera.cameraId?.let {
                glPreview.setMirrorView(isMirror)
            }
        }

        override fun onCameraClosed(closeCode: Int) {

        }

        override fun onFocusStateChange(state: Int) {
            when (state) {
                CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN -> {
                    focusView.post {
                        focusView.startFocus()
                    }
                }
                CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED, CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED -> {
                    focusView.post {
                        focusView.focusSuccess()
                    }
                }
                CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED, CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED -> {
                    focusView.post {
                        focusView.focusFailed()
                    }
                }
                CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN -> {
                }
                CaptureResult.CONTROL_AF_STATE_INACTIVE -> {
                    focusView.post {
                        focusView.hideFocusView()
                    }
                }
            }

        }
    }

    private val frameCountRunner = object : Runnable {
        override fun run() {
            handler.postDelayed(this, 1000)
            cameraInfo.post {
                if (kCamera.cameraId == "0") {
                    cameraInfo.text = "贞率:${cameraRequest.getBackFrameCount()} \n分辨率:${cameraRequest.getBackSize().width} x ${cameraRequest.getBackSize().height}"
                } else if (kCamera.cameraId == "2") {
                    cameraInfo.text = "贞率:${cameraRequest.getFont2FrameCount()} \n分辨率:${cameraRequest.getFont2Size().width} x ${cameraRequest.getFont2Size().height}"
                 } else {
                    cameraInfo.text = "贞率:${cameraRequest.getFontFrameCount()} \n分辨率:${cameraRequest.getFontSize().width} x ${cameraRequest.getFontSize().height}"
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
            glPreview.setAspectRatio(it.width, it.height)
            glPreview.onPause()
            glPreview.onResume()

            if (cameraId == "0") {
                kCamera.openCamera(
                    cameraRequest.getBackRequest(it, glPreviewProvider, this).builder(),
                    cameraListener
                )
            } else if (cameraId == "2") {
                kCamera.openCamera(
                    cameraRequest.getFont2Request(it, glPreviewProvider, this).builder(),
                    cameraListener
                )
            } else {
                kCamera.openCamera(
                    cameraRequest.getFontRequest(it, glPreviewProvider, this).builder(),
                    cameraListener
                )
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

    override fun onPreImageByteArray(
        byteArrayWithLock: ImageByteArrayWithLock,
        width: Int,
        height: Int
    ) {
        if (ConfigKey.getBoolean(ConfigKey.SHOW_PRE_YUV, false)) {

            val jpeg = ImageUtil.nv21ToJPEG(byteArrayWithLock.getImageByteArray(), width, height)
            val bitmap = ImageUtil.getPicFromBytes(jpeg)

            if (ConfigKey.getBoolean(ConfigKey.SAVE_PRE_TO_JPEG, false)) {
                val format = SimpleDateFormat("'/PRE_YUV420_888'_yyyyMMdd_HHmmss'.jpeg'", Locale.getDefault())
                val fileName = format.format(Date())
                val filePath = FilePathUtils.getRootPath() + "/Preview"
                FilePathUtils.checkFolder(filePath)
                KLog.d("createImageReader: pic file path:" + (filePath + fileName))
                var output: FileOutputStream? = null
                try {
                    output = FileOutputStream(File(filePath + fileName))
                    output.write(jpeg)
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    byteArrayWithLock.unLockByteArray()
                    if (null != output) {
                        try {
                            output.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                byteArrayWithLock.unLockByteArray()
            }

            preYuvView.post {
                preYuvView.setImageBitmap(bitmap)
            }
        } else {
            byteArrayWithLock.unLockByteArray()
        }
    }

    override fun onCaptureBitmap(picType : Int, picBitmap: Bitmap ?, savePath: String, captureTime: Long) {
        val useTime = captureTime - startPicTime
        KLog.d("take pic startTime : $startPicTime   back time : $captureTime  use Time: $useTime")
        picBitmap?.let {
            if (ConfigKey.getInt(ConfigKey.SHOW_PIC_TYPE, ConfigKey.SHOW_NONE_VALUE) == picType) {
                picView.post {
                    Toast.makeText(this, "save path: $savePath", Toast.LENGTH_SHORT).show()
                        picView.setImageBitmap(picBitmap)
                    }
            } else {
                handler.post {
                    Toast.makeText(this, "save path: $savePath", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}