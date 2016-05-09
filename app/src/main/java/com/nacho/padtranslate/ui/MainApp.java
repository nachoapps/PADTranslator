package com.nacho.padtranslate.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;

import com.nacho.padtranslate.data.TranslationDatabase;
import com.nacho.padtranslate.screenshot.MediaProjectionResults;

public class MainApp extends Application {

    private static MainApp instance;

    private Context context;

    private MediaProjectionResults mediaProjectionResults;

    private OcrWrapper ocrWrapper;

    private TranslationDatabase monsterDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        instance = this;
        System.out.println("initializing OCRWrapper");
        ocrWrapper = new OcrWrapper(context);
        try {
            System.out.println("initializing TranslationDatabase");
            monsterDatabase = new TranslationDatabase(context);
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Context getAppContext() {
        return instance.context;
    }


    public static boolean canTakeScreenshotsIfNecessary() {
        return Preferences.useLegacyScreenshots()
            ||  (instance.mediaProjectionResults != null
                && instance.mediaProjectionResults.canTakeScreenshots());
    }

    public static void setScreenshotIntentResult(int resultCode, Intent resultData) {
        instance.mediaProjectionResults = new MediaProjectionResults(resultCode, resultData);
    }

    public static MediaProjectionResults getMediaProjectionResults() {
        return instance.mediaProjectionResults;
    }

    public static OcrWrapper getOcrWrapper() {
        return instance.ocrWrapper;
    }

    public static TranslationDatabase getMonsterDatabase() { return instance.monsterDatabase; }
}
