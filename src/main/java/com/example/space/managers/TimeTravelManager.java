package com.example.space.managers;

import com.example.space.essentials.Body;
import com.example.space.essentials.Simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages time-travel functionality, allowing the simulation to save and restore states.
 */
public class TimeTravelManager {
    private final Simulation sim;
    private boolean timeTravelMode = false;
    private int currentHistoryIndex = -1;
    private final List<List<Body>> simulationHistory = new ArrayList<>();

    /**
     * Constructs a new TimeTravelManager.
     *
     * @param sim the simulation to manage
     */
    public TimeTravelManager(Simulation sim) {
        this.sim = sim;
    }

    /**
     * Saves the current state of the simulation.
     */
    public void saveSimulationState() {
        int MAX_HISTORY_STATES = 1000;
        if (simulationHistory.size() >= MAX_HISTORY_STATES) {
            simulationHistory.remove(0);
        }
        List<Body> stateCopy = new ArrayList<>();
        for (Body b : sim.bodies) {
            stateCopy.add(b.copy());
        }
        simulationHistory.add(stateCopy);
        currentHistoryIndex = simulationHistory.size() - 1;
    }

    /**
     * Restores a saved simulation state.
     *
     * @param index the index of the state to restore
     */
    public void restoreSimulationState(int index) {
        if (index < 0 || index >= simulationHistory.size()) return;
        sim.bodies.clear();
        for (Body b : simulationHistory.get(index)) {
            sim.bodies.add(b.copy());
        }
    }

    /**
     * Exits time-travel mode and cleans up history.
     */
    public void exitTimeTravelMode() {
        while (simulationHistory.size() > currentHistoryIndex + 1) {
            simulationHistory.remove(simulationHistory.size() - 1);
        }
        saveSimulationState();
        timeTravelMode = false;
    }

    /**
     * Checks if time-travel mode is active.
     *
     * @return true if in time-travel mode, false otherwise
     */
    public boolean isTimeTravelMode() {
        return timeTravelMode;
    }

    /**
     * Sets time-travel mode.
     *
     * @param val true to enable, false to disable
     */
    public void setTimeTravelMode(boolean val) {
        timeTravelMode = val;
    }

    /**
     * Adjusts the current history index.
     *
     * @param val the value to add to the index
     */
    public void addCurrentHistoryIndex(int val) {
        currentHistoryIndex += val;
    }

    /**
     * Gets the current history index.
     *
     * @return the current index
     */
    public int getCurrentHistoryIndex() {
        return currentHistoryIndex;
    }

    /**
     * Gets the simulation history.
     *
     * @return a list of saved simulation states
     */
    public List<List<Body>> getSimulationHistory() {
        return simulationHistory;
    }
}