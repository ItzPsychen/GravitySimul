package com.example.space.Managers;

import com.example.space.Essentials.Body;
import com.example.space.Essentials.Simulation;

import java.util.ArrayList;
import java.util.List;

public class TimeTravelManager {
    private final Simulation sim;
    private boolean timeTravelMode = false;
    private int currentHistoryIndex = -1;
    private final List<List<Body>> simulationHistory = new ArrayList<>();

    public TimeTravelManager(Simulation sim) {
        this.sim = sim;
    }

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
    public void restoreSimulationState(int index) {
        if (index < 0 || index >= simulationHistory.size()) return;
        sim.bodies.clear();
        for (Body b : simulationHistory.get(index)) {
            sim.bodies.add(b.copy());
        }
    }
    public void exitTimeTravelMode() {
        while (simulationHistory.size() > currentHistoryIndex + 1) {
            simulationHistory.remove(simulationHistory.size() - 1);
        }
        saveSimulationState();
        timeTravelMode = false;
    }

    public boolean isTimeTravelMode() {
        return timeTravelMode;
    }
    public void setTimeTravelMode(boolean val) {
        timeTravelMode = val;
    }

    public void addCurrentHistoryIndex(int val) {
        currentHistoryIndex += val;
    }
    public int getCurrentHistoryIndex() {
        return currentHistoryIndex;
    }
    public List<List<Body>> getSimulationHistory() {
        return simulationHistory;
    }
}
