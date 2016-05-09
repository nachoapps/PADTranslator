/*
 * Copyright 2011 Robert Theis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nacho.padtranslate.ocr;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.googlecode.leptonica.android.Pixa;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the result of OCR.
 */
public class OcrResult {

  public static class OcrToken {
    private Rect bounding;
    private String text;
    private float confidence;

    public OcrToken(Rect bounding, String text, float confidence) {
      this.bounding = bounding;
      this.text = text;
      this.confidence = confidence;
    }

    public Rect getBounding() {
      return bounding;
    }

    public String getText() {
      return text;
    }

    public float getConfidence() {
      return confidence;
    }
  }

  private Exception exception;

  private Bitmap bitmap;
  private String text;
  
  private int meanConfidence;
  private List<OcrToken> resultTokens = new ArrayList<>();

  private long timestamp;
  private long recognitionTimeRequired;

  public OcrResult() {
    this.timestamp = System.currentTimeMillis();
  }

  public boolean isFailure() {
    return exception != null;
  }

  @Override
  public String toString() {
    return text + " " + meanConfidence + " " + recognitionTimeRequired + " " + timestamp;
  }

  public List<OcrToken> getResultTokens() {
    return resultTokens;
  }

  public Exception getException() {
    return exception;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public Bitmap getBitmap() {
    return bitmap;
  }

  public void setBitmap(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int getMeanConfidence() {
    return meanConfidence;
  }

  public void setMeanConfidence(int meanConfidence) {
    this.meanConfidence = meanConfidence;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getRecognitionTimeRequired() {
    return recognitionTimeRequired;
  }

  public void setRecognitionTimeRequired(long recognitionTimeRequired) {
    this.recognitionTimeRequired = recognitionTimeRequired;
  }
}
