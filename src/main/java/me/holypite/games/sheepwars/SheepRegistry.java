package me.holypite.games.sheepwars;

import me.holypite.games.sheepwars.sheeps.*;
import me.holypite.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class SheepRegistry {

    private static final List<SheepEntry> SHEEPS = new ArrayList<>();
    // Time in seconds after which weights reach their endWeight (5 minutes)
    public static final long SCALING_DURATION = 5 * 60;

    public record SheepEntry(String id, String name, ItemStack item, double startWeight, double endWeight, Function<Entity, SheepProjectile> factory) {}

    public static void init() {
        // High initial weight, decreases over time
        register("explosive", "Mouton Explosif", 10.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Explosif", NamedTextColor.RED)).modelData("explosive").build(),
                ExplosiveSheep::new);

        register("heal", "Mouton Soin", 8.0, 4.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Soin", TextColor.fromHexString("#ff00ac"))).modelData("heal").build(),
                HealSheep::new);

        // Mid game sheeps
        register("boarding", "Mouton Abordage", 2.0, 10.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Abordage", TextColor.fromHexString("#4682B4"))).modelData("boarding").build(),
                BoardingSheep::new);

        register("icy", "Mouton Glacial", 5.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Glacial", TextColor.fromHexString("#87CEEB"))).modelData("icy").build(),
                IcySheep::new);

        // Powerful sheeps, starts at 0, increases to high
        register("black_hole", "Mouton Trou Noir", 0.0, 8.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Trou Noir", NamedTextColor.BLACK)).modelData("black_hole").build(),
                BlackHoleSheep::new);

        register("earthquake", "Mouton Tremblement de Terre", 0.0, 7.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Tremblement de Terre", TextColor.fromHexString("#8B0000"))).modelData("earthquake").build(),
                EarthquakeSheep::new);

        // Chaos sheeps
        register("party", "Mouton Party!!!", 1.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Party!!!", TextColor.fromHexString("#FF00FF"))).modelData("party").build(),
                PartySheep::new);

        register("greta", "Mouton Greta", 0.5, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Greta", TextColor.fromHexString("#228B22"))).modelData("greta").build(),
                GretaSheep::new);

        // Utility / Distraction
        register("mystery", "Mouton Mystère", 4.0, 4.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Mystère", NamedTextColor.YELLOW)).modelData("mystery").build(),
                MysterySheep::new);

        register("spider", "Mouton Araignée", 5.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Araignée", TextColor.fromHexString("#556B2F"))).modelData("spider").build(),
                SpiderSheep::new);

        register("honey", "Mouton Mielleux", 5.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Mielleux", TextColor.fromHexString("#FFD700"))).modelData("honey").build(),
                HoneySheep::new);

        register("sticky", "Mouton Gluant", 5.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Gluant", TextColor.fromHexString("#32CD32"))).modelData("sticky").build(),
                StickySheep::new);

        register("incendiary", "Mouton Incendiaire", 6.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Incendiaire", TextColor.fromHexString("#FF4500"))).modelData("incendiary").build(),
                IncendiarySheep::new);

        register("burrower", "Mouton Enfouisseur", 4.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Enfouisseur", TextColor.fromHexString("#8B4513"))).modelData("burrower").build(),
                BurrowerSheep::new);

        register("taupe", "Mouton Taupe", 4.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Taupe", TextColor.fromHexString("#8B4513"))).modelData("taupe").build(),
                TaupeSheep::new);

        register("parasite", "Mouton Parasite", 3.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Parasite", TextColor.fromHexString("#4B0082"))).modelData("parasite").build(),
                ParasiteSheep::new);

        register("clone", "Mouton Clone", 2.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Clone", TextColor.fromHexString("#40E0D0"))).modelData("clone").build(),
                CloneSheep::new);

        register("geyser", "Mouton Geyser", 4.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Geyser", TextColor.fromHexString("#1E90FF"))).modelData("geyser").build(),
                GeyserSheep::new);

        register("storm", "Mouton Tempétueux", 2.0, 7.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Tempétueux", TextColor.fromHexString("#708090"))).modelData("storm").build(),
                StormSheep::new);

        register("shield", "Mouton Bouclier", 6.0, 4.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Bouclier", TextColor.fromHexString("#808080"))).modelData("shield").build(),
                ShieldSheep::new);

        register("invisible", "Mouton Invisible", 3.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Invisible", NamedTextColor.WHITE)).modelData("invisible").build(),
                InvisibleSheep::new);

        register("seeker", "Mouton Chercheur", 2.0, 8.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Chercheur", TextColor.fromHexString("#8A2BE2"))).modelData("seeker").build(),
                SeekerSheep::new);

        register("jaw", "Mouton Mâchoire", 4.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Mâchoire", TextColor.fromHexString("#800000"))).modelData("jaw").build(),
                JawSheep::new);

        register("shuffle", "Mouton Shuffle", 3.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Shuffle", TextColor.fromHexString("#FF69B4"))).modelData("shuffle").build(),
                ShuffleSheep::new);

        register("anvil", "Mouton Enclume", 4.0, 7.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Enclume", TextColor.fromHexString("#696969"))).modelData("anvil").build(),
                AnvilSheep::new);

        register("glutton", "Mouton Glouton", 2.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Glouton", TextColor.fromHexString("#0d3d1b"))).modelData("glutton").build(),
                GluttonSheep::new);

        register("fragmentation", "Mouton Fragmentation", 3.0, 7.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Fragmentation", TextColor.fromHexString("#FF8C00"))).modelData("fragmentation").build(),
                FragmentationSheep::new);

        register("trapped", "Mouton Piégé", 5.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Piégé", TextColor.fromHexString("#FFC0CB"))).modelData("trapped").build(),
                TrappedSheep::new);

        register("blast", "Mouton Déflagration", 4.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Déflagration", TextColor.fromHexString("#FFD700"))).modelData("blast").build(),
                BlastSheep::new);

        register("thorny", "Mouton Épineux", 5.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Épineux", TextColor.fromHexString("#008000"))).modelData("thorny").build(),
                ThornySheep::new);

        register("hedgehog", "Mouton Hérisson", 3.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Hérisson", TextColor.fromHexString("#B8860B"))).modelData("hedgehog").build(),
                HedgehogSheep::new);

        register("radioactive", "Mouton Radioactif", 1.0, 6.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Radioactif", TextColor.fromHexString("#ADFF2F"))).modelData("radioactive").build(),
                RadioactiveSheep::new);

        register("glowing", "Mouton Glowing", 5.0, 5.0,
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Glowing", TextColor.fromHexString("#FFFFE0"))).modelData("glowing").build(),
                GlowingSheep::new);
    }

    private static void register(String id, String name, double startWeight, double endWeight, ItemStack item, Function<Entity, SheepProjectile> factory) {
        SHEEPS.add(new SheepEntry(id, name, item, startWeight, endWeight, factory));
    }

    public static Optional<SheepEntry> getSheepByItem(ItemStack item) {
        CustomModelData data = item.get(DataComponents.CUSTOM_MODEL_DATA);
        if (data == null || data.strings().isEmpty()) return Optional.empty();
        String id = data.strings().get(0);
        return SHEEPS.stream().filter(entry -> entry.id.equals(id)).findFirst();
    }

    public static double getCurrentWeight(SheepEntry entry, long elapsedSeconds) {
        double progress = Math.min(1.0, (double) elapsedSeconds / SCALING_DURATION);
        return entry.startWeight + (progress * (entry.endWeight - entry.startWeight));
    }

    public static ItemStack getRandomSheepItem(long elapsedSeconds) {
        SheepEntry entry = getRandomSheepEntry(elapsedSeconds, List.of());
        return entry != null ? entry.item : ItemStack.AIR;
    }

    public static Function<Entity, SheepProjectile> getRandomSheepFactory() {
        return getRandomSheepFactory(0, List.of("mystery"));
    }

    public static Function<Entity, SheepProjectile> getRandomSheepFactory(long elapsedSeconds, List<String> blacklist) {
        SheepEntry entry = getRandomSheepEntry(elapsedSeconds, blacklist);
        return entry != null ? entry.factory : null;
    }

    private static SheepEntry getRandomSheepEntry(long elapsedSeconds, List<String> blacklist) {
        if (SHEEPS.isEmpty()) return null;

        List<SheepEntry> pool = SHEEPS.stream()
                .filter(e -> blacklist.stream().noneMatch(b -> e.id.equalsIgnoreCase(b)))
                .toList();

        if (pool.isEmpty()) return null;

        double totalWeight = pool.stream()
                .mapToDouble(e -> getCurrentWeight(e, elapsedSeconds))
                .sum();

        if (totalWeight <= 0) return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));

        double roll = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double currentSum = 0;

        for (SheepEntry entry : pool) {
            currentSum += getCurrentWeight(entry, elapsedSeconds);
            if (roll <= currentSum) {
                return entry;
            }
        }

        return pool.get(pool.size() - 1);
    }
    
    public static ItemStack getSheepItemById(String id) {
        return SHEEPS.stream().filter(entry -> entry.id.equalsIgnoreCase(id)).findFirst().map(entry -> entry.item).orElse(ItemStack.AIR);
    }
    
    public static List<String> getSheepIds() {
        return SHEEPS.stream().map(entry -> entry.id).toList();
    }
}