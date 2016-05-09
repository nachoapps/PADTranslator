package com.nacho.padtranslate.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;

/**
 *
 */
public final class Preferences {
    private Preferences() {}

    public static boolean showMonsterInfoButton() {
        return getBooleanPref(R.string.pref_translate_monster_key);
    }

    public static boolean showTranslateDungeonButton() {
        return getBooleanPref(R.string.pref_translate_dungeon_key);
    }

    public static boolean showTranslateMenuButton() {
        return getBooleanPref(R.string.pref_translate_menu_key);
    }

    public static boolean showTakeScreenshotButton() {
        return getBooleanPref(R.string.pref_take_screenshot_key);
    }

    public static boolean sendDiagnostics() {
        return getBooleanPref(R.string.pref_send_diagnostics_key);
    }

    public static boolean buttonsOnAltSide() {
        return getBooleanPref(R.string.pref_buttons_alt_key);
    }

    public static boolean useLegacyScreenshots() {
        return getBooleanPref(R.string.pref_legacy_screenshot_key);
    }

    private static boolean getBooleanPref(@StringRes int resId) {
        Context ctx = MainApp.getAppContext();
        String key = ctx.getString(resId);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(key, false);
    }
}
