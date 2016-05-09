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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class PuzzleDragonXDungeonDownloader {
  // minvalue 1
//  static int startNum = 1;
//  static int endNum = 10;
  static int startNum = 1;
  static int endNum = 2120;
  static boolean overwrite = true;
  
  //------------------------------------------------------------
  static String missionPage = "http://www.puzzledragonx.com/en/mission.asp?m=";
  //------------------------------------------------------------
  // 
  // <td class="title value-end nowrap" style="min-width: 100px;">

  static String missionNamePrefix = "<td class=\"value-end nowrap\" style=\"min-width: 100px;\">"; 
  static String dungeonNamePrefix = "<td class=\"title value-end nowrap\" style=\"min-width: 100px;\">";
  static String missionNameSuffix = "</td>";
  static String dungeonNameSuffix = "</td>";
  
  static String jpNamePrefix = "<span class=\"jap\">";
  static String jpNameSuffix = "</span>";
  //------------------------------------------------------------
  
  public static void main(String[] args) throws Exception {
    File root = new File("pdx_dungeon_data");
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
      
      String pageContent = loadPage(missionPage + x);
      String missionName = extract(missionNamePrefix, missionNameSuffix, pageContent, false);
      String dungeonName = extract(dungeonNamePrefix, dungeonNameSuffix, pageContent, false);
      
      String jpMissionName = extract(jpNamePrefix, jpNameSuffix, pageContent, false);
      String pastName1Content = pageContent.substring(pageContent.indexOf(jpMissionName));
      String jpDungeonName = extract(jpNamePrefix, jpNameSuffix, pastName1Content, false);
      
      CSVWriter writer = new CSVWriter(new FileWriter(f));
      writer.writeNext(new String[] {
          Integer.toString(x),
          missionName,
          jpMissionName
      });
      writer.writeNext(new String[] {
          Integer.toString(x),
          dungeonName,
          jpDungeonName
      });
      writer.close();
      
      System.out.println("done writing " + x);
      retrievedCount++;
      Thread.sleep(1000);
    }
    
    System.out.println("done skipping: " + skippedCount);
    System.out.println("done retrieving: " + retrievedCount);
    
    
    int totalCount = 0;
    
    Map<String, String> jpNameToEnName = new HashMap<>();
    for (String fileName : root.list()) {
      CSVReader reader = new CSVReader(new FileReader(new File(root, fileName)));
      Iterator<String[]> readerIter = reader.iterator();
      while (readerIter.hasNext()) {
        String[] line = readerIter.next();
        
        String enName = line[1];
        String jpName = line[2];
        if (enName.isEmpty()) {
          continue;
        }
        jpNameToEnName.put(jpName, enName);
        totalCount++;
      }
      reader.close();
    }

    File summaryDir = new File("pdx_dungeon_summary");
    summaryDir.mkdir();

    PrintWriter pw = new PrintWriter(new FileWriter(new File(summaryDir, "training.txt")));
    pw.println("Clear!");
    pw.println("1/2");
    pw.println("!");
    pw.println("!!");
    pw.println("+");
    pw.println("∞");
    pw.println("7x6");
    pw.println("[ ] [] [XX]");
    
    CSVWriter csvw = new CSVWriter(new FileWriter(new File(summaryDir, "dungeons.csv")));
    csvw.writeNext(new String[]{
        "en_name","jp_name","jp_name_normalized"
    });
    
    for (Entry<String, String> entry : jpNameToEnName.entrySet()) {
      String enName = entry.getValue();
      String jpName = entry.getKey();
      csvw.writeNext(new String[]{
          enName,
          jpName,
          CjkNormalizer.normalize(jpName)
      });
      pw.println(jpName);
    }
    csvw.close();
    pw.close();
    
    System.out.println("done with dungeons: " + totalCount);
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
    return content.substring(start, end).trim();
  }

}
