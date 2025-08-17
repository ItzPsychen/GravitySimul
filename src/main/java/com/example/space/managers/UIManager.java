package com.example.space.managers;

import com.example.space.App;
import com.example.space.essentials.Camera;
import com.example.space.essentials.Simulation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Manages the UI components, including buttons, toolbars, and transitions.
 */
@SuppressWarnings("UnnecessaryUnicodeEscape")
public class UIManager {
    private final App main;
    private final Stage mainStage;
    private final Simulation sim;
    private final Camera cam;
    private final TimeTravelManager timeTravel;

    private Button btnStop;
    private ToggleButton btnMove;
    private ToggleButton btnTimeTravel;
    private Button btnSlowDown;
    private Button btnSpeedUp;
    private Button btnCenter;
    private ToggleButton btnFollow;
    private ToggleButton btnFullScreen;
    private ToggleButton btnGrid;
    private ToggleButton btnNames;
    private Button btnDelete;
    private ToggleButton btnInfoText;
    private Button btnHelp;
    private ToggleButton btnBodiesInfo;

    private final String oldBtnStyle = "-fx-background-color: #333; -fx-text-fill: white; -fx-font-size: 14px;";

    /**
     * Constructs a new UIManager.
     *
     * @param main        the main application instance
     * @param stage       the primary stage
     * @param sim         the simulation
     * @param cam         the camera
     * @param timeTravel  the time travel manager
     */
    public UIManager(App main, Stage stage, Simulation sim, Camera cam, TimeTravelManager timeTravel) {
        this.main = main;
        this.mainStage = stage;
        this.sim = sim;
        this.cam = cam;
        this.timeTravel = timeTravel;
    }

    /**
     * Sets up the UI components.
     *
     * @param windowWidth   the window width property
     * @param windowHeight  the window height property
     * @return the configured scene
     */
    public Scene setupUI(DoubleProperty windowWidth, DoubleProperty windowHeight) {
        main.getMainStage().setMinWidth(600);
        main.getMainStage().setMinHeight(700);

        Canvas canvas = new Canvas(windowWidth.getValue(), windowHeight.getValue());

        initializeButtons();
        HBox toolbar = createToolbar();

        for (ToggleButton b : List.of(btnFullScreen, btnGrid, btnInfoText, btnBodiesInfo)) {
            b.setSelected(true);
            selectButton(b, -1);
        }

        VBox layout = new VBox();
        layout.setAlignment(Pos.TOP_LEFT);
        layout.getChildren().addAll(toolbar, canvas);

        canvas.widthProperty().bind(main.getMainStage().widthProperty());
        canvas.heightProperty().bind(main.getMainStage().heightProperty().subtract(toolbar.heightProperty()));

        StackPane transitionOverlay = new StackPane();
        transitionOverlay.setStyle("-fx-background-color: #000;");
        transitionOverlay.setOpacity(1.0);

        StackPane root = new StackPane(layout, transitionOverlay);
        StackPane.setAlignment(layout, Pos.TOP_LEFT);

        Scene mainScene = new Scene(root, Color.BLACK);

        main.getMainStage().widthProperty().addListener((obs, oldVal, newVal) -> windowWidth.set(newVal.doubleValue()));
        main.getMainStage().heightProperty().addListener((obs, oldVal, newVal) -> windowHeight.set(newVal.doubleValue()));

        Platform.runLater(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), transitionOverlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> root.getChildren().remove(transitionOverlay));

