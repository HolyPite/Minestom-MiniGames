package me.holypite.games.sheepwars;

import me.holypite.games.sheepwars.sheeps.BlackHoleSheep;
import me.holypite.games.sheepwars.sheeps.BoardingSheep;
import me.holypite.games.sheepwars.sheeps.ExplosiveSheep;
import me.holypite.games.sheepwars.sheeps.IcySheep;
import me.holypite.games.sheepwars.sheeps.MysterySheep;
import me.holypite.games.sheepwars.sheeps.SheepProjectile;
import me.holypite.games.sheepwars.sheeps.SpiderSheep;
import me.holypite.games.sheepwars.sheeps.TaupeSheep;
import me.holypite.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.component.CustomModelData;
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

    public record SheepEntry(String id, String name, ItemStack item, Function<Entity, SheepProjectile> factory) {}

    public static void init() {
        // Explosive Sheep
        register("explosive", "Mouton Explosif", 
                new ItemBuilder(Material.WHITE_WOOL)
                        .name(Component.text("Mouton Explosif", NamedTextColor.RED))
                        .modelData("explosive").build(),
                ExplosiveSheep::new
        );

        // Black Hole Sheep
        register("black_hole", "Mouton Trou Noir", 
                new ItemBuilder(Material.WHITE_WOOL)
                        .name(Component.text("Mouton Trou Noir", NamedTextColor.BLACK))
                        .modelData("black_hole").build(),
                BlackHoleSheep::new
        );

        // Taupe Sheep
        register("taupe", "Mouton Taupe", 
                new ItemBuilder(Material.WHITE_WOOL)
                        .name(Component.text("Mouton Taupe", TextColor.fromHexString("#6B4226")))
                        .modelData("taupe").build(),
                TaupeSheep::new
        );

        // Boarding Sheep
        register("boarding", "Mouton Abordage", 
                new ItemBuilder(Material.WHITE_WOOL)
                        .name(Component.text("Mouton Abordage", TextColor.fromHexString("#4682B4")))
                        .modelData("boarding").build(),
                BoardingSheep::new
        );

        // Mystery Sheep
        register("mystery", "Mouton Mystère", 
                new ItemBuilder(Material.WHITE_WOOL)
                        .name(Component.text("Mouton Mystère", NamedTextColor.YELLOW))
                        .modelData("mystery").build(),
                MysterySheep::new
        );

        // Spider Sheep
        register("spider", "Mouton Araignée", 
                new ItemBuilder(Material.WHITE_WOOL)
                        .name(Component.text("Mouton Araignée", TextColor.fromHexString("#556B2F")))
                        .modelData("spider").build(),
                SpiderSheep::new
        );

        // Icy Sheep
        register("icy", "Mouton Glacial", 
                new ItemBuilder(Material.WHITE_WOOL)
                        .name(Component.text("Mouton Glacial", TextColor.fromHexString("#87CEEB")))
                        .modelData("icy").build(),
                IcySheep::new
        );
        
        // Add others here...
    }

    private static void register(String id, String name, ItemStack item, Function<Entity, SheepProjectile> factory) {
        SHEEPS.add(new SheepEntry(id, name, item, factory));
    }

    public static Optional<SheepEntry> getSheepByItem(ItemStack item) {
        CustomModelData data = item.get(DataComponents.CUSTOM_MODEL_DATA);
        if (data == null || data.strings().isEmpty()) return Optional.empty();
        
        String id = data.strings().get(0);

        return SHEEPS.stream()
                .filter(entry -> entry.id.equals(id)) // Faster check using ID directly if matched with modelData
                .findFirst();
    }

    public static ItemStack getRandomSheepItem() {
        if (SHEEPS.isEmpty()) return ItemStack.AIR;
        return SHEEPS.get(ThreadLocalRandom.current().nextInt(SHEEPS.size())).item;
    }
    
    public static ItemStack getSheepItemById(String id) {
        return SHEEPS.stream()
                .filter(entry -> entry.id.equalsIgnoreCase(id))
                .findFirst()
                .map(entry -> entry.item)
                .orElse(ItemStack.AIR);
    }
    
    public static List<String> getSheepIds() {
        return SHEEPS.stream().map(entry -> entry.id).toList();
    }
    
    public static Function<Entity, SheepProjectile> getRandomSheepFactory() {
        List<SheepEntry> validSheeps = SHEEPS.stream()
                .filter(e -> !e.id.equals("mystery"))
                .toList();
                
        if (validSheeps.isEmpty()) return null;
        return validSheeps.get(ThreadLocalRandom.current().nextInt(validSheeps.size())).factory;
    }
    
    // Kept for compatibility if needed, but IDs are better
    public static ItemStack getSheepItemByName(String name) {
        return SHEEPS.stream()
                .filter(entry -> entry.name.equalsIgnoreCase(name))
                .findFirst()
                .map(entry -> entry.item)
                .orElse(ItemStack.AIR);
    }
}