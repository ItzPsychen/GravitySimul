package com.example.space.prompts;

import com.example.space.essentials.Body;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.*;
import javafx.util.Callback;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.List;

/**
 * A dialog prompt that allows the user to select a celestial body to follow in the simulation.
 * The prompt displays a list of bodies with their properties and provides an option to zoom to fit.
 */
public class FollowPrompt {
    /**
     * Represents the result of the follow prompt, containing the selected body and zoom preference.
     *
     * @param body      the selected body to follow
     * @param zoomToFit whether to zoom to fit the selected body
     */
    public record FollowResult(Body body, boolean zoomToFit) { }

    /**
     * Displays the follow prompt dialog.
     *
     * @param owner           the owner stage (parent window)
     * @param bodies          the list of bodies available for selection
     * @param currentFollowed the currently followed body (pre-selected if not null)
     * @return a {@link FollowResult} containing the user's selection, or null if canceled
     */
    public FollowResult show(Stage owner, List<Body> bodies, Body currentFollowed) {
        Stage dialog = new Stage();
        dialog.setResizable(false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Follow Body");

        String bgDark = "-fx-background-color: #2b2b2b;";
        String labelBright = "-fx-text-fill: #f0f0f0;";

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle(bgDark);

        ListView<Body> bodyList = getBodyListView();
        bodyList.getItems().addAll(bodies);

        if (currentFollowed != null) bodyList.getSelectionModel().select(currentFollowed);
        else if (!bodies.isEmpty()) bodyList.getSelectionModel().selectFirst();

        CheckBox zoomCheck = new CheckBox("Zoom to Fit");
        zoomCheck.setSelected(true);
        zoomCheck.setStyle(labelBright);

        Button followBtn = new Button("Follow");
        followBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #a94442; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox buttons = new HBox(10, followBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        final FollowResult[] result = new FollowResult[1];

        followBtn.setOnAction(e -> {
            Body selected = bodyList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                result[0] = new FollowResult(selected, zoomCheck.isSelected());
                dialog.close();
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        Label selectLabel = new Label("Select a body to follow");
        selectLabel.setStyle(labelBright);
        root.getChildren().addAll(
                selectLabel,
                bodyList,
                zoomCheck,
                buttons
        );

        Scene scene = new Scene(root, 350, 400);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) followBtn.fire();
            if (e.getCode() == KeyCode.ESCAPE) cancelBtn.fire();
        });

        dialog.setScene(scene);

        if (owner.isShowing()) {
            dialog.setX(owner.getX() + owner.getWidth()/2 - 175);
            dialog.setY(owner.getY() + owner.getHeight()/2 - 200);
        }

        dialog.showAndWait();
        return result[0];
    }

    /**
     * Creates and styles a ListView for displaying celestial bodies.
     *
     * @return a configured ListView for Body objects
     */
    private static ListView<Body> getBodyListView() {
        ListView<Body> bodyList = new ListView<>();
        bodyList.setStyle("-fx-control-inner-background: #2b2b2b; " +
                "-fx-background-color: #2b2b2b; " +
                "-fx-text-fill: white; " +
                "-fx-selection-bar: #3d5a80;");

        bodyList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Body> call(ListView<Body> listView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Body body, boolean empty) {
                        super.updateItem(body, empty);

                        if (empty || body == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            HBox cell = new HBox(10);
                            cell.setAlignment(Pos.CENTER_LEFT);

                            Circle circle = new Circle(6);
                            circle.setFill(Color.hsb(body.mass % 360, 0.8, 0.9));

                            Label label = new Label(String.format("%s\t(Mass: %.2f  Speed: %.2f)", body.id, body.mass, body.getSpeed()));
                            label.setStyle("-fx-text-fill: white;");

                            cell.getChildren().addAll(circle, label);
                            setGraphic(cell);
                        }
                    }
                };
            }
        });
        return bodyList;
    }
}