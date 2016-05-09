package com.nacho.padtranslate.data;

import android.content.Context;
import android.util.Pair;

import com.google.common.base.Function;
import com.nacho.padtranslate.util.CjkNormalizer;
import com.nacho.padtranslate.util.Distance;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 */
public class TranslationDatabase {

    private static String MONSTER_DATABASE_FILE_NAME = "monsters.csv";
    private static String DUNGEON_DATABASE_FILE_NAME = "dungeons.csv";

    private List<MonsterData> monsters;
    private List<DungeonData> dungeons;

    public TranslationDatabase(Context context) throws IOException {
        readMonsters(context);
        readDungeons(context);
    }

    private void readMonsters(Context context) throws IOException {
        InputStream monsterInput = context.getAssets().open(MONSTER_DATABASE_FILE_NAME);
        monsters = new ArrayList<>();

        try {
            CSVReader reader = new CSVReader(new InputStreamReader(monsterInput));

            Iterator<String[]> iter = reader.iterator();
            iter.next();
            while (iter.hasNext()) {
                String[] line = iter.next();
                MonsterData data = new MonsterData(
                        Integer.parseInt(line[0]),
                        line[1],
                        line[9],
                        line[10],
                        line[5],
                        line[11],
                        line[8]
                );
                monsters.add(data);
            }
            reader.close();
        } finally {
            monsterInput.close();
        }

        // A really terrible performance optimization, assume that users are more interested in
        // higher numbered monsters.
        Collections.reverse(monsters);
    }
    private void readDungeons(Context context) throws IOException {
        InputStream dungeonInput = context.getAssets().open(DUNGEON_DATABASE_FILE_NAME);
        dungeons = new ArrayList<>();

        try {
            CSVReader reader = new CSVReader(new InputStreamReader(dungeonInput));

            Iterator<String[]> iter = reader.iterator();
            iter.next();
            while (iter.hasNext()) {
                String[] line = iter.next();
                DungeonData data = new DungeonData(
                        line[0],
                        capJpDungeonNameLength(line[2].trim())
                );

                String jpName = data.getNameJp();
                if (jpName.isEmpty() || jpName.equals("N/A") || jpName.matches("[-]+")) {
                    // skip garbage names
                    continue;
                }
                dungeons.add(data);
            }
            reader.close();
        } finally {
            dungeonInput.close();
        }
    }

    public MonsterData matchByBestName(String nameJp, String skillNameJp, String leaderNameJp) {
        if (nameJp.trim().isEmpty()) {
            return null;
        }
        Pair<Integer, MonsterData> nameMatch = match(nameJp, EXTRACT_NAME_JP);
        Pair<Integer, MonsterData> skillNameMatch = match(skillNameJp, EXTRACT_SKILL_JP);
        Pair<Integer, MonsterData> leaderNameMatch = match(leaderNameJp, EXTRACT_LEADER_JP);

        System.out.println("matching name          : " + CjkNormalizer.normalize(nameJp));
        System.out.println("monster name match - " + nameMatch.first + " : " +  nameMatch.second.getNameJp() +  "      -> " + nameMatch.second.getNameEn());

        System.out.println("matching skill         : " + CjkNormalizer.normalize(skillNameJp));
        System.out.println("skill name match   - " + skillNameMatch.first + " : " +  skillNameMatch.second.getSkillNameJp() +  "        -> " + skillNameMatch.second.getNameEn());

        System.out.println("matching leader        : " + CjkNormalizer.normalize(leaderNameJp));
        System.out.println("leader name match  - " + leaderNameMatch.first + " : " +  leaderNameMatch.second.getLeaderNameJp() +  "        -> " + leaderNameMatch.second.getNameEn());

        System.out.println(nameMatch.second.getNameJp());
        System.out.println(nameMatch.second.getSkillNameJp());
        System.out.println(nameMatch.second.getLeaderNameJp());

        if (nameMatch.first >= 5) {
            // really bad match, just bail
            return null;
        }

        return nameMatch.second;
    }

