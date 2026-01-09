package me.holypite.model.map;

import net.minestom.server.instance.InstanceContainer;

public class LoadedMap {
    private final InstanceContainer instance;
    private final MapConfig config;

    public LoadedMap(InstanceContainer instance, MapConfig config) {
        this.instance = instance;
        this.config = config;
    }

    public InstanceContainer getInstance() {
        return instance;
    }

    public MapConfig getConfig() {
        return config;
    }
}
