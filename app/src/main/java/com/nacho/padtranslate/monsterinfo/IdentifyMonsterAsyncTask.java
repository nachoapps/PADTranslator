package com.nacho.padtranslate.monsterinfo;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.nacho.padtranslate.data.MonsterData;
import com.nacho.padtranslate.data.MonsterInfo;
import com.nacho.padtranslate.ocr.OcrParams;
import com.nacho.padtranslate.ocr.OcrResult;
import com.nacho.padtranslate.ui.MainApp;
import com.nacho.padtranslate.util.Diagnostics;

import java.util.List;

import javax.annotation.Nullable;

public class IdentifyMonsterAsyncTask implements Function<List<OcrResult>, MonsterInfo> {

    @Nullable
    @Override
    public MonsterInfo apply(@Nullable List<OcrResult> result) {
        Preconditions.checkArgument(result.size() == 5);

        OcrResult number = result.get(0); // ignored for now
        OcrResult name = result.get(1);
        OcrResult activeSkill = result.get(2);
        OcrResult altSkill = result.get(3);
        OcrResult leaderSkill = result.get(4);

        String nameJp = name.getText();
        String skillNameJp = activeSkill.getText();
        String altSkillNameJp = altSkill.getText();
        String leaderNameJp = leaderSkill.getText();

        Diagnostics.setOcrResultText(nameJp, skillNameJp, altSkillNameJp, leaderNameJp);

        MonsterData monsterData = MainApp.getMonsterDatabase().matchByBestName(nameJp, skillNameJp, leaderNameJp);
        MonsterData altData = MainApp.getMonsterDatabase().matchAlt(altSkillNameJp);
        if (monsterData != null) {
            String altText = altData != null ? altData.getSkillTextEn() : "";
            Diagnostics.setMonsterIdMatch(monsterData.getNameEn(), altText);
            return new MonsterInfo(
                    monsterData.getNameEn(),
                    monsterData.getLeaderTextEn(),
                    monsterData.getSkillTextEn(),
                    altText
            );
        }
        return null;
    }
}
