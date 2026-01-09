package me.holypite.model.map;

import net.minestom.server.coordinate.Pos;

public class MapSpawn {
    private double x, y, z;
    private float yaw, pitch;

    public MapSpawn(double x, double y, double z, float yaw, float pitch) {
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
