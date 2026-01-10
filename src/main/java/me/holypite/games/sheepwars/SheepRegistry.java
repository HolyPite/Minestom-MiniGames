package me.holypite.games.sheepwars;

import me.holypite.games.sheepwars.sheeps.ExplosiveSheep;
import me.holypite.games.sheepwars.sheeps.SheepProjectile;
import me.holypite.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class SheepRegistry {

    private static final List<SheepEntry> SHEEPS = new ArrayList<>();

    public record SheepEntry(String name, ItemStack item, Function<Entity, SheepProjectile> factory) {}

    public static void init() {
        // Explosive Sheep (ID 1)
        register("Explosive Sheep", 
                new ItemBuilder(Material.RED_WOOL).name(Component.text("Explosive Sheep", NamedTextColor.RED)).modelData(1).build(),
                ExplosiveSheep::new
        );
        
        // Add others here...
    }

    private static void register(String name, ItemStack item, Function<Entity, SheepProjectile> factory) {
        SHEEPS.add(new SheepEntry(name, item, factory));
    }

    public static Optional<SheepEntry> getSheepByItem(ItemStack item) {
        // Compare Material and ModelData
        return SHEEPS.stream()
                .filter(entry -> entry.item.material() == item.material() && 
                        entry.item.getMeta().getCustomModelData() == item.getMeta().getCustomModelData())
                .findFirst();
    }

    public static ItemStack getRandomSheepItem() {
        if (SHEEPS.isEmpty()) return ItemStack.AIR;
        return SHEEPS.get(ThreadLocalRandom.current().nextInt(SHEEPS.size())).item;
    }
}
