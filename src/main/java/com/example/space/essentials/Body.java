package com.example.space.essentials;

/**
 * Represents a physical body in the simulation, characterized by its
 * mass, position, velocity, acceleration, and radius.
 * <p>
 * Each body is influenced by gravity and can be copied or analyzed
 * for its kinematic properties such as speed and acceleration.
 * </p>
 */
public class Body {
    /** Unique identifier or name of the body. */
    public String id;

    /** Mass of the body. */
    public double mass;

    /** Position vector of the body in world coordinates. */
    public Vector2D pos;

    /** Velocity vector of the body. */
    public Vector2D vel;

    /** Acceleration vector of the body. */
    public Vector2D acc;

    /** Radius of the body, used for rendering and hit detection. */
    public double radius;

    /**
     * Constructs a new {@code Body} for default list.
     *
     * @param mass   mass of the bodyù
     * @param radius radius of the body
     */
    public Body(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    /**
     * Constructs a new {@code Body}.
     *
     * @param id     unique identifier or name
     * @param mass   mass of the body
     * @param pos    initial position vector
     * @param vel    initial velocity vector
     * @param radius radius of the body
     */
    public Body(String id, double mass, Vector2D pos, Vector2D vel, double radius) {
        this.id = id;
        this.mass = mass;
        this.pos = pos;
        this.vel = vel;
        this.radius = radius;
        this.acc = new Vector2D();
    }

    /**
     * Creates a deep copy of this body (excluding acceleration).
     *
     * @return a new {@code Body} with identical properties
     */
    public Body copy() {
        return new Body(id, mass, new Vector2D(pos.x, pos.y), new Vector2D(vel.x, vel.y), radius);
    }

    /**
     * Computes the speed of the body as the magnitude of its velocity vector.
     *
     * @return speed of the body
     */
    public double getSpeed() {
        return Math.sqrt(Math.pow(vel.x, 2) + Math.pow(vel.y, 2));
    }

    /**
     * Computes the magnitude of the acceleration vector.
     *
     * @return acceleration magnitude
     */
    public double getAccel() {
        return Math.sqrt(Math.pow(acc.x, 2) + Math.pow(acc.y, 2));
    }
}
