package com.nacho.padtranslate.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.google.common.io.ByteStreams;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.nacho.padtranslate.ocr.OcrResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OcrWrapper implements TessBaseAPI.ProgressNotifier {

    private static final int OCR_ENGINE_MODE = TessBaseAPI.OEM_TESSERACT_ONLY;
    private static final String SOURCE_LANGUAGE_CODE = "v1.2_jpn";
    private static final String TRAINING_DATA_FILE_NAME = SOURCE_LANGUAGE_CODE + ".traineddata";

    private OcrResult lastResult;
    private Bitmap lastBitmap;
    private boolean hasSurface;

    private TessBaseAPI baseApi;
    private boolean isEngineReady;
    private boolean isPaused;

    public OcrWrapper(Context context) {
        File filesDir = context.getFilesDir();
        File tessdataDir = new File(filesDir, "tessdata");
        tessdataDir.mkdir();
        File trainedDataFile = new File(tessdataDir, TRAINING_DATA_FILE_NAME);

        if (!trainedDataFile.exists()) {
            expandTrainedData(context, trainedDataFile);
        }

        baseApi = new TessBaseAPI();
        boolean initSuccess = baseApi.init(filesDir.getAbsolutePath() + File.separator, SOURCE_LANGUAGE_CODE, OCR_ENGINE_MODE);
        System.out.println("OCR init success: " + initSuccess);
    }

    private void expandTrainedData(Context context, File destFile) {
        try (InputStream is = context.getAssets().open(TRAINING_DATA_FILE_NAME);
        FileOutputStream fos = new FileOutputStream(destFile)) {
            ByteStreams.copy(is, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressValues(TessBaseAPI.ProgressValues progressValues) {
        System.out.println("on progress values: " + progressValues);
    }

    public TessBaseAPI getBaseApi() {
        return baseApi;
    }

}
