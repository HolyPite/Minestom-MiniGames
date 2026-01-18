package me.holypite.games.elytra;

import net.minestom.server.coordinate.Pos;

public record Checkpoint(int id, Pos position, double radius) {
    public boolean contains(Pos other) {
        return position.distanceSquared(other) <= radius * radius;
    }
}
