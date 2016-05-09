/***
 Copyright (c) 2015 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 From _The Busy Coder's Guide to Android Development_
 https://commonsware.com/Android
 */

package com.nacho.padtranslate.screenshot;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.util.DisplayMetrics;
import android.view.Surface;

import com.nacho.padtranslate.util.ImageUtil;

public class ImageTransmogrifier implements ImageReader.OnImageAvailableListener {
    public static interface OnBitmapAvailableListener {
        boolean onBitmapAvailable(Bitmap bitmap);
        void onBitmapFailure(Exception e);
    }

    private static final int MAX_FAILURES = 2;

    private DisplayMetrics metrics;
    private OnBitmapAvailableListener listener;

    private final ImageReader imageReader;
    private int failures;


    ImageTransmogrifier(DisplayMetrics metrics, OnBitmapAvailableListener listener) {
        this.metrics = metrics;
        this.listener = listener;

        imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels,
                PixelFormat.RGBA_8888, 1);
        imageReader.setOnImageAvailableListener(this, null);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        System.out.println("image available");
        try (Image image = imageReader.acquireLatestImage()) {
            if (!acceptImage(image)) {
                failures++;
                return;
            }

            Bitmap bitmap = ImageUtil.bmpFromImage2(image, metrics.widthPixels, metrics.heightPixels);

            // crop bitmap
            Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0,
                    metrics.widthPixels, metrics.heightPixels);
            bitmap.recycle();

            if (!acceptBitmap(cropped)) {
                cropped.recycle();
                return;
            }

            if (listener.onBitmapAvailable(cropped)) {
                image.close();
                close();
            }
        } catch (Exception e) {
            failures++;
            if (failures > MAX_FAILURES) {
                close();
                listener.onBitmapFailure(e);
            }
        }
    }

    private boolean acceptBitmap(Bitmap bitmap) {
        return bitmap != null;
    }

    private boolean acceptImage(Image image) {
        return image != null;
    }

    Surface getSurface() {
        return(imageReader.getSurface());
    }

    void close() {
        imageReader.close();
    }
}