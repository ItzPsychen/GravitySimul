package com.example.space;

import com.example.space.Essentials.Body;
import com.example.space.Essentials.Camera;
import com.example.space.Essentials.Simulation;
import com.example.space.Essentials.Vector2D;
import com.example.space.Handlers.GridHandler;
import com.example.space.Handlers.HUDHandler;
import com.example.space.Handlers.InputHandler;
import com.example.space.Managers.RenderManager;
import com.example.space.Managers.TimeTravelManager;
import com.example.space.Managers.UIManager;
import com.example.space.Prompts.FollowPrompt;
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

public class App extends Application {
    private Stage mainStage;
    public Simulation sim = new Simulation();
    public Camera cam = new Camera();

    public DoubleProperty windowWidth = new SimpleDoubleProperty(Screen.getPrimary().getVisualBounds().getWidth());
    public DoubleProperty windowHeight = new SimpleDoubleProperty(Screen.getPrimary().getVisualBounds().getHeight());

    public double defMass = 50.0;

    public boolean moveOnlyMode = false;
    public boolean showNames = false;
    public boolean showHelpText = false;
    public boolean showGrid = true;
    public boolean showInfoText = true;
    public boolean showBodiesInfo = true;

    public boolean quitHeld = false;
    public long escHoldStart = 0;
    public static final long ESC_HOLD_DURATION_NS = 2_000_000_000L;

    public Body bodyFollowed = null;
    public boolean following = false;

    public List<Body> removedBodies;
    public TimeTravelManager timeTravel;
    public GridHandler gridRenderer;
    public HUDHandler hudRenderer;
    public UIManager uiManager;
    public RenderManager renderManager;

    public Stage getMainStage() {
        return mainStage;
    }

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;

        sim.G = 6.67430e-3;
        sim.addBody(new Body("Star", 1e6, new Vector2D(0, 0), new Vector2D(0, 0), 20));

        timeTravel = new TimeTravelManager(sim);
        gridRenderer = new GridHandler();
        hudRenderer = new HUDHandler();
        removedBodies = new ArrayList<>();

        uiManager = new UIManager(this, stage, sim, cam, timeTravel);
        Scene mainScene = uiManager.setupUI(windowWidth, windowHeight);

        renderManager = new RenderManager(this, sim, cam, timeTravel, gridRenderer, hudRenderer);

        Canvas canvas = (Canvas)((VBox)((StackPane)mainScene.getRoot()).getChildren().get(0)).getChildren().get(1);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        stage.setScene(mainScene);
        stage.getScene().setFill(Color.BLACK);
        stage.setFullScreen(true);
        stage.setTitle("Space Gravity Simulator");
        stage.show();

        Platform.runLater(() -> {
            InputHandler inputHandler = new InputHandler(this, sim, cam, uiManager, timeTravel);
            inputHandler.attach(canvas, mainScene);
            renderManager.startMainLoop(canvas, gc);
        });
    }

    public Body findBodyAt(double mouseX, double mouseY, double canvasWidth, double canvasHeight) {
        Vector2D worldPos = cam.screenToWorld(mouseX, mouseY, canvasWidth, canvasHeight);
        for (Body b : sim.bodies) if (b.pos.sub(worldPos).mag() < b.radius) return b;
        return null;
    }

    public void startFollow() {
        if (timeTravel.isTimeTravelMode() || sim.bodies.isEmpty()) return;

        FollowPrompt followDialog = new FollowPrompt();
        FollowPrompt.FollowResult result = followDialog.show(getMainStage(), sim.bodies, bodyFollowed);

        if (result != null) {
            bodyFollowed = result.body();
            following = true;
            uiManager.getBtnFollow().setSelected(true);

            if (result.zoomToFit()) cam.scale = 10.0 / bodyFollowed.radius;
            sim.speed = Math.min(sim.speed, 30.0 / bodyFollowed.getSpeed() / cam.scale);
        } else uiManager.getBtnFollow().setSelected(false);
        uiManager.selectButton(uiManager.getBtnFollow(), -1);
    }
    public void stopFollow() {
        following = false;
        cam.clearFollowTarget();
        uiManager.getBtnFollow().setSelected(false);
        uiManager.selectButton(uiManager.getBtnFollow(), -1);
        bodyFollowed = null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}