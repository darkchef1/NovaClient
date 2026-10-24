package com.nova.waypoint;

/** A named spot in a world, saved per server and dimension. */
public class Waypoint {
    public static final int[] COLORS = {0xFF4C9DFF, 0xFFFF5A5A, 0xFF3DDC84, 0xFFFFD23C, 0xFFA86BFF, 0xFFFF5FB4, 0xFF3DE0D8, 0xFFFFFFFF};

    public String name;
    public int x;
    public int y;
    public int z;
    public int dimension;
    public String server;
    public int colorIndex;
    public boolean visible = true;

    public Waypoint(String name, int x, int y, int z, int dimension, String server, int colorIndex) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.server = server;
        this.colorIndex = colorIndex;
    }

    public int getColor() {
        return COLORS[Math.floorMod(colorIndex, COLORS.length)];
    }

    public void nextColor() {
        colorIndex = (colorIndex + 1) % COLORS.length;
    }
}
