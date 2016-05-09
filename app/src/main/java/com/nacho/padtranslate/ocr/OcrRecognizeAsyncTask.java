package com.nacho.padtranslate.ocr;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.common.base.Function;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 */
public class OcrRecognizeAsyncTask implements Function<OcrParams, OcrResult>{

    private TessBaseAPI baseApi;

    public OcrRecognizeAsyncTask(TessBaseAPI baseApi) {
        this.baseApi = baseApi;
    }

    @Nullable
    @Override
    public OcrResult apply(@Nullable OcrParams input) {
        return OcrResultExtractor.build(baseApi, input.getIterLevel(), input.getData());
    }
}
