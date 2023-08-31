package com.sgf.kgl.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.sgf.kcamera.log.KLog;
import com.sgf.kgl.camera.gl.GLDrawer2D;
import com.sgf.kgl.camera.video.encoder.MediaVideoEncoder;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Sub class of GLSurfaceView to display camera preview and write video frame to capturing surface
 */
public class GLView extends GLSurfaceView {

	private static final String TAG = "GLView";
	private final CameraSurfaceRenderer mRenderer;
	private int mVideoWidth, mVideoHeight;
	private int mDisplayRotation;

	public interface GLSurfaceTextureListener {
		void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height);
		void onSurfaceChanged(final SurfaceTexture surfaceTexture, final int width, final int height);
		void onSurfaceTextureDestroyed();
	}

	public GLView(final Context context) {
		this(context, null, 0);
	}

	public GLView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs);
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mDisplayRotation = windowManager.getDefaultDisplay().getRotation() * 90;
		mRenderer = new CameraSurfaceRenderer(this, mDisplayRotation);
		setEGLContextClientVersion(2);	// GLES 2.0, API >= 8
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		KLog.i(TAG,"GLView:mDisplayRotation:" + mDisplayRotation);
		mRenderer.requestRender();
/*		// the frequency of refreshing of camera preview is at most 15 fps
		// and RENDERMODE_WHEN_DIRTY is better to reduce power consumption
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); */
	}

	public void setSurfaceTextureListener(GLSurfaceTextureListener textureListener) {
		if (mRenderer != null) {
			mRenderer.setSurfaceTextureListener(textureListener);
		}
	}

	public int getDisplayRotation() {
		return mDisplayRotation;
	}

	public int getOrientation() {
		Configuration configuration = getContext().getResources().getConfiguration();
		if (configuration == null) {
			return getContext().getApplicationContext().getResources().getConfiguration().orientation;
		} else {
			return configuration.orientation;
		}
	}

	public void setMirrorView(boolean isMirror) {
		mRenderer.setMirror(isMirror);
	}

	public void startDrawFrame() {
		mRenderer.startDrawFrame();
	}

	public void stopDrawFrame() {
		mRenderer.stopDrawFrame();
	}

	public void setVideoSize(final int width, final int height) {
		mVideoWidth = width;
		mVideoHeight = height;
	}

	public int getVideoWidth() {
		return mVideoWidth;
	}

	public int getVideoHeight() {
		return mVideoHeight;
	}

	private SurfaceTexture getSurfaceTexture() {
		KLog.i( TAG,"getSurfaceTexture:");
		return mRenderer != null ? mRenderer.mSTexture : null;
	}

	public boolean isAvailable() {
		return getSurfaceTexture() != null;
	}

	@Override
	public void surfaceDestroyed(final SurfaceHolder holder) {
		KLog.i(TAG, "surfaceDestroyed:");

		mRenderer.onSurfaceDestroyed();
		super.surfaceDestroyed(holder);
	}

	public void setVideoEncoder(final MediaVideoEncoder encoder) {
		KLog.i(TAG,"setVideoEncoder:tex_id=" + mRenderer.hTex + ",encoder=" + encoder);
		queueEvent(new Runnable() {
			@Override
			public void run() {
				synchronized (mRenderer) {
					if (encoder != null) {
						encoder.setEglContext(EGL14.eglGetCurrentContext(), mRenderer.hTex);
					}
					mRenderer.mVideoEncoder = encoder;
				}
			}
		});
	}

