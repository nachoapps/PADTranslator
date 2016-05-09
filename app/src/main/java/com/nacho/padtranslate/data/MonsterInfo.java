package com.nacho.padtranslate.data;

public class MonsterInfo {

    private String name;
    private String leaderSkill;
    private String activeSkill;
    private String altSkill;

    public MonsterInfo(String name, String leaderSkill, String activeSkill, String altSkill) {
        this.name = name;
        this.leaderSkill = leaderSkill;
        this.activeSkill = activeSkill;
        this.altSkill = altSkill;
    }

    public String getName() {
        return name;
    }

    public String getLeaderSkill() {
        return leaderSkill;
    }

    public String getActiveSkill() {
        return activeSkill;
    }

    public String getAltSkill() {
        return altSkill;
    }
}
