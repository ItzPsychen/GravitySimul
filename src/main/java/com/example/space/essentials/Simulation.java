package com.example.space.essentials;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the physics simulation of bodies under gravity.
 * <p>
 * This class manages a collection of {@link Body} instances
 * and updates their states using the Runge-Kutta 4th-order integration method.
 * </p>
 */
public class Simulation {
    /** All bodies currently in the simulation. */
    public final List<Body> bodies = new ArrayList<>();

    /** Gravitational constant (scaled for simulation). */
    public double G = 6.67430e-11;

    /** Simulation speed multiplier. */
    public double speed = 1.0;

    private boolean running = true;
    private boolean prevRunning = true;

    /**
     * Adds a new body to the simulation.
     *
     * @param b the body to add
     */
    public void addBody(Body b) {
        bodies.add(b);
    }

    /**
     * Computes accelerations on all bodies due to mutual gravitational attraction.
     *
     * @param state list of bodies representing the simulation state
     * @return array of acceleration vectors, one per body
     */
    public Vector2D[] computeAccelerations(List<Body> state) {
        int n = state.size();
        Vector2D[] a = new Vector2D[n];
        for (int i = 0; i < n; i++) a[i] = new Vector2D(0, 0);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Body bi = state.get(i);
                Body bj = state.get(j);
                Vector2D r = bj.pos.sub(bi.pos);
                double dist = Math.max(r.mag(), 1e-6); // avoid division by zero
                double force = G * bi.mass * bj.mass / (dist * dist);
                Vector2D dir = r.div(dist);
                Vector2D accOnI = dir.mul(force / bi.mass);
                Vector2D accOnJ = dir.mul(-force / bj.mass);
                a[i] = a[i].add(accOnI);
                a[j] = a[j].add(accOnJ);
            }
        }
        return a;
    }

    /**
     * Advances the simulation by a time step using
     * the Runge-Kutta 4th-order integration method.
     *
     * @param dt timestep duration
     */
    public void step(double dt) {
        int n = bodies.size();
        List<Body> s0 = new ArrayList<>(n);
        for (Body b : bodies) s0.add(b.copy());

        // Compute RK4 stages...
        Vector2D[] a1 = computeAccelerations(s0);
        List<Vector2D> k1v = new ArrayList<>(), k1x = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            k1v.add(a1[i]);
            k1x.add(s0.get(i).vel);
        }

        List<Body> s2 = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Body b = s0.get(i).copy();
            b.pos = b.pos.add(k1x.get(i).mul(dt * 0.5));
            b.vel = b.vel.add(k1v.get(i).mul(dt * 0.5));
            s2.add(b);
        }
        Vector2D[] a2 = computeAccelerations(s2);
        List<Vector2D> k2v = new ArrayList<>(), k2x = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            k2v.add(a2[i]);
            k2x.add(s2.get(i).vel);
        }

        List<Body> s3 = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Body b = s0.get(i).copy();
            b.pos = b.pos.add(k2x.get(i).mul(dt * 0.5));
            b.vel = b.vel.add(k2v.get(i).mul(dt * 0.5));
            s3.add(b);
        }
        Vector2D[] a3 = computeAccelerations(s3);
        List<Vector2D> k3v = new ArrayList<>(), k3x = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            k3v.add(a3[i]);
            k3x.add(s3.get(i).vel);
        }

        List<Body> s4 = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Body b = s0.get(i).copy();
            b.pos = b.pos.add(k3x.get(i).mul(dt));
            b.vel = b.vel.add(k3v.get(i).mul(dt));
            s4.add(b);
        }
        Vector2D[] a4 = computeAccelerations(s4);
        List<Vector2D> k4v = new ArrayList<>(), k4x = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            k4v.add(a4[i]);
            k4x.add(s4.get(i).vel);
        }

        // Apply RK4 update
        for (int i = 0; i < n; i++) {
            Body bo = bodies.get(i);
            Vector2D dv = k1v.get(i).mul(1.0 / 6.0)
                    .add(k2v.get(i).mul(1.0 / 3.0))
                    .add(k3v.get(i).mul(1.0 / 3.0))
                    .add(k4v.get(i).mul(1.0 / 6.0));
            bo.vel = bo.vel.add(dv.mul(dt));
            Vector2D dx = k1x.get(i).mul(1.0 / 6.0)
                    .add(k2x.get(i).mul(1.0 / 3.0))
                    .add(k3x.get(i).mul(1.0 / 3.0))
                    .add(k4x.get(i).mul(1.0 / 6.0));
            bo.pos = bo.pos.add(dx.mul(dt));

            bo.acc = a1[i];
        }
    }

    /** @return {@code true} if the simulation is running */
    public boolean isRunning() {
        return running;
    }

    /**
     * Sets the running state of the simulation.
     *
     * @param val {@code true} to run, {@code false} to pause
     */
    public void setRunning(boolean val) {
        running = val;
    }

    /** @return previous running state of the simulation */
    public boolean isPrevRunning() {
        return prevRunning;
    }

    /**
     * Sets the previous running state of the simulation.
     *
     * @param val state to set
     */
    public void setPrevRunning(boolean val) {
        prevRunning = val;
    }
}
