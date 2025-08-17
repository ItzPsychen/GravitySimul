package com.example.space.essentials;

/**
 * Represents the simulation camera, which manages the visible
 * portion of the world and provides transformations between
 * world and screen coordinates.
 */
public class Camera {
    /** Camera position in world coordinates. */
    public double x = 0, y = 0;

    /** Zoom scale of the camera. */
    public double scale = 1.0;

    /** Horizontal offset for rendering. */
    public double offsetX = 0;

    /** Vertical offset for rendering. */
    public double offsetY = 0;

    /** Target position when following an object. */
    private Vector2D targetPos = null;

    /**
     * Zooms the camera by a given factor, centered around the mouse world coordinates.
     *
     * @param factor       zoom factor (greater than 1 zooms in, less than 1 zooms out)
     * @param mouseWorldX  mouse x-coordinate in world space
     * @param mouseWorldY  mouse y-coordinate in world space
     */
    public void zoom(double factor, double mouseWorldX, double mouseWorldY) {
        double oldScale = scale;
        scale *= factor;
        while (scale >= 100.0) scale /= 1.001;

        x = mouseWorldX - (mouseWorldX - x) * (oldScale / scale);
        y = mouseWorldY - (mouseWorldY - y) * (oldScale / scale);
    }

    /**
     * Moves the camera by the given deltas.
     *
     * @param dx delta in x-direction
     * @param dy delta in y-direction
     */
    public void pan(double dx, double dy) {
        x += dx;
        y += dy;
    }

    /** Resets the camera position, scale, and offsets to default values. */
    public void reset() {
        x = 0;
        y = 0;
        scale = 1.0;
        offsetX = 0;
        offsetY = 0;
    }

    /**
     * Converts a world-space x-coordinate to screen space.
     *
     * @param wx          world x-coordinate
     * @param canvasWidth canvas width in pixels
     * @return screen-space x-coordinate
     */
    public double worldToScreenX(double wx, double canvasWidth) {
        return (wx - x) * scale + canvasWidth / 2.0 + offsetX;
    }

    /**
     * Converts a world-space y-coordinate to screen space.
     *
     * @param wy           world y-coordinate
     * @param canvasHeight canvas height in pixels
     * @return screen-space y-coordinate
     */
    public double worldToScreenY(double wy, double canvasHeight) {
        return (wy - y) * scale + canvasHeight / 2.0 + offsetY;
    }

    /**
     * Converts screen coordinates to world coordinates.
     *
     * @param sx           screen x-coordinate
     * @param sy           screen y-coordinate
     * @param canvasWidth  canvas width in pixels
     * @param canvasHeight canvas height in pixels
     * @return world-space position vector
     */
    public Vector2D screenToWorld(double sx, double sy, double canvasWidth, double canvasHeight) {
        double wx = (sx - canvasWidth / 2.0 - offsetX) / scale + x;
        double wy = (sy - canvasHeight / 2.0 - offsetY) / scale + y;
        return new Vector2D(wx, wy);
    }

    /**
     * Sets a follow target for the camera.
     *
     * @param target target position vector
     */
    public void setFollowTarget(Vector2D target) {
        this.targetPos = target;
    }

    /** Clears the follow target. */
    public void clearFollowTarget() {
        this.targetPos = null;
    }

    /** Updates camera position to follow the target if one is set. */
    public void updateFollow() {
        if (targetPos != null) {
            x = targetPos.x;
            y = targetPos.y;
        }
    }
}
