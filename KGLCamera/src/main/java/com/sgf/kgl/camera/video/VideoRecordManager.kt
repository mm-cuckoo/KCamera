package com.sgf.kgl.camera.video

import android.util.Log
import com.sgf.kgl.camera.CameraGLView
import com.sgf.kgl.camera.video.encoder.MediaAudioEncoder
import com.sgf.kgl.camera.video.encoder.MediaEncoder
import com.sgf.kgl.camera.video.encoder.MediaMuxerWrapper
import com.sgf.kgl.camera.video.encoder.MediaVideoEncoder
import java.io.IOException

class VideoRecordManager(val glView: CameraGLView) {

    private var mMuxer: MediaMuxerWrapper? = null
    fun startRecording(videoFilePath: String) {
        Log.i("VideoRecordManager", "videoFilePath:$videoFilePath")

        try {
            mMuxer = MediaMuxerWrapper(videoFilePath) // if you record audio only, ".m4a" is also OK.
            MediaVideoEncoder(
                mMuxer,
                mMediaEncoderListener,
                glView.videoWidth,
                glView.videoHeight
            )
            MediaAudioEncoder(mMuxer, mMediaEncoderListener)
            mMuxer?.prepare()
            mMuxer?.startRecording()
        } catch (e: IOException) {
            Log.e("VideoRecordManager", "startRecording: Exception :", e)
        }
    }

    /**
     * request stop recording
     */
    fun stopRecording() {
        if (mMuxer != null) {
            Log.i("VideoRecordManager", "stopRecording===>")
            mMuxer?.stopRecording()
            mMuxer = null
            // you should not wait here
        }
    }


    private val mMediaEncoderListener: MediaEncoder.MediaEncoderListener = object :
        MediaEncoder.MediaEncoderListener {
        override fun onPrepared(encoder: MediaEncoder) {
            Log.i("VideoRecordManager", "back onPrepared===>")
            if (encoder is MediaVideoEncoder) glView.setVideoEncoder(encoder)
        }

        override fun onStopped(encoder: MediaEncoder) {
            Log.i("VideoRecordManager", "back onStopped===>")
            if (encoder is MediaVideoEncoder) glView.setVideoEncoder(null)
        }
    }
}