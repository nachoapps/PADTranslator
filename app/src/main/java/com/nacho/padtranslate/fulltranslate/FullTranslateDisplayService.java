package com.nacho.padtranslate.fulltranslate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.nacho.padtranslate.util.ImageUtil;

public class FullTranslateDisplayService extends Service {
    private static boolean ENABLED = false;

    private static FullTranslateDisplayService instance;
    public static FullTranslateDisplayService getInstance() {
        return instance;
    }

    private WindowManager windowManager;
    private int windowWidth;

    private View infoView;

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
        ENABLED = true;

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Point windowSize = new Point();
        windowManager.getDefaultDisplay().getRealSize(windowSize);
        windowWidth = windowSize.x;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposeOldView();
        ENABLED = false;
        instance = null;
    }

    public static boolean isWindowServiceEnabled() {
        return ENABLED;
    }

    public void translateFailure() {
        Toast.makeText(FullTranslateDisplayService.this, "Failed screen translate", Toast.LENGTH_SHORT).show();
    }

    public void translateSuccess(final FullTranslateAsyncTask.TranslateResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                disposeOldView();

                if (result == null || result.getLines().isEmpty()) {
                    translateFailure();
                    return;
                }

                infoView = makeView(result);

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.CENTER;

                infoView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                windowManager.removeViewImmediate(v);
                                infoView = null;
                                return true;
                        }
                        return false;
                    }
                });
                windowManager.addView(infoView, params);

            }
        });

    }

    private View makeView(FullTranslateAsyncTask.TranslateResult result) {
        Bitmap bmp = result.getFullImage().copy(result.getFullImage().getConfig(), true);

        Rect finalRect = result.getFinalRect();
        drawNotice(bmp, finalRect);

        for (FullTranslateAsyncTask.TranslatedLine line : result.getLines()) {
            drawBox(bmp, finalRect, line.getBox(), line.getText());
        }

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bmp);
        return imageView;
    }

    private void drawNotice(Bitmap bmp, Rect finalRect) {
        drawBox(bmp, new Rect(), new Rect(0, 0, bmp.getWidth(), finalRect.top/2), "TAP TO DISMISS");

    }

    private void drawBox(Bitmap bmp, Rect finalRect, Rect lineRect, String text) {
        float textMargin = lineRect.height() * .1f;
        float textHeight = lineRect.height() - textMargin * 2;
        int leftMargin = 5;
        Rect workingRect = ImageUtil.collapseRects(finalRect, lineRect);

        Paint p = new Paint();
        p.setTextAlign(Paint.Align.LEFT);
        p.setTextSize(textHeight);

        Rect bounds = new Rect();
        p.getTextBounds(text, 0, text.length(), bounds);
        if (bounds.width() > workingRect.width()) {
            // try and scale the box size up to 1.5 times
            int newWidth = Math.min((int)(workingRect.width() * 1.5), bounds.width());
            // make sure the new box size fits in window
            newWidth = Math.min(workingRect.left + newWidth, finalRect.width());
            workingRect.right = workingRect.left + newWidth;

            // check if it fits now
            if (bounds.width() > workingRect.width()) {
                // scale it down a bit and call it quits
                p.setTextSize(textHeight * .8f);
                p.getTextBounds(text, 0, text.length(), bounds);
            }
        }

        Canvas c = new Canvas(bmp);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        c.drawRect(workingRect, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.BLACK);
        p.setStrokeWidth(2.0f);
        c.drawRect(workingRect, p);

        p.setStyle(Paint.Style.FILL_AND_STROKE);
        c.drawText(text, workingRect.left + leftMargin, workingRect.bottom - textMargin, p);
    }

    private void disposeOldView() {
        try {
            if (infoView != null) {
                windowManager.removeView(infoView);
                infoView = null;
            }
        } catch (Exception e) {
            System.out.println("failed to dispose infoView");
            e.printStackTrace();
        }
    }
}
