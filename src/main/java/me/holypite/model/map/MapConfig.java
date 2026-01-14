package me.holypite.model.map;

import java.util.List;

public class MapConfig {
    public String name;
    public int minPlayers;
    public int maxPlayers;
    public List<MapSpawn> spawns; // Global spawns
    public List<TeamConfig> teams; // Teams
    public List<MapEntityConfig> entities; // Decorative entities
    public List<MapStructureConfig> structures; // Structures to paste (if no region file)
    public Double voidY; // Custom void threshold
}