package com.nacho.padtranslate.cloudvision;

import android.graphics.Bitmap;

public class CloudVisionParams {
    private Bitmap data;
    private String targetLang;

    public CloudVisionParams(Bitmap data, String targetLang) {
        this.data = data;
        this.targetLang = targetLang;
    }

    public Bitmap getData() {
        return data;
    }

    public String getTargetLang() {
        return targetLang;
    }
}