            PauseTransition pause = new PauseTransition(Duration.millis(50));
            pause.setOnFinished(e -> fadeOut.play());
            pause.play();
        });

        return mainScene;
    }


    /**
     * Gets the follow toggle button.
     * @return the follow button instance
     */
    public ToggleButton getBtnFollow() {
        return btnFollow;
    }

    /**
     * Initializes all UI buttons with their respective icons and properties.
     * Sets focus traversal to false for all buttons.
     */
    private void initializeButtons() {
        btnStop = new Button("\u23F8");
        btnMove = new ToggleButton("\uD83D\uDDB1");
        btnTimeTravel = new ToggleButton("\u231B");
        btnSlowDown = new Button("\u23EA");
        btnSpeedUp = new Button("\u23E9");
        btnCenter = new Button("\uD83C\uDFAF");
        btnFollow = new ToggleButton("\uD83D\uDCCC");
        btnFullScreen = new ToggleButton("\u26F6");
        btnGrid = new ToggleButton("\u0023");
        btnNames = new ToggleButton("Aa");
        btnDelete = new Button("\uD83D\uDDD1");
        btnInfoText = new ToggleButton("i");
        btnHelp = new Button("?");
        btnBodiesInfo = new ToggleButton("\u25CB");

        for (ButtonBase b : List.of(btnStop, btnMove, btnTimeTravel, btnSlowDown, btnSpeedUp,
                btnCenter, btnFollow, btnFullScreen, btnGrid, btnNames, btnDelete, btnInfoText,
                btnHelp, btnBodiesInfo)) {
            b.setFocusTraversable(false);
        }

        setupButtonActions();
    }

    /**
     * Configures action handlers for all buttons.
     */
    private void setupButtonActions() {
        btnStop.setOnAction(e -> {
            flashButton(btnStop);
            sim.setRunning(!sim.isRunning());
            sim.setPrevRunning(sim.isRunning());
            btnStop.setText(sim.isRunning() ? "\u23F8" : "\u25B6");
        });

        btnMove.setOnAction(e -> {
            main.moveOnlyMode = !main.moveOnlyMode;
            selectButton(btnMove, -1);
        });

        btnTimeTravel.setOnAction(e -> {
            if (main.following) main.stopFollow();
            selectButton(btnTimeTravel, -1);
            if (timeTravel.isTimeTravelMode()) {
                timeTravel.exitTimeTravelMode();
            } else {
                timeTravel.setTimeTravelMode(true);
                sim.setRunning(false);
                sim.setPrevRunning(false);
                timeTravel.saveSimulationState();
            }
        });

        btnSpeedUp.setOnAction(e -> {
            flashButton(btnSpeedUp);
            main.sim.speed *= 1.5;
        });

        btnSlowDown.setOnAction(e -> {
            flashButton(btnSlowDown);
            main.sim.speed /= 1.5;
        });

        btnCenter.setOnAction(e -> {
            flashButton(btnCenter);
            btnFollow.setSelected(false);
            main.stopFollow();
            cam.x = 0;
            cam.y = 0;
            cam.scale = 1;
            cam.reset();
        });

        btnFollow.setOnAction(e -> {
            if (main.following) main.stopFollow();
            else main.startFollow();
        });

        btnFullScreen.setOnAction(e -> {
            mainStage.setFullScreen(!mainStage.isFullScreen());
            selectButton(btnFullScreen, -1);
        });

        btnGrid.setOnAction(e -> {
            selectButton(btnGrid, -1);
            main.showGrid = !main.showGrid;
        });

        btnNames.setOnAction(e -> {
            main.showNames = !main.showNames;
            btnNames.setSelected(main.showNames);
            selectButton(btnNames, -1);
        });

        btnDelete.setOnAction(e -> {
            flashButton(btnDelete);
            btnTimeTravel.setStyle(oldBtnStyle);
            sim.bodies.clear();
            timeTravel.getSimulationHistory().clear();
            if (btnTimeTravel.isSelected()) {
                timeTravel.setTimeTravelMode(false);
                selectButton(btnTimeTravel, 0);
            }
            if (btnFollow.isSelected()) {
                main.stopFollow();
            }
            btnFollow.setStyle(oldBtnStyle);
            main.sim.speed = 1.0;
            cam.reset();
        });

        btnInfoText.setOnAction(e -> {
            main.showInfoText = !main.showInfoText;
            selectButton(btnInfoText, -1);
        });

        btnHelp.setOnAction(e -> {
            main.showHelpText = !main.showHelpText;
            btnHelp.setText(main.showHelpText ? "\uD83D\uDCD6" : "?");
            if (main.showHelpText) btnHelp.setStyle(oldBtnStyle + "; -fx-background-color: #666;");
            else btnHelp.setStyle(oldBtnStyle);
        });

        btnBodiesInfo.setOnAction(e -> {
            selectButton(btnBodiesInfo, -1);
            main.showBodiesInfo = !main.showBodiesInfo;
        });
    }

    /**
     * Creates and styles the main toolbar containing all control buttons.
     * @return the configured HBox toolbar
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10, btnStop, btnMove, btnTimeTravel, btnSlowDown, btnSpeedUp,
                btnCenter, btnFollow, btnFullScreen, btnGrid, btnNames, btnDelete, btnInfoText,
                btnHelp, btnBodiesInfo);
        toolbar.setStyle("-fx-background-color: #111; -fx-padding: 8;");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        for (Node node : toolbar.getChildren()) if (node instanceof ButtonBase b) b.setStyle(oldBtnStyle);
        return toolbar;
    }

    /**
     * Visually flashes a button by temporarily changing its background color.
     * @param button the button to flash
     */
    public void flashButton(Button button) {
        button.setStyle(oldBtnStyle + "; -fx-background-color: #666;");
        PauseTransition pause = new PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> button.setStyle(oldBtnStyle));
        pause.play();
    }

    /**
     * Updates the visual selection state of a toggle button.
     * @param btn the toggle button to update
     * @param selection the selection mode (-1 for no change, 0 to toggle)
     */
    public void selectButton(ToggleButton btn, int selection) {
        if (selection == 0) btn.setSelected(!btn.isSelected());
        if (btn.isSelected()) btn.setStyle(oldBtnStyle + "; -fx-background-color: #666;");
        else btn.setStyle(oldBtnStyle);
    }

    // ----------------- Button Helper Methods -----------------

    /**
     * Toggles fullscreen mode and updates button state.
     */
    public void toggleFullScreen() {
        selectButton(btnFullScreen, 0);
        mainStage.setFullScreen(!mainStage.isFullScreen());
    }

    /**
     * Toggles bodies information display and updates button state.
     */
    public void toggleBodiesInfo() {
        selectButton(btnBodiesInfo, 0);
        main.showBodiesInfo = !main.showBodiesInfo;
    }

    /**
     * Centers the camera view and resets follow mode.
     */
    public void centerCamera() {
        flashButton(btnCenter);
        btnFollow.setSelected(false);
        main.stopFollow();
        cam.x = 0;
        cam.y = 0;
        cam.scale = 1;
        cam.reset();
    }

    /**
     * Clears all bodies from the simulation and resets state.
     */
    public void clearAllBodies() {
        flashButton(btnDelete);
        btnTimeTravel.setStyle(oldBtnStyle);
        sim.bodies.clear();
        timeTravel.getSimulationHistory().clear();
        if (btnTimeTravel.isSelected()) {
            timeTravel.setTimeTravelMode(false);
            selectButton(btnTimeTravel, 0);
        }
        if (btnFollow.isSelected()) main.stopFollow();
        btnFollow.setStyle(oldBtnStyle);
        main.sim.speed = 1.0;
        cam.reset();
    }

    /**
     * Sets the follow button's selected state.
     * @param selected true to select, false to deselect
     */
    public void setFollowSelected(boolean selected) {
        btnFollow.setSelected(selected);
    }

    /**
     * Toggles grid visibility and updates button state.
     */
    public void toggleGrid() {
        selectButton(btnGrid, 0);
        main.showGrid = !main.showGrid;
    }

    /**
     * Toggles help text display and updates button state.
     */
    public void toggleHelpText() {
        main.showHelpText = !main.showHelpText;
        btnHelp.setText(main.showHelpText ? "\uD83D\uDCD6" : "?");
        if (main.showHelpText) btnHelp.setStyle(oldBtnStyle + "; -fx-background-color: #666;");
        else btnHelp.setStyle(oldBtnStyle);
    }

    /**
     * Toggles informational text display and updates button state.
     */
    public void toggleInfoText() {
        main.showInfoText = !main.showInfoText;
        btnInfoText.setSelected(main.showInfoText);
        selectButton(btnInfoText, -1);
    }

    /**
     * Toggles move-only mode and updates button state.
     */
    public void toggleMoveOnlyMode() {
        main.moveOnlyMode = !main.moveOnlyMode;
        btnMove.setSelected(main.moveOnlyMode);
        selectButton(btnMove, -1);
    }

    /**
     * Toggles body name labels and updates button state.
     */
    public void toggleNames() {
        main.showNames = !main.showNames;
        btnNames.setSelected(main.showNames);
        selectButton(btnNames, -1);
    }

    /**
     * Toggles time travel mode and updates simulation state.
     */
    public void toggleTimeTravel() {
        selectButton(btnTimeTravel, 0);
        if (timeTravel.isTimeTravelMode()) {
            timeTravel.exitTimeTravelMode();
            sim.setRunning(sim.isPrevRunning());
        } else {
            if (main.following) {
                btnFollow.setSelected(false);
                main.stopFollow();
            }
            timeTravel.setTimeTravelMode(true);
            sim.setRunning(false);
            timeTravel.saveSimulationState();
        }
    }

    /**
     * Toggles simulation running state and updates button display.
     */
    public void toggleSimulationRunning() {
        if (timeTravel.isTimeTravelMode()) {
            timeTravel.exitTimeTravelMode();
            selectButton(btnTimeTravel, 0);
        }
        sim.setRunning(!sim.isRunning());
        sim.setPrevRunning(sim.isRunning());
        btnStop.setText(sim.isRunning() ? "\u23F8" : "\u25B6");
        flashButton(btnStop);
    }

    /**
     * Increases simulation speed by 50%.
     */
    public void increaseSimSpeed() {
        flashButton(btnSpeedUp);
        main.sim.speed *= 1.5;
    }

    /**
     * Decreases simulation speed by 50%.
     */
    public void decreaseSimSpeed() {
        flashButton(btnSlowDown);
        main.sim.speed /= 1.5;
    }
}
