package com.example.space.handlers;

import com.example.space.*;
import com.example.space.essentials.Body;
import com.example.space.essentials.Camera;
import com.example.space.essentials.Simulation;
import com.example.space.essentials.Vector2D;
import com.example.space.managers.TimeTravelManager;
import com.example.space.managers.UIManager;
import com.example.space.prompts.MassPrompt;
import com.example.space.prompts.ModifyBodyPrompt;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Handles all user input for the simulation, including mouse and keyboard events.
 * <p>
 * Provides support for:
 * <ul>
 *     <li>Adding/removing/modifying bodies via mouse clicks</li>
 *     <li>Dragging and panning the camera</li>
 *     <li>Zooming in and out</li>
 *     <li>Keyboard shortcuts for simulation controls</li>
 *     <li>Time travel navigation</li>
 * </ul>
 */
public class InputHandler {
    private final App main;
    private final Simulation sim;
    private final Camera cam;
    private final UIManager uiManager;
    private final TimeTravelManager timeTravel;

    /** Stores drag offsets for camera movement. */
    private final Delta dragDelta = new Delta();

    /**
     * Creates a new {@code InputHandler}.
     *
     * @param main       the main application
     * @param sim        simulation instance
     * @param cam        active camera
     * @param uiManager  manager for UI controls
     * @param timeTravel manager for time-travel functionality
     */
    public InputHandler(App main, Simulation sim, Camera cam, UIManager uiManager, TimeTravelManager timeTravel) {
        this.main = main;
        this.sim = sim;
        this.cam = cam;
        this.uiManager = uiManager;
        this.timeTravel = timeTravel;
    }

    /**
     * Attaches mouse and keyboard event handlers to the given canvas and scene.
     *
     * @param canvas the canvas for mouse interactions
     * @param scene  the scene for keyboard interactions
     */
    public void attach(Canvas canvas, Scene scene) {
        setupMouseHandlers(canvas);
        setupKeyboardHandlers(scene);
    }

    // ---------------- Mouse Handling ----------------

