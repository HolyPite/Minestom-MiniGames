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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class GretaSheep extends SheepProjectile {

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

        // 1. Find a random tree in structures/trees/
        File treesDir = new File("structures/trees");
        File[] files = treesDir.listFiles((dir, name) -> name.endsWith(".nbt"));
        
        if (files == null || files.length == 0) {
            // Fallback if no structures found
            spawnSmallBush(instance, origin);
            remove();
            return;
        }

        File selectedTree = files[ThreadLocalRandom.current().nextInt(files.length)];
        String treeName = "trees/" + selectedTree.getName().replace(".nbt", "");

        // 2. Load blocks
        List<StructureManager.StructureBlock> blocks = structureManager.getStructureBlocks(
                treeName, 
                StructureManager.StructureRotation.values()[ThreadLocalRandom.current().nextInt(4)], 
                StructureManager.StructureMirror.NONE
        );

        if (blocks == null || blocks.isEmpty()) {
            remove();
            return;
        }

        // 3. Sort by height (Y) then by distance from trunk (XZ) for natural growth
        blocks.sort(Comparator.comparingInt((StructureManager.StructureBlock b) -> b.relativePos().blockY())
                .thenComparingDouble(b -> b.relativePos().distanceSquared(new Vec(0, b.relativePos().y(), 0))));

        // 4. Animated Placement
        AtomicInteger index = new AtomicInteger(0);
        int blocksPerTick = 2;

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (instance == null || isRemoved()) return TaskSchedule.stop();

            for (int i = 0; i < blocksPerTick; i++) {
                int curr = index.getAndIncrement();
                if (curr >= blocks.size()) {
                    remove();
                    return TaskSchedule.stop();
                }

                StructureManager.StructureBlock sb = blocks.get(curr);
                Point target = origin.add(sb.relativePos());
                instance.setBlock(target, sb.block());

                // Visual effect on placement
                if (curr % 5 == 0) {
                    instance.sendGroupedPacket(new ParticlePacket(
                            Particle.HAPPY_VILLAGER,
                            target.add(0.5, 0.5, 0.5),
                            new Vec(0.2, 0.2, 0.2),
                            0.05f, 1
                    ));
                }
            }

            return TaskSchedule.tick(1);
        });
    }

    private void spawnSmallBush(Instance instance, Point pos) {
        // Simple procedural fallback
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
