package com.nacho.tesseract;

import com.google.common.math.DoubleMath;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class BoxFinder  {
  private static class BoxParams {
    double wStart;
    double wEnd;
    double hStart;
    double hEnd;
    public BoxParams(double wStart, double wEnd, double hStart, double hEnd) {
      this.wStart = wStart; this.wEnd = wEnd; this.hStart = hStart; this.hEnd = hEnd;
    }
  }
  
  private static class BoxConfig {
    BoxParams num;
    BoxParams name;
    BoxParams active1;
    BoxParams active2;
    BoxParams leader;
    public BoxConfig(BoxParams num, BoxParams name, BoxParams active1, BoxParams active2, BoxParams leader) {
      this.num = num; this.name = name; this.active1 = active1; this.active2 = active2; this.leader = leader;
    }
  }
  
//  private static BoxConfig NINE_SIXTEEN_WITH_BUTTONS = new BoxConfig(
//          new BoxParams(.155, .350, .200, .223),
//          new BoxParams(.155, .700, .224, .245),
//          new BoxParams(.160, .600, .705, .735),
//          new BoxParams(.160, .600, .755, .780),
//          new BoxParams(.310, .850, .805, .830));
//  private static BoxConfig NINE_SIXTEEN_NO_BUTTONS = new BoxConfig(
//          new BoxParams(.155, .350, .180, .202),
//          new BoxParams(.155, .700, .202, .225),
//          new BoxParams(.160, .600, .800, .830),
//          new BoxParams(.160, .600, .850, .875),
//          new BoxParams(.310, .850, .900, .925));

  private static BoxConfig POINT_584 = new BoxConfig(
          new BoxParams(.155, .350, .150, .170),
          new BoxParams(.155, .700, .170, .190),
          new BoxParams(.160, .600, .795, .825),
          new BoxParams(.160, .600, .850, .875),
          new BoxParams(.310, .850, .900, .925));
  
  private static BoxConfig POINT_666 = new BoxConfig(
          new BoxParams(.155, .350, .170, .195),
          new BoxParams(.155, .700, .195, .220),
          new BoxParams(.160, .600, .770, .800),
          new BoxParams(.160, .600, .820, .845),
          new BoxParams(.310, .850, .885, .915));
  
  
  public static void main(String[] args) throws Exception {
    
      for (File f : new File("cropped_pix").listFiles()) {
        if (f.getName().contains("800")) { continue;}
        if (f.getName().contains("full")) { continue;}
        if (f.isDirectory()) { continue;}
        
        BufferedImage img = ImageIO.read(f);
        int w = img.getWidth();
        int h = img.getHeight();
        
        double ratio = (double)w/(double)h;
        BoxConfig config = null;
        if (DoubleMath.fuzzyEquals(ratio, .584, .01)) {
          config = POINT_584;
        } else if (DoubleMath.fuzzyEquals(ratio, .666, .01)) {
          config = POINT_666;
          continue;
        }
        
//        BoxConfig config = f.getName().contains("bar") ? NINE_SIXTEEN_WITH_BUTTONS : NINE_SIXTEEN_NO_BUTTONS;
//        BoxConfig config = CROPPED;
        Rectangle imgRect = new Rectangle(0, 0, w, h);
        Rectangle num = getRect(imgRect, config.num);
        Rectangle name = getRect(imgRect, config.name);
        Rectangle active1 = getRect(imgRect, config.active1);
        Rectangle active2 = getRect(imgRect, config.active2);
        Rectangle leader = getRect(imgRect, config.leader);
        
        drawOver(img, num, name, active1, active2, leader);
        display(img);
      }

  }
  
  public static Rectangle getRect(Rectangle rect, BoxParams params) {
    int w = rect.width;
    int h = rect.height;
    
    double wStart = params.wStart, wEnd = params.wEnd, hStart = params.hStart, hEnd = params.hEnd;
    
    double wBoxStart = w * wStart;
    double wBoxEnd = w * wEnd;
    int boxWidth = (int)(wBoxEnd - wBoxStart);
    
    double hBoxStart = h * hStart; 
    double hBoxEnd = h * hEnd; 
    int boxHeight = (int)(hBoxEnd - hBoxStart);
    return new Rectangle((int)wBoxStart + rect.x, (int)hBoxStart + rect.y, boxWidth, boxHeight);
    
  }
  
  public static void drawOver(BufferedImage img, Rectangle... boxes) {
    Graphics2D g = img.createGraphics();
    
    for (Rectangle r : boxes) {
      g.setColor(Color.black);
      g.drawRect(r.x, r.y, r.width, r.height);
    }
    
    Area rectOutside = calculateRectOutside(img, boxes);
//    g.setClip(rectOutside);
    g.setColor(Color.gray);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f));
    g.fill(rectOutside);
//    Paint p = g.getPaint();
//    int w = img.getWidth();
//    int h = img.getHeight();
////    g.setStroke(BasicStroke);
//    g.setBackground(Color.black);
//    g.fillRect(0, 0, w, (int)(h * hStart));
//    g.fillRect(0, (int)(h * hEnd), w, h);
  }
  
  private static Area calculateRectOutside(BufferedImage img, Rectangle... rects) {
    Area outside = new Area(new Rectangle2D.Double(0, 0, img.getWidth(), img.getHeight()));
    for (Rectangle r : rects) {
      System.out.println("subtracting: " + r);
      outside.subtract(new Area(r));
    }
    return outside;
  }
  
  public static void display(BufferedImage img) {
    JFrame jf = new JFrame();
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    Image scaledImg = img;
//    Image scaledImg = img.getScaledInstance(img.getWidth()/2, img.getHeight()/2, Image.SCALE_SMOOTH);
    JLabel picLabel = new JLabel(new ImageIcon(scaledImg));
    JScrollPane jp = new JScrollPane(picLabel);
    jf.getContentPane().add(jp, BorderLayout.CENTER);
//    jf.setPreferredSize(new Dimension(600,  600));
    jf.setSize(new Dimension(600,  1200));
    
//    jp.add(picLabel);
    
//    jf.pack();
    jf.setVisible(true);
  }
}