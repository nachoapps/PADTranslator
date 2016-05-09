package com.nacho.padtranslate.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.nacho.padtranslate.util.ImageUtil;
import com.nacho.padtranslate.util.Util;

import java.io.File;

/**
 *
 */
public class RecentScreenshotTaker implements ScreenshotTaker {

    private Context context;

    public RecentScreenshotTaker(Context context) {
        this.context = context;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void startScreenshot(ImageTransmogrifier.OnBitmapAvailableListener listener) {
        new Thread(new LoadRecentScreenshotRunnable(listener)).start();
    }

    private class LoadRecentScreenshotRunnable implements Runnable {
        private ImageTransmogrifier.OnBitmapAvailableListener listener;

        public LoadRecentScreenshotRunnable(ImageTransmogrifier.OnBitmapAvailableListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                File screenshot = ImageUtil.getRecentScreenshot();
                Bitmap bmp = ImageUtil.readBmp(context, screenshot);
                if (bmp == null) {
                    Util.toastOnUiThread("Failed to load image. Try again in a few seconds.");
                    listener.onBitmapFailure(new NullPointerException());
                } else {
                    listener.onBitmapAvailable(bmp);
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onBitmapFailure(e);
                Util.toastOnUiThread("Failed: " + e.getMessage());
            }
        }
    }
}
