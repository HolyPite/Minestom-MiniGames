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

    public record SheepEntry(String id, String name, ItemStack item, Function<Entity, SheepProjectile> factory) {}

    public static void init() {
        register("explosive", "Mouton Explosif", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Explosif", NamedTextColor.RED)).modelData("explosive").build(),
                ExplosiveSheep::new);

        register("black_hole", "Mouton Trou Noir", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Trou Noir", NamedTextColor.BLACK)).modelData("black_hole").build(),
                BlackHoleSheep::new);

        register("taupe", "Mouton Taupe", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Taupe", TextColor.fromHexString("#6B4226"))).modelData("taupe").build(),
                TaupeSheep::new);

        register("boarding", "Mouton Abordage", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Abordage", TextColor.fromHexString("#4682B4"))).modelData("boarding").build(),
                BoardingSheep::new);

        register("mystery", "Mouton Mystère", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Mystère", NamedTextColor.YELLOW)).modelData("mystery").build(),
                MysterySheep::new);

        register("spider", "Mouton Araignée", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Araignée", TextColor.fromHexString("#556B2F"))).modelData("spider").build(),
                SpiderSheep::new);

        register("icy", "Mouton Glacial", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Glacial", TextColor.fromHexString("#87CEEB"))).modelData("icy").build(),
                IcySheep::new);

        register("honey", "Mouton Mielleux", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Mielleux", TextColor.fromHexString("#FFD700"))).modelData("honey").build(),
                HoneySheep::new);

        register("sticky", "Mouton Gluant", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Gluant", TextColor.fromHexString("#32CD32"))).modelData("sticky").build(),
                StickySheep::new);

        register("incendiary", "Mouton Incendiaire", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Incendiaire", TextColor.fromHexString("#FF4500"))).modelData("incendiary").build(),
                IncendiarySheep::new);

        register("burrower", "Mouton Enfouisseur", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Enfouisseur", TextColor.fromHexString("#8B4513"))).modelData("burrower").build(),
                BurrowerSheep::new);

        register("parasite", "Mouton Parasite", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Parasite", TextColor.fromHexString("#4B0082"))).modelData("parasite").build(),
                ParasiteSheep::new);

        register("clone", "Mouton Clone", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Clone", TextColor.fromHexString("#40E0D0"))).modelData("clone").build(),
                CloneSheep::new);

        register("party", "Mouton Party!!!", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Party!!!", TextColor.fromHexString("#FF00FF"))).modelData("party").build(),
                PartySheep::new);

        register("geyser", "Mouton Geyser", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Geyser", TextColor.fromHexString("#1E90FF"))).modelData("geyser").build(),
                GeyserSheep::new);

        register("storm", "Mouton Tempétueux", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Tempétueux", TextColor.fromHexString("#708090"))).modelData("storm").build(),
                StormSheep::new);

        register("earthquake", "Mouton Tremblement de Terre", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Tremblement de Terre", TextColor.fromHexString("#8B0000"))).modelData("earthquake").build(),
                EarthquakeSheep::new);

        register("greta", "Mouton Greta", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Greta", TextColor.fromHexString("#228B22"))).modelData("greta").build(),
                GretaSheep::new);

        register("shield", "Mouton Bouclier", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Bouclier", TextColor.fromHexString("#808080"))).modelData("shield").build(),
                ShieldSheep::new);

        register("invisible", "Mouton Invisible", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Invisible", NamedTextColor.WHITE)).modelData("invisible").build(),
                InvisibleSheep::new);

        register("seeker", "Mouton Chercheur", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Chercheur", TextColor.fromHexString("#8A2BE2"))).modelData("seeker").build(),
                SeekerSheep::new);

        register("heal", "Mouton Soin", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Soin", TextColor.fromHexString("#ff00ac"))).modelData("heal").build(),
                HealSheep::new);

        register("jaw", "Mouton Mâchoire", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Mâchoire", TextColor.fromHexString("#800000"))).modelData("jaw").build(),
                JawSheep::new);

        register("shuffle", "Mouton Shuffle", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Shuffle", TextColor.fromHexString("#FF69B4"))).modelData("shuffle").build(),
                ShuffleSheep::new);

        register("anvil", "Mouton Enclume", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Enclume", TextColor.fromHexString("#696969"))).modelData("anvil").build(),
                AnvilSheep::new);

        register("glutton", "Mouton Glouton", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Glouton", TextColor.fromHexString("#0d3d1b"))).modelData("glutton").build(),
                GluttonSheep::new);

        register("fragmentation", "Mouton Fragmentation", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Fragmentation", TextColor.fromHexString("#FF8C00"))).modelData("fragmentation").build(),
                FragmentationSheep::new);

        register("trapped", "Mouton Piégé", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Piégé", TextColor.fromHexString("#FFC0CB"))).modelData("trapped").build(),
                TrappedSheep::new);

        register("blast", "Mouton Déflagration", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Déflagration", TextColor.fromHexString("#FFD700"))).modelData("blast").build(),
                BlastSheep::new);

        register("thorny", "Mouton Épineux", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Épineux", TextColor.fromHexString("#008000"))).modelData("thorny").build(),
                ThornySheep::new);

        register("hedgehog", "Mouton Hérisson", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Hérisson", TextColor.fromHexString("#B8860B"))).modelData("hedgehog").build(),
                HedgehogSheep::new);

        register("radioactive", "Mouton Radioactif", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Radioactif", TextColor.fromHexString("#ADFF2F"))).modelData("radioactive").build(),
                RadioactiveSheep::new);

        register("glowing", "Mouton Glowing", 
                new ItemBuilder(Material.WHITE_WOOL).name(Component.text("Mouton Glowing", TextColor.fromHexString("#FFFFE0"))).modelData("glowing").build(),
                GlowingSheep::new);
    }

    private static void register(String id, String name, ItemStack item, Function<Entity, SheepProjectile> factory) {
        SHEEPS.add(new SheepEntry(id, name, item, factory));
    }

    public static Optional<SheepEntry> getSheepByItem(ItemStack item) {
        CustomModelData data = item.get(DataComponents.CUSTOM_MODEL_DATA);
        if (data == null || data.strings().isEmpty()) return Optional.empty();
        String id = data.strings().get(0);
        return SHEEPS.stream().filter(entry -> entry.id.equals(id)).findFirst();
    }

    public static ItemStack getRandomSheepItem() {
        if (SHEEPS.isEmpty()) return ItemStack.AIR;
        return SHEEPS.get(ThreadLocalRandom.current().nextInt(SHEEPS.size())).item;
    }
    
    public static ItemStack getSheepItemById(String id) {
        return SHEEPS.stream().filter(entry -> entry.id.equalsIgnoreCase(id)).findFirst().map(entry -> entry.item).orElse(ItemStack.AIR);
    }
    
    public static List<String> getSheepIds() {
        return SHEEPS.stream().map(entry -> entry.id).toList();
    }
    
    public static Function<Entity, SheepProjectile> getRandomSheepFactory() {
        return getRandomSheepFactory(List.of("mystery"));
    }

    public static Function<Entity, SheepProjectile> getRandomSheepFactory(List<String> blacklist) {
        List<SheepEntry> validSheeps = SHEEPS.stream()
                .filter(e -> blacklist.stream().noneMatch(b -> e.id.equalsIgnoreCase(b)))
                .toList();
        if (validSheeps.isEmpty()) return null;
        return validSheeps.get(ThreadLocalRandom.current().nextInt(validSheeps.size())).factory;
    }
}
