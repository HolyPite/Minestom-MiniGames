package me.holypite.model.map;

import java.util.List;

public class MapConfig {
    public String name;
    public Double voidY;
    public MapSettings settings;
    public MapRules rules;
    public List<MapSpawn> spawns; 
    public List<TeamConfig> teams; 
    public List<MapEntityConfig> entities; 
    public List<MapStructureConfig> structures;

    public static class MapSettings {
        public Long time;
        public String weather; // clear, rain, thunder
        public Double worldBorder;
    }

    public static class MapRules {
        public Boolean canFly;
        public Boolean allowHunger;
        public Boolean fallDamage;
        public Boolean canBreakBlocks;
        public Boolean canPlaceBlocks;
    }
}
