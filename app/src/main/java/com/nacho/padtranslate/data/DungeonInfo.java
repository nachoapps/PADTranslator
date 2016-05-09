package com.nacho.padtranslate.data;

import android.graphics.Rect;

public class DungeonInfo {

    private String nameEn;
    private String nameJp;
    private Rect location;

    public DungeonInfo(String nameEn, String nameJp, Rect location) {
        this.nameEn = nameEn;
        this.nameJp = nameJp;
        this.location = location;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameJp() {
        return nameJp;
    }

    public Rect getLocation() {
        return location;
    }
}
