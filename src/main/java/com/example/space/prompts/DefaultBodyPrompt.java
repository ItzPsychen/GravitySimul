package com.example.space.prompts;

import com.example.space.essentials.Body;
import com.example.space.essentials.Vector2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * A dialog prompt that allows the user to manage a list of body templates.
 *
 * Templates define reusable body characteristics (mass, radius) that can be
 * selected when creating new bodies in the simulation. One template is always
 * marked as the default and used automatically when new bodies are added.
 *
 * The prompt allows users to:
 * <ul>
 *   <li>Add new templates</li>
 *   <li>Edit existing templates</li>
 *   <li>Remove templates (ensuring at least one remains)</li>
 *   <li>Set a template as the default</li>
 * </ul>
 */
public class DefaultBodyPrompt {

    /**
     * Represents the result of the default body prompt.
     *
     * @param bodies     list of all templates
     * @param defaultBody the template chosen as default
     */
    public record DefaultBodyResult(List<Body> bodies, Body defaultBody) {}

    /** List of all currently available body templates. */
    private final List<Body> templates = new ArrayList<>();

    /** The template marked as default. */
    private Body defaultBody;

    /**
     * Constructs a new {@code DefaultBodyPrompt}.
     *
     * @param defaultBody the initial default body template; it is always
     *                    added to the templates list
     */
    public DefaultBodyPrompt(Body defaultBody) {
        this.defaultBody = defaultBody;
        templates.add(defaultBody);
    }

    /**
     * Displays the prompt dialog for managing body templates.
     *
     * @param owner the owner stage (parent window)
     * @return a {@link DefaultBodyResult} with updated templates and default, or null if canceled
     */
    public DefaultBodyResult show(Stage owner) {
        Stage dialog = new Stage();
        dialog.setResizable(false);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Body Templates");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Manage Body Templates");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 14px; -fx-font-weight: bold;");

        ListView<Body> listView = createListView();
        listView.getItems().addAll(templates);

        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button removeBtn = new Button("Remove");
        Button setDefaultBtn = new Button("Set Default");

        Button saveBtn = new Button("Save");
        Button cancelBtn = new Button("Cancel");

        styleActionBtn(addBtn, "#2196f3");
        styleActionBtn(editBtn, "#ffc107");
        styleActionBtn(removeBtn, "#a94442");
        styleActionBtn(setDefaultBtn, "#9c27b0");

        saveBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelBtn.setStyle("-fx-background-color: #a94442; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox actions = new HBox(10, addBtn, editBtn, removeBtn, setDefaultBtn);
        actions.setAlignment(Pos.CENTER);

        HBox bottom = new HBox(10, saveBtn, cancelBtn);
        bottom.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, listView, actions, bottom);

        final DefaultBodyResult[] result = new DefaultBodyResult[1];

        // --- Button logic ---
        addBtn.setOnAction(e -> {
            Body newBody = showEditDialog(dialog,null);
            if (newBody != null) {
                templates.add(newBody);
                listView.getItems().add(newBody);
            }
        });

        editBtn.setOnAction(e -> {
            Body selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Body edited = showEditDialog(dialog, selected);
                if (edited != null) {
                    int idx = templates.indexOf(selected);
                    templates.set(idx, edited);
                    listView.getItems().set(idx, edited);
                    if (selected == defaultBody) defaultBody = edited;
                }
            }
        });

        removeBtn.setOnAction(e -> {
            Body selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null && templates.size() > 1) {
                templates.remove(selected);
                listView.getItems().remove(selected);

                if (selected == defaultBody) {
                    defaultBody = templates.get(0);
                }
            }
        });

        setDefaultBtn.setOnAction(e -> {
            Body selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                defaultBody = selected;
                listView.refresh();
            }
        });

        saveBtn.setOnAction(e -> {
            result[0] = new DefaultBodyResult(new ArrayList<>(templates), defaultBody);
            dialog.close();
        });

        cancelBtn.setOnAction(e -> dialog.close());

        Scene scene = new Scene(root, 380, 400);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) saveBtn.fire();
            if (e.getCode() == KeyCode.ESCAPE) cancelBtn.fire();
        });

        dialog.setScene(scene);

        if (owner.isShowing()) {
            dialog.setX(owner.getX() + owner.getWidth()/2 - 190);
            dialog.setY(owner.getY() + owner.getHeight()/2 - 200);
        }

        dialog.showAndWait();
        return result[0];
    }

    /**
     * Styles a button with a given background color and consistent white text.
     *
     * @param btn   the button to style
     * @param color the background color in CSS hex or named format
     */
    private static void styleActionBtn(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    /**
     * Creates and configures a {@link ListView} for displaying body templates.
     * <p>
     * Each cell shows a colored circle (based on mass), along with the mass
     * and radius values. The default template is marked with a {@code [DEFAULT]} tag.
     * </p>
     *
     * @return a configured {@code ListView} for body templates
     */
    private ListView<Body> createListView() {
        ListView<Body> listView = new ListView<>();
        listView.setStyle("-fx-control-inner-background: #2b2b2b; -fx-background-color: #2b2b2b;");

        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Body> call(ListView<Body> param) {
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

                            String labelText = String.format("Mass: %.2f  Radius: %.2f", body.mass, body.radius);
                            if (body == defaultBody) {
                                labelText += "  [DEFAULT]";
                            }

                            Label label = new Label(labelText);
                            label.setStyle("-fx-text-fill: white;");

                            cell.getChildren().addAll(circle, label);
                            setGraphic(cell);
                        }
                    }
                };
            }
        });

        return listView;
    }

    /**
     * Displays a small dialog for editing or creating a body template.
     * <p>
     * The dialog allows the user to set mass and radius values. If confirmed,
     * a new {@link Body} instance is returned; otherwise {@code null}.
     * </p>
     *
     * @param owner the parent stage (usually the main prompt dialog)
     * @param body  the existing body template to edit, or {@code null} to create a new one
     * @return the created/edited {@code Body}, or {@code null} if canceled
     */
    private Body showEditDialog(Stage owner, Body body) {
        Stage dialog = new Stage();
        dialog.setResizable(false);
        dialog.initOwner(owner);  // 👈 make the prompt the owner
        dialog.initModality(Modality.WINDOW_MODAL); // 👈 blocks only the prompt
        dialog.setTitle(body == null ? "Add Body Template" : "Edit Body Template");

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #2b2b2b;");

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);

        TextField massField = new TextField(body == null ? "" : String.valueOf(body.mass));
        TextField radiusField = new TextField(body == null ? "" : String.valueOf(body.radius));

        grid.addRow(0, new Label("Mass:"), massField);
        grid.addRow(1, new Label("Radius:"), radiusField);

        grid.getChildren().stream()
                .filter(n -> n instanceof Label)
                .forEach(n -> n.setStyle("-fx-text-fill: #f0f0f0;"));

        Button okBtn = new Button("OK");
        Button cancelBtn = new Button("Cancel");
        styleActionBtn(okBtn, "#4caf50");
        styleActionBtn(cancelBtn, "#a94442");

        HBox buttons = new HBox(10, okBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(grid, buttons);

        Scene scene = new Scene(root, 280, 180);

        final Body[] result = new Body[1];

        okBtn.setOnAction(e -> {
            try {
                double mass = Double.parseDouble(massField.getText());
                double radius = Double.parseDouble(radiusField.getText());
                result[0] = new Body("template", mass, new Vector2D(0, 0), new Vector2D(0, 0), radius);
                dialog.close();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid number format").showAndWait();
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        dialog.setScene(scene);
        dialog.showAndWait();

        return result[0];
    }
}
