package com.example.space.prompts;

import com.example.space.essentials.Body;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A dialog prompt for modifying the properties of an existing celestial body.
 * Allows editing of mass, radius, position, and velocity.
 */
public class ModifyBodyPrompt {
    /**
     * Represents the modified properties of a celestial body.
     *
     * @param mass   the new mass
     * @param radius the new radius
     * @param xPos   the new x position
     * @param yPos   the new y position
     * @param xVel   the new x velocity
     * @param yVel   the new y velocity
     */
    public record BodyProperties(double mass, double radius, double xPos, double yPos, double xVel, double yVel) { }

    /**
     * Displays the modify body prompt dialog.
     *
     * @param owner the owner stage (parent window)
     * @param body  the body to modify (pre-populates fields with current values)
     * @return a {@link BodyProperties} object containing the modified values, or null if canceled
     */
    public BodyProperties show(Stage owner, Body body) {
        Stage dialog = new Stage();
        dialog.setResizable(false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Modify Body Properties");

        String bgDark = "-fx-background-color: #2b2b2b;";
        String fieldDark = "-fx-background-color: #3c3f41; -fx-text-fill: #ffffff; -fx-prompt-text-fill: #bbbbbb;";

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField massField = new TextField(Double.toString(body.mass));
        TextField radsField = new TextField(Double.toString(body.radius));
        TextField xPosField = new TextField(Double.toString(body.pos.x));
        TextField yPosField = new TextField(Double.toString(body.pos.y));
        TextField xVelField = new TextField(Double.toString(body.vel.x));
        TextField yVelField = new TextField(Double.toString(body.vel.y));

        massField.setStyle(fieldDark);
        radsField.setStyle(fieldDark);
        xPosField.setStyle(fieldDark);
        yPosField.setStyle(fieldDark);
        xVelField.setStyle(fieldDark);
        yVelField.setStyle(fieldDark);

        grid.add(createLabel("Mass:"), 0, 0);
        grid.add(massField, 1, 0);
        grid.add(createLabel("Radius:"), 0, 1);
        grid.add(radsField, 1, 1);
        grid.add(createLabel("X Position:"), 0, 2);
        grid.add(xPosField, 1, 2);
        grid.add(createLabel("Y Position:"), 0, 3);
        grid.add(yPosField, 1, 3);
        grid.add(createLabel("X Velocity:"), 0, 4);
        grid.add(xVelField, 1, 4);
        grid.add(createLabel("Y Velocity:"), 0, 5);
        grid.add(yVelField, 1, 5);

        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #a94442; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox buttons = new HBox(10, confirmButton, cancelButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        final BodyProperties[] result = new BodyProperties[1];

        confirmButton.setOnAction(e -> {
            try {
                result[0] = new BodyProperties(
                        Double.parseDouble(massField.getText()),
                        Double.parseDouble(radsField.getText()),
                        Double.parseDouble(xPosField.getText()),
                        Double.parseDouble(yPosField.getText()),
                        Double.parseDouble(xVelField.getText()),
                        Double.parseDouble(yVelField.getText())
                );
                dialog.close();
            } catch (NumberFormatException ex) {
                showErrorAlert();
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        VBox layout = new VBox(15, grid, buttons);
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPadding(new Insets(20));
        layout.setStyle(bgDark);

        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.sizeToScene();

        if (owner.isShowing()) {
            dialog.setX(owner.getX() + owner.getWidth() / 2 - 200);
            dialog.setY(owner.getY() + owner.getHeight() / 2 - 150);
        }

        dialog.showAndWait();
        return result[0];
    }

    /**
     * Creates a styled label for the prompt.
     */
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #f0f0f0;");
        return label;
    }

    /**
     * Shows an error alert when invalid input is detected.
     */
    private void showErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText("Please enter valid numbers for all fields.");
        alert.showAndWait();
    }
}