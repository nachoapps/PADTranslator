package com.nacho.padtranslate.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.widget.Toast;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.nacho.padtranslate.cloudvision.CloudVisionResult;
import com.nacho.padtranslate.fulltranslate.FullTranslateAsyncTask;
import com.nacho.padtranslate.ui.Preferences;
import com.nacho.padtranslate.ui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Diagnostics {

    private static String nameJp;
    private static String skillNameJp;
    private static String altSkillNameJp;
    private static String leaderNameJp;

    private static String identifiedName;
    private static String identifiedAltSkill;

    private static String screenshotPath;
    private static boolean configFound;
    private static Rect windowRect;
    private static int xStart;
    private static int yStart;
    private static double ratio;

    private static List<EntityAnnotation> annotations;
    private static List<FullTranslateAsyncTask.TranslatedLine> textLines;
    private static List<CloudVisionResult.SortedTextLine> rawLines;

    private static Exception lastException;

    public static void setOcrResultText(String nameJp, String skillNameJp, String altSkillNameJp, String leaderNameJp) {
        Diagnostics.nameJp = nameJp;
        Diagnostics.skillNameJp = skillNameJp;
        Diagnostics.altSkillNameJp = altSkillNameJp;
        Diagnostics.leaderNameJp = leaderNameJp;
    }

    public static void setMonsterIdMatch(String identifiedName, String identifiedAltSkill) {
        Diagnostics.identifiedName = identifiedName;
        Diagnostics.identifiedAltSkill = identifiedAltSkill;
    }

    public static void setScreenshotPath(String screenshotPath) {
        Diagnostics.screenshotPath = screenshotPath;
    }

    public static void setConfigFound(boolean configFound) {
        Diagnostics.configFound = configFound;
    }

    public static void setWindowRect(Rect windowRect) {
        Diagnostics.windowRect = windowRect;
    }

    public static void setStarts(int xStart, int yStart) {
        Diagnostics.xStart = xStart;
        Diagnostics.yStart = yStart;
    }

    public static void setRatio(double ratio) {
        Diagnostics.ratio = ratio;
    }

    public static void setAnnotations(List<EntityAnnotation> annotations) {
        Diagnostics.annotations = annotations;
    }

    public static void setTextLines(List<FullTranslateAsyncTask.TranslatedLine> textLines) {
        Diagnostics.textLines = textLines;
    }

    public static void setRawLines(List<CloudVisionResult.SortedTextLine> rawLines) {
        Diagnostics.rawLines = rawLines;
    }

    public static void setLastException(Exception lastException) {
        Diagnostics.lastException = lastException;
    }

    public static boolean getEnabled() {
        return Preferences.sendDiagnostics();
    }

    public static void sendDiagnostics(Context context) {
        if (!Diagnostics.getEnabled()) {
            return;
        }

        List<String> files = new ArrayList<>();
        if (screenshotPath != null) {
            files.add(screenshotPath);
        }

        String diagnosticText = "DIAGNOSTICS\n"
                + "\nDevice: " + Build.MANUFACTURER + " -> " + Build.MODEL
                + "\nScreenshot Path: " + screenshotPath
                + "\n\nDisplay Info"
                + "\n\tWindow: " + windowRect
                + "\n\tPAD offset: " + xStart + "," + yStart
                + "\n\tFinal ratio: " + ratio
                + "\n\tConfig Found: " + configFound
                + "\n\nOCR Results"
                + "\n\tName : " + nameJp
                + "\n\tSkill_1 : " + skillNameJp
                + "\n\tSkill_2 : " + altSkillNameJp
                + "\n\tLeader : " + leaderNameJp
                + "\n\nIdentified Monster"
                + "\n\tMonster Name : " + identifiedName
                + "\n\tAlt Skill Name : " + identifiedAltSkill
                + "\n\nFullScreen Translate"
                + "\n\tAnnotations : " + annotations
                + "\n\tTextLines : " + textLines
                + "\n\tRawLines : " + rawLines
                + "\n\nLast Exception"
                + "\n" + (lastException == null ? "none" : Throwables.getStackTraceAsString(lastException));


        Email.email(context, context.getString(R.string.app_email), "", "PAD Tx Diagnostics", diagnosticText, files);

    }
}
