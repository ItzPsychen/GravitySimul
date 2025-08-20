package com.example.space.managers;

import com.example.space.*;
import com.example.space.essentials.Body;
import com.example.space.essentials.Camera;
import com.example.space.essentials.Simulation;
import com.example.space.essentials.Vector2D;
import com.example.space.handlers.GridHandler;
import com.example.space.handlers.HUDHandler;
import com.example.space.handlers.InputHandler;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Manages the rendering of the simulation, including the main game loop,
 * body rendering, and camera updates.
 */
public class RenderManager {
    private final App main;
    private final Simulation sim;
    private final Camera cam;
    private final TimeTravelManager timeTravel;
    private final GridHandler gridRenderer;
    private final HUDHandler hudRenderer;

    private long lastNano = 0;
    private long lastFrameTime = System.nanoTime();

    /**
     * Constructs a new RenderManager.
     *
     * @param main          the main application instance
     * @param sim           the simulation to render
     * @param cam           the camera controlling the view
     * @param timeTravel    the time travel manager
     * @param gridRenderer  the grid rendering handler
     * @param hudRenderer   the HUD rendering handler
     */
    public RenderManager(App main, Simulation sim, Camera cam, TimeTravelManager timeTravel,
                         GridHandler gridRenderer, HUDHandler hudRenderer) {
        this.main = main;
        this.sim = sim;
        this.cam = cam;
        this.timeTravel = timeTravel;
        this.gridRenderer = gridRenderer;
        this.hudRenderer = hudRenderer;
    }

    /**
     * Starts the main rendering loop.
     *
     * @param canvas the canvas to render on
     * @param gc     the graphics context for drawing
     */
    public void startMainLoop(Canvas canvas, GraphicsContext gc) {
        AnimationTimer loop = new AnimationTimer() {
            private long lastSaveTime = 0;

            @Override
            public void handle(long now) {
                if (lastNano == 0) lastNano = now;
                double elapsedSec = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;
                double dt = elapsedSec * main.sim.speed;

                if (sim.isRunning() && !timeTravel.isTimeTravelMode()) {
                    if (now - lastSaveTime > 500_000_000L) {
                        timeTravel.saveSimulationState();
                        lastSaveTime = now;
                    }
                    int subSteps = 2;
                    for (int i = 0; i < subSteps; i++) sim.step(dt / subSteps);
                }

                if (main.quitHeld && now - main.escHoldStart >= App.ESC_HOLD_DURATION_NS) System.exit(0);

                render(gc, canvas, canvas.getHeight());
            }
        };
        timeTravel.saveSimulationState();
        loop.start();
    }

    /**
     * Renders the simulation (bodies, grid, and HUD).
     *
     * @param gc     the graphics context
     * @param canvas the canvas to draw on
     * @param h      the canvas height
     */
    void render(GraphicsContext gc, Canvas canvas, double h) {
        InputHandler.AimingData aim = main.inputHandler.aimingData.copy();
        gc.setFill(Color.rgb(5, 5, 20));
        gc.fillRect(0, 0, canvas.getWidth(), h);

        if (main.showGrid) gridRenderer.drawGrid(gc, canvas.getWidth(), h, cam);

        if (main.aimingMode && main.inputHandler != null && main.inputHandler.aimingData != null) {
            double dx = aim.curr.x - aim.screenStart.x;
            double dy = aim.curr.y - aim.screenStart.y;
            double moveCheck = Math.hypot(aim.curr.x, aim.curr.y);

            if (moveCheck > 0) {
                Vector2D opp = new Vector2D(
                        aim.screenStart.x - dx,
                        aim.screenStart.y - dy
                );

                drawArrowhead(gc, aim.screenStart.x, aim.screenStart.y, opp.x, opp.y);

                gc.setLineDashes(0);
                gc.setLineWidth(1.0);

                double angleRad = Math.atan2(dy, dx);
                double angleDeg = Math.toDegrees(Math.PI - angleRad);
                double speed = Math.sqrt(dx * dx + dy * dy) / 5;

                gc.setFill(Color.WHITE);
                gc.fillText(String.format("Angle: %.1f°  Speed: %.1f", angleDeg, speed),
                        aim.screenStart.x + 10, aim.screenStart.y - 10);
            }
        }

        for (Body b : sim.bodies) {
            double sx = cam.worldToScreenX(b.pos.x, canvas.getWidth());
            double sy = cam.worldToScreenY(b.pos.y, h);
            double sr = Math.max(2, b.radius * cam.scale);
            gc.setFill(Color.hsb((b.mass % 360), 0.8, 0.9));
            gc.fillOval(sx - sr / 2, sy - sr / 2, sr, sr);
            gc.setStroke(Color.WHITE);
            gc.strokeLine(sx, sy, sx + b.vel.x * 0.2 * cam.scale, sy + b.vel.y * 0.2 * cam.scale);

            if (main.showNames) gc.fillText(b.id, sx - 10, sy + sr + 12);
        }

        if (main.following && sim.bodyFollowed != null) cam.setFollowTarget(sim.bodyFollowed.pos);
        else cam.clearFollowTarget();

        cam.updateFollow();

        hudRenderer.drawHUD(main, gc, cam, canvas, sim.bodies, sim.bodies.size(), timeTravel);
    }

    /**
     * Draws an arrow from a starting point to an end point on the given {@link GraphicsContext}.
     * <p>
     * The arrow consists of a line from the start to the base of the arrowhead, and a filled
     * triangular arrowhead at the tip. The arrowhead size and offset are fixed.
     *
     * @param gc     the {@link GraphicsContext} used for drawing
     * @param startX the X coordinate of the arrow start
     * @param startY the Y coordinate of the arrow start
     * @param endX   the X coordinate of the arrow tip
     * @param endY   the Y coordinate of the arrow tip
     */
    private void drawArrowhead(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        double arrowLength = 10;
        double headOffset = 8;
        double angle = Math.atan2(endY - startY, endX - startX);

        double tipX = endX + headOffset * Math.cos(angle);
        double tipY = endY + headOffset * Math.sin(angle);

        double baseX = tipX - arrowLength * Math.cos(angle);
        double baseY = tipY - arrowLength * Math.sin(angle);

        double x1 = baseX - arrowLength * 0.5 * Math.cos(angle - Math.PI / 2);
        double y1 = baseY - arrowLength * 0.5 * Math.sin(angle - Math.PI / 2);
        double x2 = baseX - arrowLength * 0.5 * Math.cos(angle + Math.PI / 2);
        double y2 = baseY - arrowLength * 0.5 * Math.sin(angle + Math.PI / 2);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeLine(startX, startY, baseX, baseY);

        gc.setFill(Color.WHITE);
        gc.fillPolygon(new double[]{tipX, x1, x2}, new double[]{tipY, y1, y2}, 3);
    }
}