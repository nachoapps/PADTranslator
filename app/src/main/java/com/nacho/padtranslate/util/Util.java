package com.nacho.padtranslate.util;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.nacho.padtranslate.ui.MainApp;

/**
 *
 */
public class Util {

    public static void toastOnUiThread(final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainApp.getAppContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
