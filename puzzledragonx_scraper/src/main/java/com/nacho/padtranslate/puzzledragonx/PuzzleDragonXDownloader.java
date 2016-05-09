package com.nacho.padtranslate.puzzledragonx;

import com.google.common.io.CharStreams;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

public class PuzzleDragonXDownloader {
  // minvalue 1
//  static int startNum = 2009;
//  static int endNum = 2010;
//  static int startNum = 1;
  static int startNum = 2389;
  static int endNum = 2881;
  static boolean overwrite = true;
  
  //------------------------------------------------------------
  static String monsterPage = "http://www.puzzledragonx.com/en/monster.asp?n=";
  static String skillPage = "http://www.puzzledragonx.com/en/skill.asp?s=";
  static String leaderPage = "http://www.puzzledragonx.com/en/leaderskill.asp?s=";
  //------------------------------------------------------------
  static String enNamePrefix = "Name</span>:</td><td class=\"data\">";
  static String enNameSuffix = "</td>";
  
  static String jpNamePrefix = "<td class=\"data jap\">";
  static String jpNameSuffix = "</td>";
  //------------------------------------------------------------
  static String skillIdPrefix = "<a href=\"skill.asp?s=";
  static String skillIdSuffix = "\">";
  
  static String skillNameJpPrefix = "JP Ver:</td><td class=\"value-end\">";
  static String skillNameJpSuffix = "</td>";
  
  static String skillEffectPrefix = "Effect:</td><td class=\"value-end\">";
  static String skillEffectSuffix = "</td>";
  //------------------------------------------------------------
  static String leaderIdPrefix = "<a href=\"leaderskill.asp?s=";
  static String leaderIdSuffix = "\">";
  
  static String leaderNameJpPrefix = "JP Ver:</td><td class=\"value-end\">";
  static String leaderNameJpSuffix = "</td>";
  
  static String leaderEffectPrefix = "Effect:</td><td class=\"value-end\">";
  static String leaderEffectSuffix = "</td>";
  //------------------------------------------------------------
  
  public static void main(String[] args) throws Exception {
    File root = new File("pdx_data");
    root.mkdir();
    
    int skippedCount = 0;
    int retrievedCount = 0;
    for (int x = startNum; x < endNum; x++) {
      File f = new File(root, x + ".csv");
      if (f.exists() && !overwrite) {
        System.out.println("skipping " + x);
        skippedCount++;
        continue;
      }
      
      String pageContent = loadPage(monsterPage + x);
      String enName = extract(enNamePrefix, enNameSuffix, pageContent, false);
      String jpName = extract(jpNamePrefix, jpNameSuffix, pageContent, false);
      
      String skillId = extract(skillIdPrefix, skillIdSuffix, pageContent, false);
      String skillNameJp = "";
      String skillText = "";
      if (skillId != "") {
        String skillContent = loadPage(skillPage + skillId);
        skillNameJp = extract(skillNameJpPrefix, skillNameJpSuffix, skillContent, true);
        skillText = extract(skillEffectPrefix, skillEffectSuffix, skillContent, true);
      }
      
      String leaderId = extract(leaderIdPrefix, leaderIdSuffix, pageContent, false);
      String leaderNameJp = "";
      String leaderText = "";
      if (leaderId != "") {
        String leaderContent = loadPage(leaderPage + leaderId);
        leaderNameJp = extract(leaderNameJpPrefix, leaderNameJpSuffix, leaderContent, true);
        leaderText = extract(leaderEffectPrefix, leaderEffectSuffix, leaderContent, true);
      }
      
      CSVWriter writer = new CSVWriter(new FileWriter(f));
      String[] line = {
          Integer.toString(x),
          enName,
          jpName,
          skillId,
          skillNameJp,
          skillText,
          leaderId,
          leaderNameJp,
          leaderText
      };
      writer.writeNext(line);
      writer.close();
      
      System.out.println("done writing " + x);
      retrievedCount++;
      Thread.sleep(1000);
    }
    
    System.out.println("done skipping: " + skippedCount);
    System.out.println("done retrieving: " + retrievedCount);
    
    File summaryDir = new File("pdx_summary");
    summaryDir.mkdir();
    
    PrintWriter pw = new PrintWriter(new FileWriter(new File(summaryDir, "training.txt")));
    for (int i = 0; i < 100; i++) {
      pw.println("No." + i);
    }
    
    CSVWriter csvw = new CSVWriter(new FileWriter(new File(summaryDir, "monsters.csv")));
    csvw.writeNext(new String[]{
        "number","en_name","jp_name",
        "skill_id","jp_skill_name","en_skill_text",
        "leader_id","jp_leader_name","en_leader_text",
        "jp_name_normalized","jp_skill_name_normalized","jp_leader_name_normalized"
    });
    
    int totalCount = 0;
    for (String fileName : root.list()) {
      CSVReader reader = new CSVReader(new FileReader(new File(root, fileName)));
//      String text = Files.readFirstLine(new File(root, fileName), StandardCharsets.UTF_8);
//      pw.println(text);
      String[] line = reader.readNext();
      String[] linePlusNormalized = new String[line.length + 3];
      System.arraycopy(line, 0, linePlusNormalized, 0, line.length);
      linePlusNormalized[line.length + 0] = CjkNormalizer.normalize(line[2]);
      linePlusNormalized[line.length + 1] = CjkNormalizer.normalize(line[4]);
      linePlusNormalized[line.length + 2] = CjkNormalizer.normalize(line[7]);
      csvw.writeNext(linePlusNormalized);
      maybePrint(pw, line[2]);
      maybePrint(pw, line[4]);
      maybePrint(pw, line[7]);
      reader.close();
      
      totalCount++;
    }
    csvw.close();
    pw.close();
    
    System.out.println("done with monsters: " + totalCount);
  }
  
  private static void maybePrint(PrintWriter pw, String text) {
    text = text.trim();
    if (text.isEmpty()) {return;}
    pw.println(text);
  }
  
  private static String loadPage(String url) throws Exception {
    URL u = new URL(url);
    InputStream is = u.openStream();
    InputStreamReader reader = new InputStreamReader(is);
    String pageContent = CharStreams.toString(reader);
    reader.close(); is.close();
    return pageContent;
  }
  
  private static String extract(String prefix, String suffix, String content, boolean secondVersionPossible) {
    int start = content.indexOf(prefix);
    if (start < 0) {
      return "";
    }
    
    start = start + prefix.length();
    int secondStart = content.indexOf(prefix, start);
    if (secondVersionPossible && secondStart > 0) {
      // if there's a second english description, it's for the JP version of the skill, that's what we want
      start = secondStart + prefix.length();
    }
    int end = content.indexOf(suffix, start);
    return content.substring(start, end);
  }

}
