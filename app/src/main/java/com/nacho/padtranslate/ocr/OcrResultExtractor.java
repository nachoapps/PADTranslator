package com.nacho.padtranslate.ocr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

/**
 *
 */
public class OcrResultExtractor {

    public static OcrResult build(TessBaseAPI baseApi, int iterLvl, Bitmap img) {
        OcrResult result = new OcrResult();
        result.setBitmap(img);

        try {
            long start = System.currentTimeMillis();
//            Pix pix = Binarize.otsuAdaptiveThreshold(ReadFile.readBitmap(img));
//            Pix pix = Binarize.sauvolaBinarizeTiled(ReadFile.readBitmap(img));
//            baseApi.setImage(pix);
            baseApi.setImage(img);
            String textResult = baseApi.getUTF8Text();
            long timeRequired = System.currentTimeMillis() - start;

            result.setText(textResult);
            result.setRecognitionTimeRequired(timeRequired);
            result.setMeanConfidence(baseApi.meanConfidence());

            final ResultIterator iter = baseApi.getResultIterator();
            iter.begin();

            do {
                String utf8Text = iter.getUTF8Text(iterLvl);
                System.out.println(utf8Text);
                result.getResultTokens().add(new OcrResult.OcrToken(
                    iter.getBoundingRect(iterLvl),
                        utf8Text,
                    iter.confidence(iterLvl)
                ));
            } while (iter.next(iterLvl));
            iter.delete();
        } catch (Exception e) {
            result.setException(e);
        }

        baseApi.clear();
        return result;
    }


    public static Bitmap getAnnotatedBitmap(Bitmap bitmap, OcrResult result) {

        Bitmap copy = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(copy);
        Paint paint = new Paint();

        for (OcrResult.OcrToken token : result.getResultTokens()) {
              paint.setAlpha(0xFF);
              paint.setColor(0xFF00CCFF);
              paint.setStyle(Paint.Style.STROKE);
              paint.setStrokeWidth(2);
              paint.setTextSize(9.0f);
              Rect r = token.getBounding();
              canvas.drawRect(r, paint);

              paint.setColor(Color.BLACK);
              paint.setTextSize(30);
//              canvas.drawText(Integer.toString(i), (float)r.left, (float)r.bottom, paint);
        }
        return copy;
  }

}
