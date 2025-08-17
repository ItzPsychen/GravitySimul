package com.example.space;

import com.example.space.essentials.Body;
import com.example.space.essentials.Camera;
import com.example.space.essentials.Simulation;
import com.example.space.essentials.Vector2D;
import com.example.space.handlers.GridHandler;
import com.example.space.handlers.HUDHandler;
import com.example.space.handlers.InputHandler;
import com.example.space.managers.RenderManager;
import com.example.space.managers.TimeTravelManager;
import com.example.space.managers.UIManager;
import com.example.space.prompts.FollowPrompt;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Main application class for the Space Gravity Simulator.
 * <p>
 * This class initializes and manages the simulation, rendering, input handling,
 * and UI components. It uses JavaFX to create a fullscreen simulation environment
 * where users can interact with celestial bodies, follow them, and explore
 * gravitational dynamics.
 * </p>
 */
public class App extends Application {

    /** Main application stage (window). */
    private Stage mainStage;

    /** Core physics simulation instance. */
    public final Simulation sim = new Simulation();

    /** Camera instance for handling world-to-screen transformations. */
    public final Camera cam = new Camera();

    /** Window width property bound to the screen's visual bounds. */
    public final DoubleProperty windowWidth = new SimpleDoubleProperty(Screen.getPrimary().getVisualBounds().getWidth());

    /** Window height property bound to the screen's visual bounds. */
    public final DoubleProperty windowHeight = new SimpleDoubleProperty(Screen.getPrimary().getVisualBounds().getHeight());

    /** Default mass for newly created bodies. */
    public final double defMass = 50.0;

    /** Whether the simulator is in move-only mode. */
    public boolean moveOnlyMode = false;

    /** Whether names of bodies are displayed. */
    public boolean showNames = false;

    /** Whether help text is displayed. */
    public boolean showHelpText = false;

    /** Whether the grid is rendered. */
    public boolean showGrid = true;

    /** Whether simulation information text is displayed. */
    public boolean showInfoText = true;

    /** Whether body information is displayed. */
    public boolean showBodiesInfo = true;

    /** Whether the quit command is currently being held. */
    public boolean quitHeld = false;

    /** Timestamp when escape key started being held. */
    public long escHoldStart = 0;

    /** Duration (in nanoseconds) the escape key must be held to quit. */
    public static final long ESC_HOLD_DURATION_NS = 2_000_000_000L;

    /** Body currently being followed. */
    public Body bodyFollowed = null;

    /** Whether the simulation is currently following a body. */
    public boolean following = false;

    /** List of removed bodies (e.g., for undo or time travel). */
    public List<Body> removedBodies;

    /** Manager for time travel functionality. */
    public TimeTravelManager timeTravel;

    /** Grid rendering handler. */
    public GridHandler gridRenderer;

    /** HUD (Heads-Up Display) rendering handler. */
    public HUDHandler hudRenderer;

    /** UI Manager for handling interactive elements. */
    public UIManager uiManager;

    /** Rendering manager controlling the draw loop. */
    public RenderManager renderManager;

    /**
     * Gets the primary application stage.
     *
     * @return the main JavaFX stage
     */
    public Stage getMainStage() {
        return mainStage;
    }

    /**
     * Entry point for the JavaFX application.
     * <p>
     * Initializes the simulation, UI, rendering system, and input handlers.
     * Sets up the primary stage in fullscreen mode.
     * </p>
     *
     * @param stage the primary stage provided by the JavaFX runtime
     */
    @Override
    public void start(Stage stage) {
        this.mainStage = stage;

        // Configure simulation constants and add initial body (star)
        sim.G = 6.67430e-3;
        sim.addBody(new Body("Star", 1e6, new Vector2D(0, 0), new Vector2D(0, 0), 20));

        // Initialize managers and handlers
        timeTravel = new TimeTravelManager(sim);
        gridRenderer = new GridHandler();
        hudRenderer = new HUDHandler();
        removedBodies = new ArrayList<>();

        uiManager = new UIManager(this, stage, sim, cam, timeTravel);
        Scene mainScene = uiManager.setupUI(windowWidth, windowHeight);

        renderManager = new RenderManager(this, sim, cam, timeTravel, gridRenderer, hudRenderer);

        // Prepare canvas graphics context
        Canvas canvas = (Canvas) ((VBox) ((StackPane) mainScene.getRoot()).getChildren().get(0)).getChildren().get(1);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Configure stage
        stage.setScene(mainScene);
        stage.getScene().setFill(Color.BLACK);
        stage.setFullScreen(true);
        stage.setTitle("Space Gravity Simulator");
        stage.show();

        // Defer input attachment and rendering loop start until after UI loads
        Platform.runLater(() -> {
            InputHandler inputHandler = new InputHandler(this, sim, cam, uiManager, timeTravel);
            inputHandler.attach(canvas, mainScene);
            renderManager.startMainLoop(canvas, gc);
        });
    }

    /**
     * Finds a body located at a specific mouse position on the canvas.
     *
     * @param mouseX       the x-coordinate of the mouse in screen space
     * @param mouseY       the y-coordinate of the mouse in screen space
     * @param canvasWidth  the width of the canvas
     * @param canvasHeight the height of the canvas
     * @return the {@link Body} found at the given screen coordinates, or {@code null} if none exists
     */
    public Body findBodyAt(double mouseX, double mouseY, double canvasWidth, double canvasHeight) {
        Vector2D worldPos = cam.screenToWorld(mouseX, mouseY, canvasWidth, canvasHeight);
        for (Body b : sim.bodies) {
            if (b.pos.sub(worldPos).mag() < b.radius) {
                return b;
            }
        }
        return null;
    }

    /**
     * Starts following a selected body.
     * <p>
     * Opens a dialog prompt for selecting a body to follow. Adjusts the camera
     * and simulation speed accordingly.
     * </p>
     */
    public void startFollow() {
        if (timeTravel.isTimeTravelMode() || sim.bodies.isEmpty()) return;

        FollowPrompt followDialog = new FollowPrompt();
        FollowPrompt.FollowResult result = followDialog.show(getMainStage(), sim.bodies, bodyFollowed);

        if (result != null) {
            bodyFollowed = result.body();
            following = true;
            uiManager.getBtnFollow().setSelected(true);

            if (result.zoomToFit()) {
                cam.scale = 10.0 / bodyFollowed.radius;
            }
            sim.speed = Math.min(sim.speed, 30.0 / bodyFollowed.getSpeed() / cam.scale);
        } else {
            uiManager.getBtnFollow().setSelected(false);
        }
        uiManager.selectButton(uiManager.getBtnFollow(), -1);
    }

    /**
     * Stops following the current body.
     * <p>
     * Resets the camera follow state and UI button selection.
     * </p>
     */
    public void stopFollow() {
        following = false;
        cam.clearFollowTarget();
        uiManager.getBtnFollow().setSelected(false);
        uiManager.selectButton(uiManager.getBtnFollow(), -1);
        bodyFollowed = null;
    }

    /**
     * Main entry point of the application.
     *
     * @param args command-line arguments passed to the program
     */
    public static void main(String[] args) {
        launch(args);
    }
}
