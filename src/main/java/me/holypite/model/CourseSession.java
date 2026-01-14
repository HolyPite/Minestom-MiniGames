package me.holypite.model;

import net.minestom.server.entity.Player;

public class CourseSession {
    private final Player player;
    private final long startTime;
    private int rocketsUsed;
    private int nextCheckpointIndex;

    public CourseSession(Player player) {
        this.player = player;
        this.startTime = System.currentTimeMillis();
        this.rocketsUsed = 0;
        this.nextCheckpointIndex = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getRocketsUsed() {
        return rocketsUsed;
    }

    public void incrementRocketsUsed() {
        this.rocketsUsed++;
    }

    public int getNextCheckpointIndex() {
        return nextCheckpointIndex;
    }

    public void incrementCheckpointIndex() {
        this.nextCheckpointIndex++;
    }
}
