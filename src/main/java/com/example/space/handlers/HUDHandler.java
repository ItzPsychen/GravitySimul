package com.example.space.handlers;

import com.example.space.App;
import com.example.space.essentials.Body;
import com.example.space.essentials.Camera;
import com.example.space.managers.TimeTravelManager;
import com.example.space.essentials.Vector2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Responsible for rendering the Heads-Up Display (HUD) overlay in the simulation.
 * <p>
 * The HUD shows information such as:
 * <ul>
 *     <li>Camera position and zoom level</li>
 *     <li>Mouse position in world coordinates</li>
 *     <li>Simulation statistics (speed, body count)</li>
 *     <li>Help glossary and time-travel information</li>
 * </ul>
 * It also displays a dynamic list of all bodies with their mass, speed, and acceleration.
 */
public class HUDHandler {

    /** Tracks the mouse position in world coordinates. */
    private final AtomicReference<Vector2D> mouseWorldPos = new AtomicReference<>(new Vector2D(0, 0));

    private static final double BODY_LIST_WIDTH = 240;
    private static final double BODY_ENTRY_HEIGHT = 20;
    private static final double CIRCLE_RADIUS = 6;
    private static final double PADDING = 5;
    private static final double TOP_OFFSET = 10;

    /**
     * Draws the HUD overlay including information text, help menu,
     * and optional body list.
     *
     * @param main       reference to the main application
     * @param gc         graphics context used for rendering
     * @param cam        the active camera
     * @param canvas     the canvas being drawn to
     * @param bodies     list of bodies currently in the simulation
     * @param bodyCount  number of bodies in the simulation
     * @param timeTravel time travel manager used for history navigation
     */
    public void drawHUD(App main, GraphicsContext gc, Camera cam, Canvas canvas, List<Body> bodies,
                        int bodyCount, TimeTravelManager timeTravel) {        gc.setFill(Color.WHITE);
        canvas.setOnMouseMoved(e -> mouseWorldPos.set(cam.screenToWorld(e.getX(), e.getY(), canvas.getWidth(), canvas.getHeight())));

        Vector2D worldPos = mouseWorldPos.get();

        String infoText = "[ " + cam.x + " ; " + cam.y + " ]" +
                "\nCanvas: " + canvas.getWidth() + " ; " + canvas.getHeight() +
                "\nPointing: " + worldPos.x + " ; " + worldPos.y +
                "\nBodies:\t" + bodyCount +
                "\nSpeed:\t" + main.sim.speed +
                "\nZoom:\t" + cam.scale + "\n\n";

        String hudText = getString(main, timeTravel, infoText);
        gc.fillText(hudText,10, 20);

        if (main.showBodiesInfo) drawBodyList(gc, canvas.getWidth(), canvas.getHeight(), bodies);

        if (main.quitHeld) {
            long now = System.nanoTime();
            double extra = (main.getMainStage().isFullScreen()) ? 0 : -40;
            if (now - main.escHoldStart < 500_000_000L) gc.fillText("Quitting", 10, canvas.getHeight() - 10 + extra);
            else if (now - main.escHoldStart < 1_000_000_000L) gc.fillText("Quitting.", 10, canvas.getHeight() - 10 + extra);
            else if (now - main.escHoldStart < 1_500_000_000L) gc.fillText("Quitting..", 10, canvas.getHeight() - 10 + extra);
            else gc.fillText("Quitting...", 10, canvas.getHeight() - 10);
        }
    }

    /**
     * Builds the HUD text string including optional info, help glossary, and time-travel info.
     *
     * @param main      the main app
     * @param timeTravel time travel manager
     * @param infoText  current info text string
     * @return a formatted HUD text string
     */
    private static String getString(App main, TimeTravelManager timeTravel, String infoText) {
        String glossaryText = """
                F11\t\t\t\tfull screen\
                
                SCROLL\t\t\tzoom\
                
                SPACE\t\t\tstop/start time\
                
                CTRL-Z\t\t\tundo last action\
                
                CLICK\t\t\tadd body\
                
                SHIFT-CLICK\t\tadd specific body\
                
                UP/DOWN\t\tspeed up/slow down\
                
                CTRL-E\t\t\terase all and reset\
                
                CTRL-F\t\t\tfollow body\
                
                CTRL-WASD\t\tchange center offset\
                
                WASD\t\t\tmove around\
                
                C\t\t\t\tcenter space\
                
                M\t\t\t\tmove only mode\
                
                N\t\t\t\tshow names\
                
                G\t\t\t\tactivate grid\
                
                T\t\t\t\ttime travel mode
                
                """;

        String timeTravelText = "RIGHT/LEFT\t\tnavigate\n" +
                "Time save\t\t\t" + (timeTravel.getCurrentHistoryIndex() + 1) + "/" + timeTravel.getSimulationHistory().size() + "\n\n";

        String hudText = (main.showInfoText) ? infoText : "";
        hudText += (main.showHelpText) ? glossaryText : "";
        hudText += (timeTravel.isTimeTravelMode()) ? timeTravelText : "";
        hudText += (main.showHelpText || main.showInfoText) ? "HOLD Q\t\t\tto quit" : "";
        return hudText;
    }

    /**
     * Draws a detailed list of all bodies on the right side of the screen.
     *
     * @param gc          graphics context
     * @param canvasWidth width of the canvas
     * @param canvasHeight height of the canvas
     * @param bodies      list of bodies to display
     */
    private void drawBodyList(GraphicsContext gc, double canvasWidth, double canvasHeight, List<Body> bodies) {
        double startX = canvasWidth - BODY_LIST_WIDTH - PADDING;
        double startY = TOP_OFFSET;

        gc.setFill(Color.rgb(20, 20, 30, 0.7));
        gc.fillRect(startX, startY, BODY_LIST_WIDTH, canvasHeight - TOP_OFFSET - PADDING);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Bodies (" + bodies.size() + ")", startX + BODY_LIST_WIDTH / 2, startY + 15);
        gc.setTextAlign(TextAlignment.LEFT);

        double idX = startX + PADDING + CIRCLE_RADIUS * 2 + 5;
        double massX = idX + 40;
        double speedX = massX + 80;
        double accelX = speedX + 60;

        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("ID", idX, startY + 30);
        gc.fillText("Mass", massX, startY + 30);
        gc.fillText("Speed", speedX, startY + 30);
        gc.fillText("Accel", accelX, startY + 30);

        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            double entryY = startY + 50 + i * BODY_ENTRY_HEIGHT;

            if (entryY > canvasHeight - PADDING - BODY_ENTRY_HEIGHT) {
                gc.setFill(Color.WHITE);
                gc.fillText("...", startX + PADDING, entryY);
                break;
            }

            gc.setFill(Color.hsb(body.mass % 360, 0.8, 0.9));
            gc.fillOval(startX + PADDING, entryY - CIRCLE_RADIUS, CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2);

            gc.setFill(Color.WHITE);
            gc.fillText(body.id, idX, entryY + 5);
            gc.fillText(String.format("%.2f", body.mass), massX, entryY + 5);
            gc.fillText(String.format("%.2f", body.getSpeed()), speedX, entryY + 5);
            gc.fillText(String.format("%.2f", body.getAccel()), accelX, entryY + 5);
        }
    }
}
