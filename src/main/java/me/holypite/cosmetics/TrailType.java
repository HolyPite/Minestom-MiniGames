package me.holypite.cosmetics;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum TrailType {
    NONE("Aucun", NamedTextColor.GRAY),
    SMOKE("Fumée", NamedTextColor.DARK_GRAY),
    FLAME("Flammes", NamedTextColor.RED),
    HEARTS("Cœurs", NamedTextColor.LIGHT_PURPLE),
    MUSIC("Musique", NamedTextColor.AQUA),
    RAINBOW("Arc-en-ciel", NamedTextColor.GOLD);

    private final String displayName;
    private final TextColor color;

    TrailType(String displayName, TextColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TextColor getColor() {
        return color;
    }
}
