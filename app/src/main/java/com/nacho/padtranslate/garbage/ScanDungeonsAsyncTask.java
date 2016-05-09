package com.nacho.padtranslate.garbage;

import com.google.common.base.Preconditions;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.nacho.padtranslate.data.DungeonData;
import com.nacho.padtranslate.data.DungeonInfo;
import com.nacho.padtranslate.ocr.OcrParams;
import com.nacho.padtranslate.ocr.OcrRecognizeAsyncTask;
import com.nacho.padtranslate.ocr.OcrResult;
import com.nacho.padtranslate.ui.MainApp;
import com.nacho.padtranslate.util.CjkNormalizer;

import java.util.List;

public class ScanDungeonsAsyncTask {
//        extends OcrRecognizeAsyncTask {
//    public static interface DungeonsIdentifiedListener {
//        void dungeonsIdentified(List<DungeonInfo> dungeons);
//        void identificationFailed(OcrResult result);
//    }
//
//    public static class DungeonOcrResults {
//        private OcrResult dungeons;
//    }
//
//
//    private DungeonsIdentifiedListener[] listeners;
//
//    public ScanDungeonsAsyncTask(TessBaseAPI baseApi, DungeonsIdentifiedListener... listeners) {
//        super(baseApi);
//        this.listeners = listeners;
//        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD);
//    }
//
//    @Override
//    protected List<OcrResult> doInBackground(OcrParams... params) {
//        Preconditions.checkArgument(params.length == 1);
//        return super.doInBackground(params);
//    }
//
//    @Override
//    protected void onPostExecute(List<OcrResult> result) {
//        OcrResult page = result.get(0); // ignored for now
//
//        for (OcrResult.OcrToken token : page.getResultTokens()) {
//            String normalized = CjkNormalizer.normalize(token.getText());
//            if (normalized.contains("EAR") || normalized.contains("EOR")
//                    || normalized.contains("/2")
//                    || normalized.matches("あと.*日") // days remaining
////                    || normalized.contains("★") // challenge mode star
////                    || normalized.contains("☆") // challenge mode star
//                ) {
//                System.out.println("skipping: " + normalized);
//            } else {
//                System.out.println("scanning: " + normalized);
//                System.out.println("raw: " + token.getText() + " " + token.getBounding() + " " + token.getConfidence());
//                DungeonData dungeonData = MainApp.getMonsterDatabase().matchDungeon(token.getText());
//                if (dungeonData == null) {
//                    System.out.println("no matching dungeon");
//                } else {
//                    System.out.println("matched: " + dungeonData.getNameEn());
//                }
//            }
//
//            System.out.println("normalized: " + normalized);
//        }
//        System.out.println("done");
//
//        for (DungeonsIdentifiedListener listener : listeners) {
//            listener.identificationFailed(page);
//        }
//    }
}
