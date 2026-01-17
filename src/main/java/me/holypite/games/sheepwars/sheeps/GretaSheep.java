package me.holypite.games.sheepwars.sheeps;

import me.holypite.manager.StructureManager;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.DyeColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class GretaSheep extends SheepProjectile {

    private static final double CHANCE_SMALL = 73.0;
    private static final double CHANCE_MID = 22.0;
    private static final double CHANCE_BIG = 4.0;
    private static final double CHANCE_GIANT = 1.0;
    
    private final StructureManager structureManager = new StructureManager();

    public GretaSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.GREEN);
            meta.setCustomName(Component.text("Mouton Greta", TextColor.color(0x228B22)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        if (getInstance() == null) return;
        
        Instance instance = getInstance();
        Point origin = getPosition();

        // 1. Determine size category based on probabilities: 73/22/4.5/0.5
        String category = rollCategory();
        File categoryDir = new File("structures/trees/" + category);
        File[] files = categoryDir.listFiles((dir, name) -> name.endsWith(".nbt"));
        
        // Fallback search if category is empty
        if (files == null || files.length == 0) {
            files = findAnyAvailableTree();
        }

        if (files == null || files.length == 0) {
            spawnSmallBush(instance, origin);
            remove();
            return;
        }

        File selectedTree = files[ThreadLocalRandom.current().nextInt(files.length)];
        // Get path relative to "structures/" for StructureManager
        String treeName = "trees/" + selectedTree.getParentFile().getName() + "/" + selectedTree.getName().replace(".nbt", "");

        // 2. Load blocks with random rotation and mirroring
        StructureManager.StructureRotation rotation = StructureManager.StructureRotation.values()[ThreadLocalRandom.current().nextInt(4)];
        StructureManager.StructureMirror mirror = StructureManager.StructureMirror.values()[ThreadLocalRandom.current().nextInt(4)];
        
        StructureManager.StructureData structureData = structureManager.getStructureBlocks(
                treeName, 
                rotation, 
                mirror,
                false // Exclude Air
        );

        if (structureData == null || structureData.blocks().isEmpty()) {
            remove();
            return;
        }
        
        List<StructureManager.StructureBlock> blocks = new ArrayList<>(structureData.blocks());

        // 3. Sort for natural growth (Bottom up)
        blocks.sort(Comparator.comparingInt((StructureManager.StructureBlock b) -> b.relativePos().blockY())
                .thenComparingDouble(b -> b.relativePos().distanceSquared(new Vec(0, b.relativePos().y(), 0))));
        
        // Calculate offset to center the structure on the sheep
        double widthX = structureData.maxPoint().x() - structureData.minPoint().x();
        double widthZ = structureData.maxPoint().z() - structureData.minPoint().z();
        
        Point centerOffset = structureData.minPoint().add(widthX / 2.0, 0, widthZ / 2.0);
        Point placementOrigin = origin.sub(centerOffset);

        // 4. Animated Placement
        AtomicInteger index = new AtomicInteger(0);
        int blocksPerTick = category.equals("giant") ? 40 : (category.equals("big") ? 20 : 5);

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (instance == null || isRemoved()) return TaskSchedule.stop();

            for (int i = 0; i < blocksPerTick; i++) {
                int curr = index.getAndIncrement();
                if (curr >= blocks.size()) {
                    remove();
                    return TaskSchedule.stop();
                }

                StructureManager.StructureBlock sb = blocks.get(curr);
                
                Point target = placementOrigin.add(sb.relativePos());
                instance.setBlock(target, sb.block());

                if (curr % 5 == 0) {
                    TKit.spawnParticles(instance, Particle.HAPPY_VILLAGER, target.add(0.5, 0.5, 0.5), 0.2f, 0.2f, 0.2f, 0.05f, 1);
                }
            }

            return TaskSchedule.tick(1);
        });
    }

    private String rollCategory() {
        double roll = ThreadLocalRandom.current().nextDouble() * 100;
        if (roll < CHANCE_SMALL) return "small";
        if (roll < CHANCE_SMALL + CHANCE_MID) return "mid";
        if (roll < CHANCE_SMALL + CHANCE_MID + CHANCE_BIG) return "big";
        return "giant";
    }

    private File[] findAnyAvailableTree() {
        String[] categories = {"small", "mid", "big", "giant"};
        for (String cat : categories) {
            File dir = new File("structures/trees/" + cat);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".nbt"));
            if (files != null && files.length > 0) return files;
        }
        return null;
    }

    private void spawnSmallBush(Instance instance, Point pos) {
        for (int i = 0; i < 3; i++) {
            instance.setBlock(pos.add(0, i, 0), net.minestom.server.instance.block.Block.OAK_LOG);
        }
        instance.setBlock(pos.add(0, 3, 0), net.minestom.server.instance.block.Block.OAK_LEAVES);
    }

    @Override
    public String getId() {
        return "greta";
    }
}