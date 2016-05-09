package com.nacho.padtranslate.ocr;

import android.graphics.Bitmap;

/**
 *
 */
public class OcrParams {
    private Bitmap data;
    private int iterLevel;

    public OcrParams(Bitmap data, int iterLevel) {
        this.data = data;
        this.iterLevel = iterLevel;
    }

    public Bitmap getData() {
        return data;
    }

    public int getIterLevel() {
        return iterLevel;
    }
}
