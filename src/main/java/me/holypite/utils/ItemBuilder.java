package me.holypite.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.ItemComponent;

import java.util.List;

public class ItemBuilder {

    private final ItemStack.Builder builder;

    public ItemBuilder(Material material) {
        this.builder = ItemStack.builder(material);
    }

    public ItemBuilder name(Component name) {
        builder.set(ItemComponent.ITEM_NAME, name);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        builder.set(ItemComponent.LORE, lore);
        return this;
    }

    public ItemBuilder modelData(int data) {
        builder.set(ItemComponent.CUSTOM_MODEL_DATA, data);
        return this;
    }

    public ItemStack build() {
        return builder.build();
    }
}
