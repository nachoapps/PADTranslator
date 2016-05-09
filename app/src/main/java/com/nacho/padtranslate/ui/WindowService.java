package com.nacho.padtranslate.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.nacho.padtranslate.screenshot.DynamicScreenshotTaker;
import com.nacho.padtranslate.screenshot.RecentScreenshotTaker;
import com.nacho.padtranslate.screenshot.ScreenshotTaker;
import com.nacho.padtranslate.util.Diagnostics;

import java.util.concurrent.atomic.AtomicBoolean;

public class WindowService extends Service {

    private static WindowService instance;
    public static WindowService getInstance() {
        return instance;
    }

    private static Rect windowRect;

    public static void setWindowRect(Rect windowRect) {
        if (!windowRect.equals(WindowService.windowRect)) {
            System.out.println("Replacing window (" + WindowService.windowRect + ") with (" + windowRect + ")");
            Diagnostics.setWindowRect(windowRect);
            WindowService.windowRect = windowRect;
        }
    }

    public static Rect getWindowRect() {
        return windowRect;
    }

    private WindowManager windowManager;
    private ScreenshotTaker screenshotTaker;

    public WindowService() {
    }

    private LinearLayout layout;

    private WindowServiceButton monsterButton;
    private WindowServiceButton dungeonButton;
    private WindowServiceButton menuButton;
    private WindowServiceButton screenshotButton;

    private AtomicBoolean busyLock;

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();

        if (!MainApp.canTakeScreenshotsIfNecessary()) {
            System.out.println("tried to start, but didn't have required permissions");
            stopSelf();
            return;
        }

        if (getWindowRect() == null) {
            System.out.println("Shutting down, no windowrect available");
            stopSelf();
            return;
        }

        instance = this;
        busyLock = new AtomicBoolean(false);

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        if (Preferences.useLegacyScreenshots()) {
            screenshotTaker = new RecentScreenshotTaker(getBaseContext());
        } else {
            screenshotTaker = new DynamicScreenshotTaker(
                    windowManager,
                    (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE),
                    MainApp.getMediaProjectionResults());
        }

        monsterButton = new MonsterInfoButton(
                getBaseContext(),
                R.drawable.ic_pets_black_24dp,
                screenshotTaker,
                busyLock
        );
        dungeonButton = new FullTranslateButton(
                getBaseContext(),
                R.drawable.ic_flag_24dp,
                screenshotTaker,
                busyLock
        );
        screenshotButton = new ScreenshotButton(
                getBaseContext(),
                R.drawable.ic_get_app_black_24dp,
                screenshotTaker,
                busyLock
        );

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 30, 0, 0);

        if (Preferences.showMonsterInfoButton()) {
            layout.addView(monsterButton.getView());
        }

        if (Preferences.showTranslateDungeonButton()) {
            layout.addView(dungeonButton.getView(), layoutParams);
        }

        if (!Preferences.useLegacyScreenshots() && Preferences.showTakeScreenshotButton()) {
            layout.addView(screenshotButton.getView(), layoutParams);
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | (Preferences.buttonsOnAltSide() ? Gravity.RIGHT : Gravity.LEFT);
        params.verticalMargin = .1f;

        windowManager.addView(layout, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (layout != null) windowManager.removeView(layout);
        instance = null;
        if (screenshotTaker != null) {
            screenshotTaker.shutdown();
            screenshotTaker = null;
        }
    }
}