    /**
     * Configures mouse click, drag, and scroll handlers for the canvas.
     *
     * @param canvas the canvas to attach handlers to
     */
    private void setupMouseHandlers(Canvas canvas) {
        canvas.setOnMousePressed(e -> {
            if (main.moveOnlyMode) {
                dragDelta.x = e.getX();
                dragDelta.y = e.getY();
                main.stopFollow();
            } else if (e.getButton() == MouseButton.PRIMARY && e.isShiftDown()) {
                if (timeTravel.isTimeTravelMode()) timeTravel.exitTimeTravelMode();
                Vector2D w = cam.screenToWorld(e.getX(), e.getY(), canvas.getWidth(), canvas.getHeight());
                MassPrompt.BodyProperties props = new MassPrompt().prompt(main.getMainStage());

                if (props != null) {
                    sim.addBody(new Body("B" + sim.bodies.size(),
                            props.mass(),
                            new Vector2D(w.x, w.y),
                            new Vector2D(props.velX(), props.velY()),
                            props.radius()
                    ));
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                ContextMenu bodyMenu = new ContextMenu();
                MenuItem removeItem = new MenuItem("Remove");
                MenuItem followItem = new MenuItem("Follow");
                MenuItem modifyItem = new MenuItem("Modify...");
                bodyMenu.getItems().addAll(removeItem, followItem, modifyItem);

                Body b = main.findBodyAt(e.getX(), e.getY(), canvas.getWidth(), canvas.getHeight());
                if (b != null) {
                    removeItem.setOnAction(a -> {
                        if (main.bodyFollowed == b) main.stopFollow();
                        sim.bodies.remove(b);
                    });
                    followItem.setOnAction(a -> {
                        main.bodyFollowed = b;
                        main.following = true;
                        main.uiManager.getBtnFollow().setSelected(true);
                        main.uiManager.selectButton(main.uiManager.getBtnFollow(), -1);
                    });
                    modifyItem.setOnAction(a -> {
                        ModifyBodyPrompt.BodyProperties props = new ModifyBodyPrompt().show(main.getMainStage(), b);
                        if (props != null) {
                            b.mass = props.mass();
                            b.radius = props.radius();
                            b.pos.x = props.xPos();
                            b.pos.y = props.yPos();
                            b.vel.x = props.xVel();
                            b.vel.y = props.yVel();
                        }
                    });
                    bodyMenu.show(canvas, e.getScreenX(), e.getScreenY());
                }
            } else if (e.getButton() == MouseButton.PRIMARY && e.isControlDown()) {
                Body b = main.findBodyAt(e.getX(), e.getY(), canvas.getWidth(), canvas.getHeight());
                if (b != null) {
                    if (timeTravel.isTimeTravelMode()) timeTravel.exitTimeTravelMode();
                    if (main.bodyFollowed == b) main.stopFollow();
                    sim.bodies.remove(b);
                    main.removedBodies.add(b);
                }
            } else if (e.getButton() == MouseButton.PRIMARY) {
                Vector2D w = cam.screenToWorld(e.getX(), e.getY(), canvas.getWidth(), canvas.getHeight());
                sim.addBody(new Body("B" + sim.bodies.size(), main.defMass, new Vector2D(w.x, w.y), new Vector2D(0, 0), 5));
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.MIDDLE || e.getButton() == MouseButton.SECONDARY) {
                double dx = (e.getX() - dragDelta.x) / cam.scale;
                double dy = (e.getY() - dragDelta.y) / cam.scale;
                cam.pan(-dx, -dy);
                dragDelta.x = e.getX();
                dragDelta.y = e.getY();
                main.stopFollow();
            }
        });

        canvas.setOnScroll(e -> {
            if (e.isInertia()) return;
            double dx = e.getDeltaX();
            double dy = e.getDeltaY();
            double threshold = 0.5;
            if (Math.abs(dx) < threshold && Math.abs(dy) < threshold) return;

            boolean possiblePinch = isPossiblePinch(dx, threshold, dy);

            if (possiblePinch) {
                double zoomFactor = Math.pow(1.001, dy);
                Vector2D mouseWorld = cam.screenToWorld(e.getX(), e.getY(), canvas.getWidth(), canvas.getHeight());
                cam.zoom(zoomFactor, mouseWorld.x, mouseWorld.y);
            } else cam.pan(-dx / cam.scale, -dy / cam.scale);
            e.consume();
        });
    }

    // ---------------- Keyboard Handling ----------------

    /**
     * Configures keyboard shortcuts and key events.
     *
     * @param scene the scene to attach handlers to
     */
    private void setupKeyboardHandlers(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W:         handleWKey(e);                          break;
                case A:         handleAKey(e);                          break;
                case S:         handleSKey(e);                          break;
                case D:         handleDKey(e);                          break;
            }
            if (e.isControlDown()) {
                switch (e.getCode()) {
                    case E:     uiManager.clearAllBodies();             break;
                    case F:     handleFKey();                           break;
                    case Z:     handleZKey();                           break;
                    default:                                            break;
                }
            } else switch (e.getCode()) {
                case F11:       uiManager.toggleFullScreen();           break;
                case B:         uiManager.toggleBodiesInfo();           break;
                case C:         uiManager.centerCamera();               break;
                case G:         uiManager.toggleGrid();                 break;
                case H, QUOTE:  uiManager.toggleHelpText();             break;
                case I:         uiManager.toggleInfoText();             break;
                case M:         uiManager.toggleMoveOnlyMode();         break;
                case N:         uiManager.toggleNames();                break;
                case Q:         handleQKey();                           break;
                case T:         uiManager.toggleTimeTravel();           break;
                case SPACE:     uiManager.toggleSimulationRunning();    break;
                case UP:        uiManager.increaseSimSpeed();           break;
                case DOWN:      uiManager.decreaseSimSpeed();           break;
                default:                                                break;
            }

            if (timeTravel.isTimeTravelMode()) {
                handleTimeTravelNavigation(e);
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.Q) {
                main.quitHeld = false;
                main.escHoldStart = 0;
            }
        });
    }

    // --- Helper methods for movement keys (WASD) ---

    private void handleWKey(javafx.scene.input.KeyEvent e) {
        if (e.isControlDown()) {
            if (e.isShiftDown()) cam.offsetY -= 2;
            else cam.offsetY -= 10;
        } else {
            if (e.isShiftDown()) cam.pan(0, -4 / cam.scale);
            else cam.pan(0, -20 / cam.scale);
        }
    }
    private void handleAKey(javafx.scene.input.KeyEvent e) {
        if (e.isControlDown()) {
            if (e.isShiftDown()) cam.offsetX -= 2;
            else cam.offsetX -= 10;
        } else {
            if (e.isShiftDown()) cam.pan(-4 / cam.scale, 0);
            else cam.pan(-20 / cam.scale, 0);
        }
    }
    private void handleSKey(javafx.scene.input.KeyEvent e) {
        if (e.isControlDown()) {
            if (e.isShiftDown()) cam.offsetY += 2;
            else cam.offsetY += 10;
        } else {
            if (e.isShiftDown()) cam.pan(0, 4 / cam.scale);
            else cam.pan(0, 20 / cam.scale);
        }
    }
    private void handleDKey(javafx.scene.input.KeyEvent e) {
        if (e.isControlDown()) {
            if (e.isShiftDown()) cam.offsetX += 2;
            else cam.offsetX += 10;
        } else {
            if (e.isShiftDown()) cam.pan(4 / cam.scale, 0);
            else cam.pan(20 / cam.scale, 0);
        }
    }

    private void handleFKey() {
        if (main.following) {
            main.stopFollow();
            main.startFollow();
        } else {
            uiManager.setFollowSelected(true);
            main.startFollow();
        }
    }
    private void handleQKey() {
        if (!main.quitHeld) {
            main.quitHeld = true;
            main.escHoldStart = System.nanoTime();
        }
    }
    private void handleZKey() {
        System.out.println("ctrl-z huh.. nah this does nothing");
        // TODO
    }

    /**
     * Handles navigation through simulation history when in time travel mode.
     *
     * @param e the key event
     */
    private void handleTimeTravelNavigation(javafx.scene.input.KeyEvent e) {
        if (e.getCode() == KeyCode.LEFT && timeTravel.getCurrentHistoryIndex() > 0) {
            timeTravel.addCurrentHistoryIndex(-1);
            timeTravel.restoreSimulationState(timeTravel.getCurrentHistoryIndex());
        } else if (e.getCode() == KeyCode.RIGHT && timeTravel.getCurrentHistoryIndex() < timeTravel.getSimulationHistory().size() - 1) {
            timeTravel.addCurrentHistoryIndex(1);
            timeTravel.restoreSimulationState(timeTravel.getCurrentHistoryIndex());
        }
    }

    /**
     * Determines if a mouse scroll event represents a pinch-zoom gesture.
     *
     * @param dx        horizontal scroll delta
     * @param threshold minimum delta to consider
     * @param dy        vertical scroll delta
     * @return {@code true} if the event is likely a pinch gesture
     */
    private static boolean isPossiblePinch(double dx, double threshold, double dy) {
        boolean possiblePinch = false;
        if (Math.abs(dx) > threshold && Math.abs(dy) > threshold) {
            if ((dx > 0 && dy < 0) || (dx < 0 && dy > 0)) {
                possiblePinch = true;
            } else {
                double ratio = Math.abs(dx / dy);
                if (ratio > 0.5 && ratio < 2.0) {
                    possiblePinch = true;
                }
            }
        } else if (Math.abs(dx) < threshold && Math.abs(dy) > threshold) {
            possiblePinch = true;
        }
        return possiblePinch;
    }

    /**
     * Helper class storing mouse drag deltas.
     */
    static class Delta {
        double x, y;
    }
}