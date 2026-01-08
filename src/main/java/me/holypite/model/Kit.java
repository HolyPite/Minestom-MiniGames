package me.holypite.model;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public abstract class Kit {
    
    private final String name;
    private final ItemStack icon; // For UI later

    public Kit(String name, ItemStack icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public abstract void apply(Player player);
}
