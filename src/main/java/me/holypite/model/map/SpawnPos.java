package me.holypite.model.map;

import net.minestom.server.coordinate.Pos;

public class SpawnPos {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public SpawnPos(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Pos toPos() {
        return new Pos(x, y, z, yaw, pitch);
    }
}
