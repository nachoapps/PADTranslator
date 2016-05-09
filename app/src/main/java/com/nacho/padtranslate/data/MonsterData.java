package com.nacho.padtranslate.data;

public class MonsterData {
    private int num;
    private String nameEn;
    private String nameJp;
    private String skillNameJp;
    private String skillTextEn;
    private String leaderNameJp;
    private String leaderTextEn;

    public MonsterData(int num,
                       String nameEn, String nameJp,
                       String skillNameJp, String skillTextEn,
                       String leaderNameJp, String leaderTextEn) {
        this.num = num;
        this.nameEn = nameEn;
        this.nameJp = nameJp;
        this.skillNameJp = skillNameJp;
        this.skillTextEn = skillTextEn;
        this.leaderNameJp = leaderNameJp;
        this.leaderTextEn = leaderTextEn;
    }

    public int getNum() {
        return num;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameJp() {
        return nameJp;
    }

    public String getSkillNameJp() {
        return skillNameJp;
    }

    public String getSkillTextEn() {
        return skillTextEn;
    }

    public String getLeaderNameJp() {
        return leaderNameJp;
    }

    public String getLeaderTextEn() {
        return leaderTextEn;
    }

    @Override
    public String toString() {
        return "MonsterData{" +
                "num=" + num +
                ", nameEn='" + nameEn + '\'' +
                ", nameJp='" + nameJp + '\'' +
                ", skillNameJp='" + skillNameJp + '\'' +
                ", leaderNameJp='" + leaderNameJp + '\'' +
                ", skillTextEn='" + skillTextEn + '\'' +
                ", leaderTextEn='" + leaderTextEn + '\'' +
                '}';
    }
}
