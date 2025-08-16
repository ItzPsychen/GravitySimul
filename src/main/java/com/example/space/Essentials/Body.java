package com.example.space.Essentials;

public class Body {
    public String id;
    public double mass;
    public Vector2D pos;
    public Vector2D vel;
    public Vector2D acc;
    public double radius;

    public Body(String id, double mass, Vector2D pos, Vector2D vel, double radius) {
        this.id = id; this.mass = mass; this.pos = pos; this.vel = vel; this.radius = radius;
        this.acc = new Vector2D();
    }
    public Body copy() {
        return new Body(id, mass, new Vector2D(pos.x,pos.y), new Vector2D(vel.x,vel.y), radius);
    }

    public double getSpeed() {
        return Math.sqrt(Math.pow(vel.x,2) + Math.pow(vel.y,2));
    }
    public double getAccel() {
        return Math.sqrt(Math.pow(acc.x,2) + Math.pow(acc.y,2));
    }
}
