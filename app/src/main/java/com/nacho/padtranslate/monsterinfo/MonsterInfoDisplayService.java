package com.nacho.padtranslate.monsterinfo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.nacho.padtranslate.data.MonsterInfo;
import com.nacho.padtranslate.ui.R;

public class MonsterInfoDisplayService extends Service {
    private static boolean ENABLED = false;

    private static MonsterInfoDisplayService instance;
    public static MonsterInfoDisplayService getInstance() {
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

    public void identificationFailed() {
        Toast.makeText(MonsterInfoDisplayService.this, "Failed Identification", Toast.LENGTH_SHORT).show();
    }

    public void monsterIdentified(final MonsterInfo info) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                disposeOldView();

                if (info == null) {
                    identificationFailed();
                    return;
                }

                infoView = makeView(info);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        windowWidth,
                        WindowManager.LayoutParams.WRAP_CONTENT,
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

    private View makeView(MonsterInfo monsterInfo) {
        LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.fragment_monster_info, null);

        ((TextView)v.findViewById(R.id.monster_name_text)).setText(monsterInfo.getName());
        ((TextView)v.findViewById(R.id.leader_skill_text)).setText(monsterInfo.getLeaderSkill());
        ((TextView)v.findViewById(R.id.active_skill_text)).setText(monsterInfo.getActiveSkill());
        ((TextView)v.findViewById(R.id.alt_skill_text)).setText(monsterInfo.getAltSkill());
        
        return v;
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
