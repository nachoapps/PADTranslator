package com.nacho.padtranslate.screenshot;

/**
 *
 */
public interface ScreenshotTaker {

    void shutdown();
    void startScreenshot(ImageTransmogrifier.OnBitmapAvailableListener listener);
}