    public MonsterData matchAlt(String altSkill) {
        Pair<Integer, MonsterData> skillNameMatch = match(altSkill, EXTRACT_SKILL_JP);
        if (skillNameMatch.first < 3) {
            System.out.println("alt match " + skillNameMatch.first + " : " + skillNameMatch.second.getNameEn());
            return skillNameMatch.second;
        } else {
            System.out.println("failed alt match " + skillNameMatch.first);
            return null;
        }
    }

    public Pair<Integer, MonsterData> match(String text, Function<MonsterData, String> f) {
        text = CjkNormalizer.normalize(text);
        int bestDist = Integer.MAX_VALUE;
        MonsterData bestMatch = null;
        for (MonsterData data : monsters) {
            int nameDist = Distance.computeLevenshteinDistance(text, f.apply(data));
            if (nameDist < bestDist) {
                bestDist = nameDist;
                bestMatch = data;
            }
            if (bestDist == 0) {
                // if we found an exact match we can bail out early, we'll never find a better one
                break;
            }
        }
        return Pair.create(bestDist, bestMatch);
    }

    public DungeonData matchDungeon(String dungeonText) {
        Pair<Integer, DungeonData> dungeonMatch = matchDungeon(dungeonText, EXTRACT_DUNGEON_JP);
        if (dungeonMatch.first <= 1) {
            System.out.println("dungeon match " + dungeonMatch.first + " : " + dungeonMatch.second);
            return dungeonMatch.second;
        } else if (dungeonText.length() > 8 && dungeonMatch.first <= 2) {
            System.out.println("extended dungeon match " + dungeonMatch.first + " : " + dungeonMatch.second);
            return dungeonMatch.second;
        } else {
            System.out.println("failed dungeon match [" + dungeonMatch.first + "] " + dungeonText);
            System.out.println("best guess: " + dungeonMatch.second.getNameEn());
            System.out.println("against:" + CjkNormalizer.normalize(dungeonText));
            System.out.println("matched:" + dungeonMatch.second.getNameJp());
            return null;
        }
    }

    public Pair<Integer, DungeonData> matchDungeon(String text, Function<DungeonData, String> f) {
        text = CjkNormalizer.normalize(text);
        text = capJpDungeonNameLength(text);
//        String firstFour = text.length() >= 4 ? text.substring(0, 4) : null;
        int bestDist = Integer.MAX_VALUE;
        DungeonData bestMatch = null;
        for (DungeonData data : dungeons) {
            String textToCheck = f.apply(data);
//            if (firstFour != null && textToCheck.length() >= 4) {
//                // special case; check if the distance between the first four characters is golden, take that
//                int nameDist = Distance.computeLevenshteinDistance(text, textToCheck);
            // need to split into parent/child dungeons first
//            }

            int nameDist = Distance.computeLevenshteinDistance(text, textToCheck);
            if (nameDist < bestDist) {
                bestDist = nameDist;
                bestMatch = data;
            }
            if (bestDist == 0) {
                // if we found an exact match we can bail out early, we'll never find a better one
                break;
            }
        }
        return Pair.create(bestDist, bestMatch);
    }

    private Function<DungeonData, String> EXTRACT_DUNGEON_JP = new Function<DungeonData, String>() {
        @Override
        public String apply(DungeonData input) {
            return input.getNameJp();
        }
    };
    private Function<MonsterData, String> EXTRACT_NAME_JP = new Function<MonsterData, String>() {
        @Override
        public String apply(MonsterData input) {
            return input.getNameJp();
        }
    };
    private Function<MonsterData, String> EXTRACT_SKILL_JP = new Function<MonsterData, String>() {
        @Override
        public String apply(MonsterData input) {
            return input.getSkillNameJp();
        }
    };
    private Function<MonsterData, String> EXTRACT_LEADER_JP = new Function<MonsterData, String>() {
        @Override
        public String apply(MonsterData input) {
            return input.getLeaderNameJp();
        }
    };

    private static String capJpDungeonNameLength(String text) {
        return text.length() >= 8 ? text.substring(0, 8) : text;
    }
}
