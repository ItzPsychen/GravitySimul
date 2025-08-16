package com.example.space.Essentials;

public class Camera {
    public double x = 0, y = 0;
    public double scale = 1.0;
    public double offsetX = 0;
    public double offsetY = 0;
    public double toolBarOffset = 0;
    private Vector2D targetPos = null;

    public double windowWidth = 1920;
    public double windowHeight = 1080;

    public void zoom(double factor, double mouseWorldX, double mouseWorldY) {
        double oldScale = scale;
        scale *= factor;
        while (scale >= 100.0) scale /= 1.001;

        x = mouseWorldX - (mouseWorldX - x) * (oldScale / scale);
        y = mouseWorldY - (mouseWorldY - y) * (oldScale / scale);
    }
    public void pan(double dx, double dy) {
        x += dx;
        y += dy;
    }
    public void reset() {
        x = 0;
        y = 0;
        scale = 1.0;
        offsetX = 0;
        offsetY = 0;
    }

    public double worldToScreenX(double wx, double canvasWidth) {
        return (wx - x) * scale + canvasWidth / 2.0 + offsetX;
    }
    public double worldToScreenY(double wy, double canvasHeight) {
        return (wy - y) * scale + canvasHeight / 2.0 + offsetY;
    }
    public Vector2D screenToWorld(double sx, double sy, double canvasWidth, double canvasHeight){
        double wx = (sx - canvasWidth / 2.0 - offsetX) / scale + x;
        double wy = (sy - canvasHeight / 2.0 - offsetY) / scale + y;
        return new Vector2D(wx, wy);
    }

    public void setFollowTarget(Vector2D target) {
        this.targetPos = target;
    }
    public void clearFollowTarget() {
        this.targetPos = null;
    }
    public void updateFollow() {
        if (targetPos != null) {
            x = targetPos.x;
            y = targetPos.y;
        }
    }
}
