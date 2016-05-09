package com.nacho.padtranslate.screenshot;

import android.app.Activity;
import android.content.Intent;

public class MediaProjectionResults {
    private int screenshotResultCode;
    private Intent screenshotResultData;

    public MediaProjectionResults(int screenshotResultCode, Intent screenshotResultData) {
        this.screenshotResultCode = screenshotResultCode;
        this.screenshotResultData = screenshotResultData;
    }

    public boolean canTakeScreenshots() {
        return screenshotResultCode == Activity.RESULT_OK && screenshotResultData != null;
    }

    public int getScreenshotResultCode() {
        return screenshotResultCode;
    }

    public Intent getScreenshotResultData() {
        return screenshotResultData;
    }
}
