package me.holypite.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.Material;

public enum GameType {
    DUEL(Material.IRON_SWORD, "Duel 1v1", "Affrontez un autre joueur dans une arène."),
    SHEEP_WARS(Material.WHITE_WOOL, "SheepWars", "Lancez des moutons explosifs sur vos ennemis !");

    private final Material icon;
    private final String displayName;
    private final String description;

    GameType(Material icon, String displayName, String description) {
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
    }

    public Material getIcon() {
        return icon;
    }

    public Component getDisplayName() {
        return Component.text(displayName, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false);
    }

    public Component getDescription() {
        return Component.text(description, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
    }
}
