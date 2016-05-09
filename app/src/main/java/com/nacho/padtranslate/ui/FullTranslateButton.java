package com.nacho.padtranslate.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.util.Pair;

import com.nacho.padtranslate.cloudvision.CloudVisionParams;
import com.nacho.padtranslate.cloudvision.CloudVisionResult;
import com.nacho.padtranslate.cloudvision.CloudVisionScanAsyncTask;
import com.nacho.padtranslate.fulltranslate.FullTranslateAsyncTask;
import com.nacho.padtranslate.fulltranslate.FullTranslateDisplayService;
import com.nacho.padtranslate.screenshot.DynamicScreenshotTaker;
import com.nacho.padtranslate.screenshot.ScreenshotTaker;
import com.nacho.padtranslate.util.Diagnostics;
import com.nacho.padtranslate.util.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
public class FullTranslateButton extends WindowServiceButton {

    public FullTranslateButton(Context context, @DrawableRes int idleRes, ScreenshotTaker screenshotTaker, AtomicBoolean busyLock) {
        super(context, idleRes, screenshotTaker, busyLock);
    }

    @Override
    public void doWork(Bitmap bitmap) {
        Bitmap windowCropped = ImageUtil.extractBox(bitmap, WindowService.getWindowRect());
        Pair<Bitmap, Rect> padCropped = ImageUtil.trimToImage(windowCropped);
        Rect dungeonViewRect = ImageUtil.getMainWindowRect(padCropped.first);

        Bitmap postCropped = ImageUtil.extractBox(padCropped.first, dungeonViewRect);
        Rect finalRect = ImageUtil.collapseRects(padCropped.second, dungeonViewRect);

        System.out.println("kicking off cloud vision scan");

        CloudVisionResult visionResult = new CloudVisionScanAsyncTask().apply(new CloudVisionParams(postCropped, "ja"));
        setImageOnUiThread(R.drawable.translate);
        FullTranslateAsyncTask.TranslateResult translateResult = new FullTranslateAsyncTask(windowCropped, finalRect).apply(visionResult);

        FullTranslateDisplayService.getInstance().translateSuccess(translateResult);
        completeRequest();
        diagnostics(translateResult);
    }

    public void diagnostics(FullTranslateAsyncTask.TranslateResult result) {
        if (!Diagnostics.getEnabled()) {
            return;
        }
        Bitmap maskedResult = ImageUtil.extractBox(result.getFullImage(), result.getFinalRect());
        List<Rect> boxes = new ArrayList<>();
        for (CloudVisionResult.SortedTextLine line : result.getRawLines()) {
            boxes.add(line.getLineRect());
        }
        ImageUtil.drawOver(maskedResult, boxes.toArray(new Rect[0]));
        File file = ImageUtil.writeBmpToScreenshots(getView().getContext(), maskedResult, "cloud_annotated.png");
        Diagnostics.setScreenshotPath(file.getPath());
    }
}
