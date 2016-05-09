package com.nacho.padtranslate.cloudvision;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Encapsulates the result of OCR.
 */
public class CloudVisionResult {

    private Bitmap data;
    private List<EntityAnnotation> annotations;
    private List<TextLine> lines;

    public CloudVisionResult(Bitmap data, List<EntityAnnotation> annotations) {
        this.data = data;
        this.annotations = new ArrayList<>(annotations);
        this.annotations.remove(0); // get rid of the first overall summary result
        this.lines = combine(this.annotations);
    }

    public Bitmap getData() {
        return data;
    }

    public List<EntityAnnotation> getAnnotations() {
        return annotations;
    }

    public List<TextLine> getLines() {
        return lines;
    }

    private static List<TextLine> combine(List<EntityAnnotation> annotations) {
        List<TextLine> lines = new ArrayList<>();
        for (EntityAnnotation a : annotations){
            TextChunk chunk = new TextChunk(a);
            boolean added = false;
            for (TextLine line : lines) {
                if (line.tryAddChunk(chunk)) {
                    added = true;
                    break;
                }
            }
            if (!added) {
                lines.add(new TextLine(chunk));
            }
        }
        return lines;
    }

    public static class TextLine {
        // when collapsing chunks in a line, remember to allow for gaps, causing a split in the TextLine
        private List<TextChunk> chunks;
        private double midSum;
        private double widthSum;
        private double heightSum;

        public TextLine(TextChunk chunk) {
            chunks = new ArrayList<>();
            addChunk(chunk);
        }

        private void addChunk(TextChunk chunk) {
            chunks.add(chunk);
            midSum += chunk.box.exactCenterY();
            widthSum += chunk.box.width();
            heightSum += chunk.box.height();
        }

        public boolean tryAddChunk(TextChunk chunk) {
            double avgMid = midSum/chunks.size();
            double newBoxMaxDelta = getHeightAvg() / 4d; //chunk.box.height()/4d; // half of half the chunk height
            double delta = Math.abs(chunk.box.exactCenterY() - avgMid);
            if (delta < newBoxMaxDelta) {
                addChunk(chunk);
                return true;
            }
            return false;
        }

        public double getHeightAvg() {
            return heightSum / chunks.size();
        }

        public List<TextChunk> getChunks() {
            return chunks;
        }
    }

    public static class TextChunk {
        Rect box;
        String text;
        Float confidence;
        public TextChunk(EntityAnnotation annotation) {
            this.box = CloudUtils.polyToRect(annotation.getBoundingPoly());
            this.text = annotation.getDescription();
            this.confidence = annotation.getConfidence();
            shrinkBox(this.box, this.text);
        }
    }

    // each character can be at most height*ratio.
    // could improve by calculating character for font size?
    private static double widthHeightRatio = 1.2d;

    public static Rect shrinkBox(Rect box, String text) {
        double maxWidth = widthHeightRatio * box.height() * text.length();
        if (box.width() > maxWidth) {
            box.right = box.left + (int)maxWidth;
        }
        return box;
    }

    public static class SortedTextLine {
        List<TextChunk> chunks;
        Rect lineRect;
        private double acceptWidth;
        private String text;

        public SortedTextLine(TextChunk chunk, double avgHeight) {
            this.chunks = new ArrayList<>();
            this.acceptWidth = widthHeightRatio * avgHeight * 4; // guestimation, maybe use a better method, 4 characters apart
            this.lineRect = new Rect(chunk.box);
            this.text = "";
            addChunk(chunk);

        }
        public void addChunk(TextChunk chunk) {
            chunks.add(chunk);
            lineRect.union(chunk.box);
            this.text += " " + chunk.text;
        }

        public boolean tryAdd(TextChunk chunk) {
            int delta = Math.abs(EXTRACT_LEFT.apply(chunk) - lineRect.right);
            if (delta < acceptWidth) {
                addChunk(chunk);
                return true;
            }
            return false;
        }

        public Rect getLineRect() {
            return lineRect;
        }

        public String getText() {
            return this.text.trim();
        }

        @Override
        public String toString() {
            return "SortedTextLine{" +
                    "text='" + text + '\'' +
                    ", lineRect=" + lineRect +
                    '}';
        }
    }

    public static List<SortedTextLine> extractSortedTextLines(TextLine line) {
        List<TextChunk> sortedChunks = Ordering.natural().onResultOf(EXTRACT_LEFT).sortedCopy(line.getChunks());
        // maybe do some validation on overlaps
        double heightAvg = line.getHeightAvg();

        List<SortedTextLine> results = new ArrayList<>();
        SortedTextLine curLine = new SortedTextLine(sortedChunks.get(0), heightAvg);
        for (int i = 1; i < sortedChunks.size(); i++) {
            TextChunk chunk = sortedChunks.get(i);
            if (!curLine.tryAdd(chunk)) {
                results.add(curLine);
                curLine = new SortedTextLine(chunk, heightAvg);
            }
        }
        results.add(curLine);
        return results;
    }

    private static Function<TextChunk, Integer> EXTRACT_LEFT = new Function<TextChunk, Integer>() {
        @Nullable @Override
        public Integer apply(@Nullable TextChunk input) {
            return input.box.left;
        }
    };
}


