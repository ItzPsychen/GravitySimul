package com.example.space.managers;

import com.example.space.*;
import com.example.space.essentials.Body;
import com.example.space.essentials.Camera;
import com.example.space.essentials.Simulation;
import com.example.space.handlers.GridHandler;
import com.example.space.handlers.HUDHandler;
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
        gc.setFill(Color.rgb(5, 5, 20));
        gc.fillRect(0, 0, canvas.getWidth(), h);

        if (main.showGrid) gridRenderer.drawGrid(gc, canvas.getWidth(), h, cam);

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

        if (main.following && main.bodyFollowed != null) cam.setFollowTarget(main.bodyFollowed.pos);
        else cam.clearFollowTarget();

        cam.updateFollow();

        hudRenderer.drawHUD(main, gc, cam, canvas, sim.bodies, sim.bodies.size(), timeTravel);
    }
}