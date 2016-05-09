package com.nacho.padtranslate.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;

import com.nacho.padtranslate.screenshot.DynamicScreenshotTaker;
import com.nacho.padtranslate.screenshot.ScreenshotTaker;
import com.nacho.padtranslate.util.Diagnostics;
import com.nacho.padtranslate.util.ImageUtil;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
public class ScreenshotButton extends WindowServiceButton {

    public ScreenshotButton(Context context, @DrawableRes int idleRes, ScreenshotTaker screenshotTaker, AtomicBoolean busyLock) {
        super(context, idleRes, screenshotTaker, busyLock);
    }

    @Override
    public void doWork(Bitmap bitmap) {
        Bitmap windowCropped = ImageUtil.extractBox(bitmap, WindowService.getWindowRect());
        Bitmap padCropped = ImageUtil.trimToImage(windowCropped).first;

        String fileSuffix = System.currentTimeMillis() + ".png";
        File file = ImageUtil.writeBmpToScreenshots(getView().getContext(), padCropped, "pad_" + fileSuffix);
        diagnostics(file);
    }

    private void diagnostics(File file) {
        if (!Diagnostics.getEnabled()) {
            return;
        }
        Diagnostics.setScreenshotPath(file.getAbsolutePath());
    }
}