//********************************************************************************


	/**
	 * GLSurfaceViewのRenderer
	 */
	private static final class CameraSurfaceRenderer implements Renderer, SurfaceTexture.OnFrameAvailableListener {	// API >= 11

		private final WeakReference<GLView> mWeakParent;
		private SurfaceTexture mSTexture;	// API >= 11
		private int hTex;
		private GLDrawer2D mDrawer;
		private final float[] mStMatrix = new float[16];
		private final float[] mMvpMatrix = new float[16];
		private MediaVideoEncoder mVideoEncoder;
		private GLSurfaceTextureListener mTextureListener;

		private volatile boolean isMirror = false;
		private final int mRotation;

		private final AtomicBoolean isRunning = new AtomicBoolean(true);

		public CameraSurfaceRenderer(final GLView parent, final int rotation) {
			KLog.i(TAG,"CameraSurfaceRenderer:rotation:" + rotation);
			this.mRotation = rotation;
			mWeakParent = new WeakReference<>(parent);
			Matrix.setIdentityM(mMvpMatrix, 0);
		}

		public void setMirror(final boolean isMirror) {
			this.isMirror = isMirror;
			if (mDrawer != null ) {
				mDrawer.setMirror(isMirror);
			}
			startDrawFrame();
		}

		public void startDrawFrame() {
			isRunning.set(true);
			requestRender();
		}

		public void stopDrawFrame() {
			isRunning.set(false);
		}

		public void setSurfaceTextureListener(GLSurfaceTextureListener textureListener) {
			this.mTextureListener = textureListener;
			if (mSTexture != null) {
				final GLView parent = mWeakParent.get();
				if (parent != null) {
					final int view_width = parent.getWidth();
					final int view_height = parent.getHeight();
					mTextureListener.onSurfaceTextureAvailable(mSTexture, view_width, view_height);
				}
			}
		}

		@Override
		public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
			KLog.i(TAG,"====onSurfaceCreated-=======");

			// This renderer required OES_EGL_image_external extension
			final String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);	// API >= 8
//			if (DEBUG) Log.i(TAG, "onSurfaceCreated:Gl extensions: " + extensions);
			if (!extensions.contains("OES_EGL_image_external"))
				throw new RuntimeException("This system does not support OES_EGL_image_external.");
			// create textur ID
			hTex = GLDrawer2D.initTex();
			// create SurfaceTexture with texture ID.
			mSTexture = new SurfaceTexture(hTex);
			mSTexture.setOnFrameAvailableListener(this);
			// clear screen with yellow color so that you can see rendering rectangle
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			final GLView parent = mWeakParent.get();
			// create object for preview display
			mDrawer = new GLDrawer2D(mRotation);
			mDrawer.setMatrix(mMvpMatrix, 0);
			mDrawer.setMirror(isMirror);

			if (mTextureListener != null) {
//				final CameraGLView parent = mWeakParent.get();
				if (parent != null) {
					final int view_width = parent.getWidth();
					final int view_height = parent.getHeight();
					mTextureListener.onSurfaceTextureAvailable(mSTexture, view_width, view_height);
				}
			}
		}

		@Override
		public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
			KLog.i(TAG,"onSurfaceChanged:width" + width + "  height:" + height);
			GLES20.glViewport(0, 0, width , height);
			if (mTextureListener != null) {
				mTextureListener.onSurfaceChanged(mSTexture, width, height);
			}
		}

		/**
		 * when GLSurface context is soon destroyed
		 */
		public void onSurfaceDestroyed() {
			KLog.i(TAG,"onSurfaceDestroyed:");
			if (mDrawer != null) {
				mDrawer.release();
				mDrawer = null;
			}
			if (mSTexture != null) {
				mSTexture.release();
				mSTexture = null;
			}
			if (this.mTextureListener != null) {
				this.mTextureListener.onSurfaceTextureDestroyed();
			}
			GLDrawer2D.deleteTex(hTex);
		}

		private volatile boolean requesrUpdateTex = false;
		private boolean flip = true;
		/**
		 * drawing to GLSurface
		 * we set renderMode to GLSurfaceView.RENDERMODE_WHEN_DIRTY,
		 * this method is only called when #requestRender is called(= when texture is required to update)
		 * if you don't set RENDERMODE_WHEN_DIRTY, this method is called at maximum 60fps
		 */
		@Override
		public void onDrawFrame(final GL10 unused) {

			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

			if (requesrUpdateTex && mSTexture != null) {
				requesrUpdateTex = false;
				// update texture(came from camera)
				mSTexture.updateTexImage();
				// get texture matrix
				mSTexture.getTransformMatrix(mStMatrix);
			}
			// draw to preview screen
			if (mDrawer != null) {
				mDrawer.draw(hTex, mStMatrix);
			}

			flip = !flip;
			if (flip) {	// ~30fps
				synchronized (this) {
					if (mVideoEncoder != null) {
						// notify to capturing thread that the camera frame is available.
//						mVideoEncoder.frameAvailableSoon(mStMatrix);
						mVideoEncoder.frameAvailableSoon(mStMatrix, mMvpMatrix);
					}
				}
			}
		}
		//
		@Override
		public void onFrameAvailable(final SurfaceTexture st) {
			requesrUpdateTex = true;
			if (isRunning.get()) {
				requestRender();
			}
		}

		private void requestRender() {
			final GLView parent = mWeakParent.get();
			if (parent != null) {
				parent.requestRender();
			}
		}
	}

}
