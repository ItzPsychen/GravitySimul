package com.example.space.prompts;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

/**
 * A dialog prompt for setting the properties of a new celestial body.
 * Allows configuration of mass, radius, and initial velocity.
 */
public class MassPrompt {
    /**
     * Represents the properties of a celestial body.
     *
     * @param mass   the mass of the body
     * @param radius the radius of the body
     * @param velX   the initial x velocity
     * @param velY   the initial y velocity
     */
    public record BodyProperties(double mass, double radius, double velX, double velY) { }

    /**
     * Displays the mass prompt dialog.
     *
     * @param owner the owner stage (parent window)
     * @return a {@link BodyProperties} object containing the user's input, or null if canceled
     */
    public BodyProperties prompt(Stage owner) {
        Stage dialog = new Stage();
        dialog.setResizable(false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Set New Body Properties");

        String bgDark     = "-fx-background-color: #2b2b2b;";
        String labelBright= "-fx-text-fill: #f0f0f0;";
        String fieldDark  = "-fx-background-color: #3c3f41; -fx-text-fill: #ffffff; -fx-prompt-text-fill: #bbbbbb;";

        Label massLabel  = new Label("Mass:");
        massLabel .setStyle(labelBright);
        TextField massField  = new TextField("100");
        massField .setStyle(fieldDark);
        Slider    massSlider = new Slider(0, 1_000_000, 100);
        styleSlider(massSlider, bgDark, labelBright);

        Label radiusLabel  = new Label("Radius:");
        radiusLabel .setStyle(labelBright);
        TextField radiusField  = new TextField("5");
        radiusField .setStyle(fieldDark);
        Slider radiusSlider = new Slider(0, 1_000, 5);
        styleSlider(radiusSlider, bgDark, labelBright);

        NumberStringConverter oneDecimal = new NumberStringConverter("#.0");
        Bindings.bindBidirectional(massField.textProperty(),   massSlider.valueProperty(),   oneDecimal);
        Bindings.bindBidirectional(radiusField.textProperty(), radiusSlider.valueProperty(), oneDecimal);

        Label velocityLabel = new Label("Initial Velocity (X, Y):");
        velocityLabel.setStyle(labelBright);
        TextField velXField = new TextField("0");
        TextField velYField = new TextField("0");
        velXField.setStyle(fieldDark);
        velYField.setStyle(fieldDark);

        Button confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        Button cancelButton  = new Button("Cancel");
        cancelButton .setStyle("-fx-background-color: #a94442; -fx-text-fill: white; -fx-font-weight: bold;");

        final BodyProperties[] result = new BodyProperties[1];

        confirmButton.setOnAction(e -> {
            double mass = parseOrDefault(massField.getText(), massSlider.getValue());
            double radius = Math.max(parseOrDefault(radiusField.getText(), radiusSlider.getValue()), 0.1);
            double velX = parseOrDefault(velXField.getText(), 0);
            double velY = parseOrDefault(velYField.getText(), 0);
            result[0] = new BodyProperties(mass, radius, velX, velY);
            dialog.close();
        });
        cancelButton.setOnAction(e -> {
            result[0] = null;
            dialog.close();
        });

        VBox layout = new VBox(15,
                labeledRow(massLabel, massField, massSlider),
                labeledRow(radiusLabel, radiusField, radiusSlider),
                velocityRow(velocityLabel, velXField, velYField),
                new HBox(15, confirmButton, cancelButton)
        );
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPadding(new Insets(20));
        layout.setStyle(bgDark);

        layout.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                confirmButton.fire();
            }
        });

        Scene scene = new Scene(layout);
        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.setX(owner.getX() + owner.getWidth() / 2 - 200);
        dialog.setY(owner.getY() + owner.getHeight() / 2 - 150);
        dialog.showAndWait();

        return result[0];
    }

    /**
     * Parses a string to a double, returning a default value if parsing fails.
     */
    private static double parseOrDefault(String text, double def) {
        try {
            return Double.parseDouble(text.replace(",", "."));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Creates a labeled row with a text field and slider.
     */
    private static HBox labeledRow(Label label, TextField field, Slider slider) {
        label.setMinWidth(60);
        field.setPrefWidth(80);
        HBox.setHgrow(slider, Priority.ALWAYS);

        HBox row = new HBox(10, label, field, slider);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Creates a row for velocity input fields.
     */
    private static HBox velocityRow(Label label, TextField velX, TextField velY) {
        HBox row = new HBox(10, label, new Label("X:"), velX, new Label("Y:"), velY);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().stream()
                .filter(n -> n instanceof Label)
                .forEach(n -> n.setStyle("-fx-text-fill: #f0f0f0;"));
        return row;
    }

    /**
     * Styles a slider with consistent appearance.
     */
    private static void styleSlider(Slider slider, String bgStyle, String labelStyle) {
        slider.setMajorTickUnit((slider.getMax() - slider.getMin()) / 5);
        slider.setMinorTickCount(4);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setStyle(bgStyle + labelStyle);
    }
}