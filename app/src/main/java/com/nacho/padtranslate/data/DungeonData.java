package com.nacho.padtranslate.data;

/**
 *
 */
public class DungeonData {
    private String nameEn;
    private String nameJp;

    public DungeonData(String nameEn, String nameJp) {
        this.nameEn = nameEn;
        this.nameJp = nameJp;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameJp() {
        return nameJp;
    }

    @Override
    public String toString() {
        return "DungeonData{" +
                "nameEn='" + nameEn + '\'' +
                ", nameJp='" + nameJp + '\'' +
                '}';
    }
}
