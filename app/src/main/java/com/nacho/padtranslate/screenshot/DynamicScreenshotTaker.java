package com.nacho.padtranslate.screenshot;

import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 */
public class DynamicScreenshotTaker implements ScreenshotTaker, ImageTransmogrifier.OnBitmapAvailableListener {
    
    private WindowManager windowManager;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjectionResults mediaProjectionResults;

    private DisplayMetrics realMetrics;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private ImageTransmogrifier.OnBitmapAvailableListener wrappedListener;
    private ImageTransmogrifier transmogrifier;

    public DynamicScreenshotTaker(WindowManager windowManager, MediaProjectionManager mediaProjectionManager, MediaProjectionResults mediaProjectionResults) {
        this.windowManager = windowManager;
        this.mediaProjectionManager = mediaProjectionManager;
        this.mediaProjectionResults = mediaProjectionResults;

        startup();
    }

    private void startup() {
        shutdown();

        if (mediaProjectionResults.canTakeScreenshots()) {
            mMediaProjection = mediaProjectionManager.getMediaProjection(
                    mediaProjectionResults.getScreenshotResultCode(),
                    mediaProjectionResults.getScreenshotResultData());

            Display display = windowManager.getDefaultDisplay();
            realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);
        }
    }

    public void shutdown() {
        cleanupScreenshot();
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void cleanupScreenshot() {
        wrappedListener = null;
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (transmogrifier != null) {
            transmogrifier.close();
            transmogrifier = null;
        }
    }

    public void startScreenshot(ImageTransmogrifier.OnBitmapAvailableListener listener) {
        cleanupScreenshot();

        wrappedListener = listener;
        transmogrifier = new ImageTransmogrifier(realMetrics, this);

        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                realMetrics.widthPixels, realMetrics.heightPixels, realMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                transmogrifier.getSurface(), null, null);
    }

    @Override
    public boolean onBitmapAvailable(Bitmap bitmap) {
        ImageTransmogrifier.OnBitmapAvailableListener listener = wrappedListener;
        cleanupScreenshot();
        return listener.onBitmapAvailable(bitmap);
    }

    @Override
    public void onBitmapFailure(Exception e) {
        ImageTransmogrifier.OnBitmapAvailableListener listener = wrappedListener;
        cleanupScreenshot();
        listener.onBitmapFailure(e);
    }
}
