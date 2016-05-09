package com.nacho.padtranslate.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Pair;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.math.DoubleMath;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;

public class ImageUtil {

    public static Rect collapseRects(Rect topRect, Rect... otherRects) {
        Rect finalRect = new Rect(topRect);
        for (Rect r : otherRects) {
            int newLeft = finalRect.left + r.left;
            int newTop = finalRect.top + r.top;
            finalRect = new Rect(newLeft, newTop, newLeft + r.width(), newTop + r.height());
        }
        return finalRect;
    }

    private static class BoxParams {
        public double wStart;
        public double wEnd;
        public double hStart;
        public double hEnd;
        public BoxParams(double wStart, double wEnd, double hStart, double hEnd) {
            this.wStart = wStart; this.wEnd = wEnd; this.hStart = hStart; this.hEnd = hEnd;
        }
    }

    public static class BoxConfig {
        public BoxParams num;
        public BoxParams name;
        public BoxParams active1;
        public BoxParams active2;
        public BoxParams leader;
        public BoxConfig(BoxParams num, BoxParams name, BoxParams active1, BoxParams active2, BoxParams leader) {
            this.num = num; this.name = name; this.active1 = active1; this.active2 = active2; this.leader = leader;
        }
    }

    private static BoxConfig POINT_584 = new BoxConfig(
            new BoxParams(.155, .350, .150, .170),  // num
            new BoxParams(.155, .700, .170, .190),  // name
            new BoxParams(.160, .600, .795, .825),  // active 1
            new BoxParams(.160, .600, .850, .875),  // active 2
            new BoxParams(.310, .850, .900, .925)); // leader

    private static BoxConfig POINT_666 = new BoxConfig(
            new BoxParams(.155, .350, .170, .195),  // num
            new BoxParams(.155, .700, .195, .220),  // name
            new BoxParams(.160, .600, .765, .800),  // active 1
            new BoxParams(.160, .600, .820, .850),  // active 2
            new BoxParams(.310, .850, .885, .915)); // leader

    public static BoxConfig DEFAULT_CONFIG = POINT_584;

    public static BoxConfig getBoxConfig(Bitmap bmp) {
        return getBoxConfig((double) bmp.getWidth() / (double) bmp.getHeight());
    }
    public static BoxConfig getBoxConfig(double ratio) {
        Diagnostics.setRatio(ratio);
        if (DoubleMath.fuzzyEquals(ratio, .584, .01)) {
            return POINT_584;
        } else if (DoubleMath.fuzzyEquals(ratio, .666, .01)) {
            return POINT_666;
        } else {
            return null;
        }
    }

    public static BoxParams getMainWindowBox(Bitmap bmp) {
        double ratio = (double) bmp.getWidth() / (double) bmp.getHeight();
        Diagnostics.setRatio(ratio);
        if (DoubleMath.fuzzyEquals(ratio, .584, .01)) {
            return POINT_584_MAIN_BOX;
        } else if (DoubleMath.fuzzyEquals(ratio, .666, .01)) {
            return POINT_666_MAIN_BOX;
        } else {
            return null;
        }
    }

    private static final BoxParams POINT_584_MAIN_BOX = new BoxParams(0, 1, .21, .86);
    private static final BoxParams POINT_666_MAIN_BOX = new BoxParams(0, 1, .23, .84);
    private static final BoxParams DEFAULT_MAIN_BOX = POINT_584_MAIN_BOX;

    public static Rect getMainWindowRect(Bitmap bmp) {
        BoxParams params = MoreObjects.firstNonNull(getMainWindowBox(bmp), DEFAULT_MAIN_BOX);
        return getRect(bmp, params);
    }

