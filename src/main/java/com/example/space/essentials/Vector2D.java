package com.example.space.essentials;

/**
 * Represents a 2D vector with common vector operations
 * such as addition, subtraction, scaling, and magnitude.
 */
public class Vector2D {
    /** X component of the vector. */
    public double x;

    /** Y component of the vector. */
    public double y;

    /** Constructs a zero vector. */
    public Vector2D() {
        this(0, 0);
    }

    /**
     * Constructs a vector with the given components.
     *
     * @param x x-component
     * @param y y-component
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Adds this vector to another.
     *
     * @param v vector to add
     * @return resulting vector
     */
    public Vector2D add(Vector2D v) {
        return new Vector2D(x + v.x, y + v.y);
    }

    /**
     * Subtracts another vector from this vector.
     *
     * @param v vector to subtract
     * @return resulting vector
     */
    public Vector2D sub(Vector2D v) {
        return new Vector2D(x - v.x, y - v.y);
    }

    /**
     * Multiplies this vector by a scalar.
     *
     * @param s scalar factor
     * @return resulting scaled vector
     */
    public Vector2D mul(double s) {
        return new Vector2D(x * s, y * s);
    }

    /**
     * Divides this vector by a scalar.
     *
     * @param s scalar divisor
     * @return resulting scaled vector
     */
    public Vector2D div(double s) {
        return new Vector2D(x / s, y / s);
    }

    /**
     * Computes the magnitude (length) of the vector.
     *
     * @return vector magnitude
     */
    public double mag() {
        return Math.hypot(x, y);
    }
}
