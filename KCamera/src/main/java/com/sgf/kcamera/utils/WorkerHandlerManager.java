package com.sgf.kcamera.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 全局工作线程
 */
public class WorkerHandlerManager {
    public enum Tag {
        T_TYPE_DATA("data-thread"),
        T_TYPE_BUSINESS("business-thread"),
        T_TYPE_IMAGE_SURFACE("image-surface-thread"),
        T_TYPE_CAMERA("camera-thread");
        protected final String tag;
        Tag(String tag) {
            this.tag = tag;
        }
    }

    private final Map<Tag, WorkerThread> mHandlerThreadMap;
    private WorkerHandlerManager() {
        mHandlerThreadMap = new HashMap<>(Tag.values().length);
        for (Tag tag : Tag.values()) {
            mHandlerThreadMap.put(tag, new WorkerThread(tag));
        }
    }

    private WorkerThread obtain(Tag tag) {
        if (mHandlerThreadMap.containsKey(tag)) {
            return mHandlerThreadMap.get(tag);
        }
        return null;
    }

    public static Looper getLooper(Tag tag) {
        return Objects.requireNonNull(WorkerHandlerManager.getInstance().obtain(tag)).getLooper();
    }

    public static Handler getHandler(Tag tag) {
        return Objects.requireNonNull(WorkerHandlerManager.getInstance().obtain(tag)).getHandler();
    }

    public static Scheduler getScheduler(Tag tag) {
        return AndroidSchedulers.from(WorkerHandlerManager.getLooper(tag));
    }

    public void release() {
        for (WorkerThread handlerThread : mHandlerThreadMap.values()) {
            handlerThread.stop();
        }
    }

    public static WorkerHandlerManager getInstance() {
        return Create.workerHandlerManager;
    }

    private static class Create {
        static WorkerHandlerManager workerHandlerManager = new WorkerHandlerManager();
    }

    private static class WorkerThread {
        private HandlerThread mHandlerThread;
        private Handler mHandler;

        private WorkerThread(Tag tag) {
            mHandlerThread = new HandlerThread(tag.tag);
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }

        private Looper getLooper() {
            return mHandler.getLooper();
        }

        private Handler getHandler() { return mHandler; }

        private void stop() {
            mHandlerThread.quitSafely();

            try {
                mHandlerThread.join();
                mHandlerThread = null;
                mHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
