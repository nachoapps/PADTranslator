/*
 * Copyright (C) 2008 ZXing authors
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

import android.view.Menu;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the text correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 * 
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
public final class CaptureSettings {
//        extends Activity implements SurfaceHolder.Callback,
//  ShutterButton.OnShutterButtonListener {

  private static final String TAG = CaptureSettings.class.getSimpleName();
  
  // Note: These constants will be overridden by any default values defined in preferences.xml.
  
  /** ISO 639-3 language code indicating the default recognition language. */
  public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "eng";
  
  /** ISO 639-1 language code indicating the default target language for translation. */
  public static final String DEFAULT_TARGET_LANGUAGE_CODE = "es";
  
  /** The default online machine translation service to use. */
  public static final String DEFAULT_TRANSLATOR = "Google Translate";
  
  /** The default OCR engine to use. */
  public static final String DEFAULT_OCR_ENGINE_MODE = "Tesseract";
  
  /** The default page segmentation mode to use. */
  public static final String DEFAULT_PAGE_SEGMENTATION_MODE = "Auto";
  
  /** Whether to use autofocus by default. */
  public static final boolean DEFAULT_TOGGLE_AUTO_FOCUS = true;
  
  /** Whether to initially disable continuous-picture and continuous-video focus modes. */
  public static final boolean DEFAULT_DISABLE_CONTINUOUS_FOCUS = true;
  
  /** Whether to beep by default when the shutter button is pressed. */
  public static final boolean DEFAULT_TOGGLE_BEEP = false;
  
  /** Whether to initially show a looping, real-time OCR display. */
  public static final boolean DEFAULT_TOGGLE_CONTINUOUS = false;
  
  /** Whether to initially reverse the image returned by the camera. */
  public static final boolean DEFAULT_TOGGLE_REVERSED_IMAGE = false;
  
  /** Whether to enable the use of online translation services be default. */
  public static final boolean DEFAULT_TOGGLE_TRANSLATION = true;
  
  /** Whether the light should be initially activated by default. */
  public static final boolean DEFAULT_TOGGLE_LIGHT = false;

  
}
