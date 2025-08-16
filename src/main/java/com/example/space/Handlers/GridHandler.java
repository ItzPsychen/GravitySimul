package com.example.space.Handlers;

import com.example.space.Essentials.Camera;
import com.example.space.Essentials.Vector2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GridHandler {
    public void drawGrid(GraphicsContext gc, double w, double h, Camera cam) {
        Vector2D topLeft = cam.screenToWorld(0, 0, w, h);
        Vector2D bottomRight = cam.screenToWorld(w, h, w, h);

        double spacing = 0.01;
        double screenSpacing = spacing * cam.scale;
        while (screenSpacing < 2 || screenSpacing > 200) {
            spacing *= 10.0;
            screenSpacing = spacing * cam.scale;
        }

        for (int i = 0; i < 3; ++i) {
            double alpha;
            double fadePeak = 30;
            double fadeEnd = 100;

            if (screenSpacing <= fadePeak) alpha = 0.2 + 0.8 * (screenSpacing / fadePeak);
            else alpha = Math.max(0.1, 1.0 - (screenSpacing - fadePeak) / (fadeEnd - fadePeak));

            if (i == 0) {
                alpha *= 0.3;
                gc.setLineWidth(0.8);
            } else if (i == 1) {
                alpha *= 0.5;
                gc.setLineWidth(1.0);
            } else {
                alpha *= 0.8;
                gc.setLineWidth(1.2);
            }

            gc.setStroke(Color.gray(0.6, alpha));

            double startX = Math.floor(topLeft.x / spacing) * spacing;
            double endX = Math.ceil(bottomRight.x / spacing) * spacing;
            for (double x = startX; x <= endX; x += spacing) {
                double sx = cam.worldToScreenX(x, w);
                gc.strokeLine(sx, 0, sx, h);
            }

            double startY = Math.floor(topLeft.y / spacing) * spacing;
            double endY = Math.ceil(bottomRight.y / spacing) * spacing;
            for (double y = startY; y <= endY; y += spacing) {
                double sy = cam.worldToScreenY(y, h);
                gc.strokeLine(0, sy, w, sy);
            }

            spacing *= 10.0;
            screenSpacing = spacing * cam.scale;
        }

        if (topLeft.x <= 0 && bottomRight.x >= 0) {
            double sx = cam.worldToScreenX(0, w);
            gc.setStroke(Color.BLUE.deriveColor(0, 1, 1, 0.5));
            gc.setLineWidth(1.5);
            gc.strokeLine(sx, 0, sx, h);
        }

        if (topLeft.y <= 0 && bottomRight.y >= 0) {
            double sy = cam.worldToScreenY(0, h);
            gc.setStroke(Color.RED.deriveColor(0, 1, 1, 0.5));
            gc.setLineWidth(1.5);
            gc.strokeLine(0, sy, w, sy);
        }
    }
}
