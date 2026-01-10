package me.holypite.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.component.CustomModelData;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public class ItemBuilder {

    private final ItemStack.Builder builder;

    public ItemBuilder(Material material) {
        this.builder = ItemStack.builder(material);
    }

    public ItemBuilder name(Component name) {
        builder.set(DataComponents.ITEM_NAME, name);
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        builder.set(DataComponents.LORE, lore);
        return this;
    }

    public ItemBuilder modelData(int data) {
        builder.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float) data), List.of(), List.of(), List.of()));
        return this;
    }

    public ItemBuilder modelData(String id) {
        builder.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(id), List.of()));
        return this;
    }

    public ItemStack build() {
        return builder.build();
    }
}