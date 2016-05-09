package com.nacho.padtranslate.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.widget.Toast;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.nacho.padtranslate.data.MonsterInfo;
import com.nacho.padtranslate.monsterinfo.IdentifyMonsterAsyncTask;
import com.nacho.padtranslate.monsterinfo.MonsterInfoDisplayService;
import com.nacho.padtranslate.ocr.OcrParams;
import com.nacho.padtranslate.ocr.OcrRecognizeAsyncTask;
import com.nacho.padtranslate.ocr.OcrResult;
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
public class MonsterInfoButton extends WindowServiceButton {

    public MonsterInfoButton(Context context, @DrawableRes int idleRes, ScreenshotTaker screenshotTaker, AtomicBoolean busyLock) {
        super(context, idleRes, screenshotTaker, busyLock);
    }

    @Override
    public void doWork(Bitmap bitmap) {
        Preconditions.checkNotNull(bitmap);
        Bitmap windowCropped = ImageUtil.extractBox(bitmap, WindowService.getWindowRect());
        Bitmap padCropped = ImageUtil.trimToImage(windowCropped).first;

        ImageUtil.BoxConfig config = ImageUtil.getBoxConfig(padCropped);
        Diagnostics.setConfigFound(config != null);
        if (config == null) {
            System.out.println("failed to identify screen config, using default");
            config = ImageUtil.DEFAULT_CONFIG;
        }

        int iterLvl = TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE;
        List<OcrResult> params = new ArrayList<>();
        OcrRecognizeAsyncTask ocr = new OcrRecognizeAsyncTask(MainApp.getOcrWrapper().getBaseApi());
        params.add(ocr.apply(new OcrParams(ImageUtil.extractBox(padCropped, config.num), iterLvl)));
        params.add(ocr.apply(new OcrParams(ImageUtil.extractBox(padCropped, config.name), iterLvl)));
        params.add(ocr.apply(new OcrParams(ImageUtil.extractBox(padCropped, config.active1), iterLvl)));
        params.add(ocr.apply(new OcrParams(ImageUtil.extractBox(padCropped, config.active2), iterLvl)));
        params.add(ocr.apply(new OcrParams(ImageUtil.extractBox(padCropped, config.leader), iterLvl)));

        setImageOnUiThread(R.drawable.translate);

        MonsterInfo info = new IdentifyMonsterAsyncTask().apply(params);
        MonsterInfoDisplayService.getInstance().monsterIdentified(info);
        diagnostics(padCropped, config);
    }

    private void diagnostics(Bitmap bmp, ImageUtil.BoxConfig config) {
        if (!Diagnostics.getEnabled()) {
            return;
        }

        Bitmap maskedBmp = bmp.copy(bmp.getConfig(), true);

        ImageUtil.drawOver(maskedBmp,
                ImageUtil.getRect(maskedBmp, config.num),
                ImageUtil.getRect(maskedBmp, config.name),
                ImageUtil.getRect(maskedBmp, config.active1),
                ImageUtil.getRect(maskedBmp, config.active2),
                ImageUtil.getRect(maskedBmp, config.leader)
        );

        String fileSuffix = System.currentTimeMillis() + ".png";
        File file = ImageUtil.writeBmpToScreenshots(getView().getContext(), maskedBmp, "masked_" + fileSuffix);
        Diagnostics.setScreenshotPath(file.getAbsolutePath());
    }
}
