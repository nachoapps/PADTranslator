package com.nacho.padtranslate.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nacho.padtranslate.screenshot.ImageTransmogrifier;
import com.nacho.padtranslate.screenshot.DynamicScreenshotTaker;
import com.nacho.padtranslate.screenshot.ScreenshotTaker;
import com.nacho.padtranslate.util.Diagnostics;
import com.nacho.padtranslate.util.ImageUtil;
import com.nacho.padtranslate.util.Util;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class WindowServiceButton implements ImageTransmogrifier.OnBitmapAvailableListener {
    private ImageView view;

    @DrawableRes
    private int idleRes;
    private ScreenshotTaker screenshotTaker;
    private AtomicBoolean busyLock;

    public WindowServiceButton(
            final Context context,
            @DrawableRes int idleRes,
            final ScreenshotTaker screenshotTaker,
            final AtomicBoolean busyLock) {
        this.idleRes = idleRes;
        this.screenshotTaker = screenshotTaker;
        this.busyLock = busyLock;
        view = new ImageView(context);
        view.setBackgroundColor(Color.WHITE);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (busyLock.get()) {
                            Toast.makeText(context, "Already scanning", Toast.LENGTH_SHORT).show();
                        } else {
                            handlePress();
                        }
                        return true;
                }
                return false;
            }
        });

        setIdle();
    }

    public ImageView getView() {
        return view;
    }

    private void handlePress() {
        busyLock.set(true);
        setScreenshot();
        screenshotTaker.startScreenshot(this);
    }

    protected Context getContext() {
        return getView().getContext();
    }

    @Override
    public final boolean onBitmapAvailable(Bitmap bitmap) {
        setAction();
        new Thread(new Work(bitmap)).start();
        return true;
    }

    @Override
    public final void onBitmapFailure(Exception e) {
        Diagnostics.setLastException(e);
        if (Preferences.useLegacyScreenshots()) {
            Util.toastOnUiThread("Couldn't load screenshot. Try again in a few seconds.");
        } else {
            Util.toastOnUiThread("Screenshot failed.");
        }
        completeRequest();
    }

    private void setScreenshot() {
        setImageOnUiThread(R.drawable.ic_photo_camera_24dp);
    }

    protected void setAction() {
        setImageOnUiThread(R.drawable.ic_sync_24dp);
    }

    protected void setIdle() {
        busyLock.set(false);
        setImageOnUiThread(idleRes);
    }

    protected void setImageOnUiThread(@DrawableRes final int resId) {
        getView().post(new Runnable() {
            @Override
            public void run() {
            view.setImageResource(resId);
            }
        });
    }

    protected void completeRequest() {
        setIdle();
        Diagnostics.sendDiagnostics(view.getContext());
    }

    private class Work implements Runnable {
        private Bitmap bitmap;

        public Work(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            try {
                doWork(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Diagnostics.setLastException(e);
                Util.toastOnUiThread("Action failed. Enable diagnostics for more info.");
            } finally {
                completeRequest();
            }
        }

    }

    protected abstract void doWork(Bitmap bitmap) throws Exception;
}
