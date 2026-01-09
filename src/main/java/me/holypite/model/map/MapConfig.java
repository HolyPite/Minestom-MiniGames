package me.holypite.model.map;

import java.util.List;

public class MapConfig {
    public String name;
    public int minPlayers;
    public int maxPlayers;
    public List<MapSpawn> spawns; // Global spawns (if no teams)
    public List<TeamConfig> teams; // Teams configuration
}
