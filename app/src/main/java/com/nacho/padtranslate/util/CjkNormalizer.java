package com.nacho.padtranslate.util;

import com.google.common.base.CharMatcher;
import com.ibm.icu.text.Transliterator;

/**
 */
public class CjkNormalizer {

    private static Transliterator tl = Transliterator.getInstance("NFC; Fullwidth-Halfwidth; Upper");

    public static String normalize(String text) {
        text = text.replace('・', ' '); // JP Dot character
        text = text.replace('｡', ' '); // other JP Dot
        text = text.replace('【', '['); // jp brackets
        text = text.replace('】', ']'); // jp brackets
        text = tl.transliterate(text);
        text = text.replace('・', ' '); // JP Dot character
        text = text.replace('｡', ' '); // other JP Dot
        text = text.replace('【', '['); // jp brackets
        text = text.replace('】', ']'); // jp brackets

        text = text.replace('\'', ' ');
        text = text.replace('.', ' ');
        text = text.replace(',', ' ');
        text = text.replace('…', ' ');
        text = text.replace('"', ' ');
        text = text.replace('!', ' ');
        text = text.replace('(', '[');
        text = text.replace(')', ']');

        text = text.replace('ー', '-'); // tesseract can never recognize this - hiragana one
        text = text.replace('一', '-'); // tesseract can never recognize this - katakana one
        text = text.replace('ｰ', '-'); // halfwith form
        // OTHER small forms
        text = text.replace('ｧ', 'ｱ');
        text = text.replace('ｨ', 'ｲ');
        text = text.replace('ｩ', 'ｳ');
        text = text.replace('ｪ', 'ｴ');
        text = text.replace('ｫ', 'ｵ');
        text = text.replace('ｬ', 'ﾔ');
        text = text.replace('ｭ', 'ﾕ');
        text = text.replace('ｮ', 'ﾖ');
        text = text.replace('ｯ', 'ﾂ');

        // specific kanji
        text = text.replace('卜', 'ﾄ');

        // numbers that look like kana
        text = text.replace('7', 'ﾌ');

        text = CharMatcher.WHITESPACE.removeFrom(text);
        text = CharMatcher.JAVA_ISO_CONTROL.removeFrom(text);
        return text;
    }
}
