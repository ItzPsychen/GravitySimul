package com.example.space.Essentials;

public class Vector2D {
    public double x, y;
    public Vector2D() { this(0,0); }
    public Vector2D(double x, double y){ this.x = x; this.y = y; }

    public Vector2D add(Vector2D v){ return new Vector2D(x+v.x, y+v.y); }
    public Vector2D sub(Vector2D v){ return new Vector2D(x-v.x, y-v.y); }
    public Vector2D mul(double s){ return new Vector2D(x*s, y*s); }
    public Vector2D div(double s){ return new Vector2D(x/s, y/s); }
    public double mag(){ return Math.hypot(x,y); }
}
