package me.holypite.model.map;

import java.util.List;
import java.util.Map;

public class MapEntityConfig {
    public String type;
    public MapSpawn pos;
    public Map<String, String> meta;
    public List<MapEntityConfig> passengers;
}
