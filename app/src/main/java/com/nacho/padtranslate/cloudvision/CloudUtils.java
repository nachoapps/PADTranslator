package com.nacho.padtranslate.cloudvision;

import android.graphics.Rect;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.Vertex;

import java.util.List;

public class CloudUtils {

    public static Rect polyToRect(BoundingPoly poly) {
        List<Vertex> vertices = poly.getVertices();
        Preconditions.checkArgument(vertices.size() == 4);
        Vertex topLeft = vertices.get(0);
        Vertex bottomRight = vertices.get(2);
        try {
            return new Rect(topLeft.getX(), topLeft.getY(), bottomRight.getX(), bottomRight.getY());
        } catch (Exception e) {
            System.out.println("topleft: " + topLeft);
            System.out.println("bottomright: " + topLeft);
            throw e;
        }
    }
}
