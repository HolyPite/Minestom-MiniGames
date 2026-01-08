package me.holypite.model.map;

import java.util.List;

public class TeamConfig {
    public String name;
    public String color; // Ex: "RED", "BLUE"
    public int maxPlayers;
    public List<SpawnPos> spawns;

    public TeamConfig(String name, String color, int maxPlayers, List<SpawnPos> spawns) {
        this.name = name;
        this.color = color;
        this.maxPlayers = maxPlayers;
        this.spawns = spawns;
    }
}
