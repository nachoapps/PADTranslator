package com.nacho.padtranslate.fulltranslate;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.nacho.padtranslate.cloudvision.CloudVisionParams;
import com.nacho.padtranslate.cloudvision.CloudVisionResult;
import com.nacho.padtranslate.data.DungeonData;
import com.nacho.padtranslate.ui.MainApp;
import com.nacho.padtranslate.util.Diagnostics;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class FullTranslateAsyncTask implements Function<CloudVisionResult, FullTranslateAsyncTask.TranslateResult> {

    public static class TranslateResult {
        private Bitmap fullImage;
        private Rect finalRect;
        private List<CloudVisionResult.SortedTextLine> rawLines;
        private List<TranslatedLine> lines;

        public Bitmap getFullImage() {
            return fullImage;
        }

        public Rect getFinalRect() {
            return finalRect;
        }

        public List<CloudVisionResult.SortedTextLine> getRawLines() {
            return rawLines;
        }

        public List<TranslatedLine> getLines() {
            return lines;
        }
    }

    public static class TranslatedLine {
        private String text;
        private Rect box;

        public TranslatedLine(String text, Rect box) {
            this.text = text;
            this.box = box;
        }

        public String getText() {
            return text;
        }

        public Rect getBox() {
            return box;
        }

        @Override
        public String toString() {
            return "TranslatedLine{" +
                    "text='" + text + '\'' +
                    ", box=" + box +
                    '}';
        }
    }


    private Bitmap fullImage;
    private Rect finalRect;

    public FullTranslateAsyncTask(Bitmap fullImage, Rect finalRect) {
        super();
        this.fullImage = fullImage;
        this.finalRect = finalRect;
    }

    @Nullable
    @Override
    public TranslateResult apply(@Nullable CloudVisionResult result) {
        TranslateResult tr = new TranslateResult();
        tr.fullImage = this.fullImage;
        tr.finalRect = this.finalRect;
        tr.rawLines = new ArrayList<>();
        tr.lines = new ArrayList<>();
        for (CloudVisionResult.TextLine line : result.getLines()) {
            for (CloudVisionResult.SortedTextLine sortedLine : CloudVisionResult.extractSortedTextLines(line)) {
                String translatedText = tryTranslate(sortedLine.getText());
                if (translatedText != null) {
                    tr.lines.add(new TranslatedLine(translatedText, sortedLine.getLineRect()));
                }
                tr.rawLines.add(sortedLine);
            }
        }

        Diagnostics.setAnnotations(result.getAnnotations());
        Diagnostics.setTextLines(tr.lines);
        Diagnostics.setRawLines(tr.rawLines);

        return tr;
    }

    private static String[] BAD_START_STRINGS = {
            "あと", // TIME REMAINING
    };

    // these can start, in which case we remove and then discard 0 length string
    // they can also suffix, which is bad because it's an accident
    private static String[] BAD_CONTAINS_STRINGS = {
        "clear", // DUNGEON CLEAR
        "+", // PLUS EGG DROP
        "スタミナ", // STAMINA
        "バトル", // FLOORS
        "ハトル", // FLOORS (no diacritic)
        "スコア", // SCORE
        "ドロップ 率", // DROP RATE
        "経験", // XP
    };

    // these can start, in which case we remove and then discard 0 length string
    // they can also suffix, which is bad because it's an accident
    private static String[] BAD_PATTERN_STRINGS = {
        "タマゴ.+倍", // PLUS EGG DROP
    };

    private String tryTranslate(String text) {
        text = text.toLowerCase().trim();
        for (String s : BAD_START_STRINGS) {
            if (text.startsWith(s)) {
                return null;
            }
        }
        for (String s : BAD_CONTAINS_STRINGS) {
            text = text.replace(s, "");
        }
        for (String s : BAD_PATTERN_STRINGS) {
            text = text.replaceAll(s, "");
        }
        text = text.trim();

        if (text.length() <= 1) {
            return null;
        }

        DungeonData dungeonData = MainApp.getMonsterDatabase().matchDungeon(text);
        return dungeonData == null ? null : dungeonData.getNameEn();
    }
}
