package com.example.space.Managers;

import com.example.space.App;
import com.example.space.Essentials.Camera;
import com.example.space.Essentials.Simulation;
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

    public UIManager(App main, Stage stage, Simulation sim, Camera cam, TimeTravelManager timeTravel) {
        this.main = main;
        this.mainStage = stage;
        this.sim = sim;
        this.cam = cam;
        this.timeTravel = timeTravel;
    }

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

        main.getMainStage().widthProperty().addListener((obs, oldVal, newVal) -> {
            windowWidth.set(newVal.doubleValue());
            cam.windowWidth = windowWidth.getValue();
        });
        main.getMainStage().heightProperty().addListener((obs, oldVal, newVal) -> {
            windowHeight.set(newVal.doubleValue());
            cam.windowHeight = windowHeight.getValue();
        });

        cam.toolBarOffset = toolbar.getHeight();

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

    private void initializeButtons() {
        btnStop = new Button("⏸");
        btnMove = new ToggleButton("🖱");
        btnTimeTravel = new ToggleButton("⏳");
        btnSlowDown = new Button("⏪");
        btnSpeedUp = new Button("⏩");
        btnCenter = new Button("🎯");
        btnFollow = new ToggleButton("📌");
        btnFullScreen = new ToggleButton("⛶");
        btnGrid = new ToggleButton("⌗");
        btnNames = new ToggleButton("aA");
        btnDelete = new Button("🗑");
        btnInfoText = new ToggleButton("i");
        btnHelp = new Button("?");
        btnBodiesInfo = new ToggleButton("⚪");

        for (ButtonBase b : List.of(btnStop, btnMove, btnTimeTravel, btnSlowDown, btnSpeedUp,
                btnCenter, btnFollow, btnFullScreen, btnGrid, btnNames, btnDelete, btnInfoText,
                btnHelp, btnBodiesInfo)) {
            b.setFocusTraversable(false);
        }

        setupButtonActions();
    }
    private void setupButtonActions() {
        btnStop.setOnAction(e -> {
            flashButton(btnStop);
            sim.setRunning(!sim.isRunning());
            sim.setPrevRunning(sim.isRunning());
            btnStop.setText(sim.isRunning() ? "⏸" : "▶");
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
            btnHelp.setText(main.showHelpText ? "📖" : "?");
            if (main.showHelpText) btnHelp.setStyle(oldBtnStyle + "; -fx-background-color: #666;");
            else btnHelp.setStyle(oldBtnStyle);
        });

        btnBodiesInfo.setOnAction(e -> {
            selectButton(btnBodiesInfo, -1);
            main.showBodiesInfo = !main.showBodiesInfo;
        });
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10, btnStop, btnMove, btnTimeTravel, btnSlowDown, btnSpeedUp,
                btnCenter, btnFollow, btnFullScreen, btnGrid, btnNames, btnDelete, btnInfoText,
                btnHelp, btnBodiesInfo);
        toolbar.setStyle("-fx-background-color: #111; -fx-padding: 8;");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        for (Node node : toolbar.getChildren()) if (node instanceof ButtonBase b) b.setStyle(oldBtnStyle);
        return toolbar;
    }

    public void flashButton(Button button) {
        button.setStyle(oldBtnStyle + "; -fx-background-color: #666;");
        PauseTransition pause = new PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> button.setStyle(oldBtnStyle));
        pause.play();
    }
    public void selectButton(ToggleButton btn, int selection) {
        if (selection == 0) btn.setSelected(!btn.isSelected());
        if (btn.isSelected()) btn.setStyle(oldBtnStyle + "; -fx-background-color: #666;");
        else btn.setStyle(oldBtnStyle);
    }

    public ToggleButton getBtnFollow() {
        return btnFollow;
    }

    public void toggleFullScreen() {
        selectButton(btnFullScreen, 0);
        mainStage.setFullScreen(!mainStage.isFullScreen());
    }
    public void toggleBodiesInfo() {
        selectButton(btnBodiesInfo, 0);
        main.showBodiesInfo = !main.showBodiesInfo;
    }
    public void centerCamera() {
        flashButton(btnCenter);
        btnFollow.setSelected(false);
        main.stopFollow();
        cam.x = 0;
        cam.y = 0;
        cam.scale = 1;
        cam.reset();
    }
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
    public void setFollowSelected(boolean selected) {
        btnFollow.setSelected(selected);
    }
    public void toggleGrid() {
        selectButton(btnGrid, 0);
        main.showGrid = !main.showGrid;
    }
    public void toggleHelpText() {
        main.showHelpText = !main.showHelpText;
        btnHelp.setText(main.showHelpText ? "📖" : "?");
        if (main.showHelpText) btnHelp.setStyle(oldBtnStyle + "; -fx-background-color: #666;");
        else btnHelp.setStyle(oldBtnStyle);
    }
    public void toggleInfoText() {
        main.showInfoText = !main.showInfoText;
        btnInfoText.setSelected(main.showInfoText);
        selectButton(btnInfoText, -1);
    }
    public void toggleMoveOnlyMode() {
        main.moveOnlyMode = !main.moveOnlyMode;
        btnMove.setSelected(main.moveOnlyMode);
        selectButton(btnMove, -1);
    }
    public void toggleNames() {
        main.showNames = !main.showNames;
        btnNames.setSelected(main.showNames);
        selectButton(btnNames, -1);
    }
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
    public void toggleSimulationRunning() {
        if (timeTravel.isTimeTravelMode()) {
            timeTravel.exitTimeTravelMode();
            selectButton(btnTimeTravel, 0);
        }
        sim.setRunning(!sim.isRunning());
        sim.setPrevRunning(sim.isRunning());
        btnStop.setText(sim.isRunning() ? "⏸" : "▶");
        flashButton(btnStop);
    }
    public void increaseSimSpeed() {
        flashButton(btnSpeedUp);
        main.sim.speed *= 1.5;
    }
    public void decreaseSimSpeed() {
        flashButton(btnSlowDown);
        main.sim.speed /= 1.5;
    }
}
