package com.nacho.tesseract;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

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
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class TesseractExample {
  public static void main(String[] args) throws Exception {
    Tesseract instance = new Tesseract(); // JNA Interface Mapping
//    instance.setConfigs(new ArrayList<String>());
    instance.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SPARSE_TEXT_OSD);
    instance.setLanguage("v1.2_jpn");
    
    BufferedImage img = ImageIO.read(new File(new File("other_pix"), "postCropped.png"));

      try {
          String result = instance.doOCR(img);
//          TessAPI.INSTANCE.TessBaseAPIGetThresholdedImage(instance);
//          ImageIO.write(im, formatName, output)
          System.out.println("Done " + result);
          List<Rectangle> rects = instance.getSegmentedRegions(img, TessPageIteratorLevel.RIL_TEXTLINE);
          drawOver(img, rects.toArray(new Rectangle[0]));
          display(img);
          ImageIO.write(img, "png", new File(new File("other_pix"), "annotated.png"));
      } catch (TesseractException e) {
          System.err.println(e.getMessage());
      }
  }

  public static Rectangle getRect(Rectangle rect, double wStart, double wEnd, double hStart,
          double hEnd) {
    int w = rect.width;
    int h = rect.height;

    double wBoxStart = w * wStart;
    double wBoxEnd = w * wEnd;
    int boxWidth = (int) (wBoxEnd - wBoxStart);

    double hBoxStart = h * hStart;
    double hBoxEnd = h * hEnd;
    int boxHeight = (int) (hBoxEnd - hBoxStart);
    return new Rectangle((int) wBoxStart + rect.x, (int) hBoxStart + rect.y, boxWidth, boxHeight);

  }

  public static void drawOver(BufferedImage img, Rectangle... boxes) {
    Graphics2D g = img.createGraphics();

    for (Rectangle r : boxes) {
      g.setColor(Color.black);
      g.drawRect(r.x, r.y, r.width, r.height);
    }

    Area rectOutside = calculateRectOutside(img, boxes);
    // g.setClip(rectOutside);
    g.setColor(Color.gray);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f));
    g.fill(rectOutside);
    // Paint p = g.getPaint();
    // int w = img.getWidth();
    // int h = img.getHeight();
    //// g.setStroke(BasicStroke);
    // g.setBackground(Color.black);
    // g.fillRect(0, 0, w, (int)(h * hStart));
    // g.fillRect(0, (int)(h * hEnd), w, h);
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

//    Image scaledImg = img;
     Image scaledImg = img.getScaledInstance(img.getWidth()/2,
                                             img.getHeight()/2, 
                                             Image.SCALE_SMOOTH);
    JLabel picLabel = new JLabel(new ImageIcon(scaledImg));
    JScrollPane jp = new JScrollPane(picLabel);
    jf.getContentPane().add(jp, BorderLayout.CENTER);
    // jf.setPreferredSize(new Dimension(600, 600));
    jf.setSize(new Dimension(600, 1200));

    // jp.add(picLabel);

    // jf.pack();
    jf.setVisible(true);
  }
}