    public static Pair<Bitmap, Rect> trimToImage(Bitmap bmp) {
        int yStart = Integer.MAX_VALUE;
        int xStart = Integer.MAX_VALUE;

        int midX = bmp.getWidth() / 2;
        for (int y = 0; y < bmp.getHeight(); y++) {
            int pixel = bmp.getPixel(midX, y);
            if (pixel == Color.BLACK) {
            } else {
                yStart = y;
                break;
            }
        }

        for (int x = 0; x < bmp.getWidth(); x++) {
            int pixel = bmp.getPixel(x, yStart);
            if (pixel == Color.BLACK) {
            } else {
                xStart = x;
                break;
            }
        }

        Diagnostics.setStarts(xStart, yStart);
        if (xStart == Integer.MAX_VALUE || yStart == Integer.MAX_VALUE) {
            System.out.println("couldn't identify starts: " + xStart + "," + yStart);
            return null;
        }

        Rect imageRect = new Rect(xStart, yStart, bmp.getWidth() - xStart, bmp.getHeight() - yStart);
        return Pair.create(extractBox(bmp, imageRect), imageRect);
//        return Bitmap.createBitmap(bmp, xStart, yStart, bmp.getWidth() - xStart * 2, bmp.getHeight() - yStart * 2);
    }

    public static Bitmap extractBox(Bitmap img, BoxParams params) {
        Rect box = getRect(img, params);
        return Bitmap.createBitmap(img, box.left, box.top, box.width(), box.height());
    }

    public static Bitmap extractBox(Bitmap img, Rect box) {
        Preconditions.checkNotNull(img);
        Preconditions.checkNotNull(box);
        return Bitmap.createBitmap(img, box.left, box.top, box.width(), box.height());
    }

    public static void drawOver(Bitmap img, Rect... boxes) {
        System.out.println("drawing over: " + img.getWidth() + " - " + img.getHeight());
        Canvas canvas = new Canvas(img);
        for (Rect b : boxes) {
            canvas.clipRect(b, Region.Op.DIFFERENCE);
        }
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setAlpha(200);
        canvas.drawPaint(paint);

        canvas.clipRect(new Rect(0,0, img.getWidth(), img.getHeight()), Region.Op.UNION);
        for (Rect b : boxes) {
            Paint boxPaint = new Paint();
            boxPaint.setColor(Color.BLACK);
            boxPaint.setStrokeWidth(0);
            boxPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(b, boxPaint);
        }
    }

    public static Rect getRect(Bitmap bmp, BoxParams params) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        double wStart = params.wStart, wEnd = params.wEnd, hStart = params.hStart, hEnd = params.hEnd;

        double wBoxStart = w * wStart;
        double wBoxEnd = w * wEnd;

        double hBoxStart = h * hStart;
        double hBoxEnd = h * hEnd;
        return new Rect((int)wBoxStart, (int)hBoxStart, (int)wBoxEnd, (int)hBoxEnd);
    }

    public static File writeBmpToScreenshots(Context context, Bitmap bmp, String fileName) {
        File screenshots = getScreenshotsDirectory();
        screenshots.mkdirs();
        File file = new File(screenshots, fileName);
        ImageUtil.writeBmp(context, bmp, file);
        return file;
    }

    public static File getScreenshotsDirectory() {
        File pictures = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return new File(pictures, "Screenshots");
    }

    public static File getRecentScreenshot() {
        File screenshots = getScreenshotsDirectory();
        if (!screenshots.isDirectory()) {
            throw new IllegalStateException("Screenshots directory not found: " + screenshots.getAbsolutePath());
        }
        long lastModified = Long.MIN_VALUE;
        File newest = null;
        for (File file : screenshots.listFiles()) {
            if (file.lastModified() > lastModified) {
                newest = file;
                lastModified = file.lastModified();
            }
        }
        if (newest == null) {
            throw new IllegalStateException("No screenshot found in: " + screenshots.getAbsolutePath());
        }
        return newest;
    }

    public static Bitmap readBmp(Context context, File file) throws IOException {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
    }

    public static void writeBmp(Context context, Bitmap bmp, File file) {
        System.out.println("writing bmp to: " + file);

        try (FileOutputStream output = new FileOutputStream(file);) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null, null);
    }

    public static Bitmap bmpFromImage2(Image image, int mWidth, int mHeight) {
        Image.Plane[] planes = image.getPlanes();
        Buffer buffer = planes[0].getBuffer().rewind();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * mWidth;

        // create bitmap
        Bitmap bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }
}
