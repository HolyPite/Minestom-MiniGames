package me.holypite.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StructurePreviewManager {

    private final StructureManager structureManager;
    private final Map<UUID, ActivePreview> activePreviews = new ConcurrentHashMap<>();

    public StructurePreviewManager(StructureManager structureManager) {
        this.structureManager = structureManager;
    }

    public void startPreview(Player player, String structureName) {
        cancelPreview(player); // Cancel previous if any

        List<StructureManager.StructureBlock> blocks = structureManager.getStructureBlocks(
                structureName, 
                StructureManager.StructureRotation.R0, 
                StructureManager.StructureMirror.NONE
        );

        if (blocks == null || blocks.isEmpty()) {
            player.sendMessage(Component.text("Structure not found or empty!", NamedTextColor.RED));
            return;
        }

        if (blocks.size() > 500) {
            player.sendMessage(Component.text("Structure too large for full preview (Limit: 500 blocks).", NamedTextColor.YELLOW));
            // We could implement a wireframe mode here
        }

        ActivePreview preview = new ActivePreview(player, structureName, blocks);
        activePreviews.put(player.getUuid(), preview);
        
        player.sendMessage(Component.text("Preview started! Clic gauche pour poser, Sneak pour annuler.", NamedTextColor.GREEN));
    }

    public void cancelPreview(Player player) {
        ActivePreview preview = activePreviews.remove(player.getUuid());
        if (preview != null) {
            preview.cleanup();
        }
    }

    public void confirmPreview(Player player) {
        ActivePreview preview = activePreviews.remove(player.getUuid());
        if (preview != null) {
            Pos pos = preview.getCurrentPos();
            structureManager.placeStructure(player.getInstance(), pos, preview.name);
            preview.cleanup();
            player.sendMessage(Component.text("Structure placed!", NamedTextColor.GREEN));
        }
    }

    public boolean hasPreview(Player player) {
        return activePreviews.containsKey(player.getUuid());
    }

    private class ActivePreview {
        private final Player player;
        private final String name;
        private final List<Entity> displayEntities = new ArrayList<>();
        private final List<StructureManager.StructureBlock> blocks;
        private Pos currentPos;
        private final Task updateTask;

        public ActivePreview(Player player, String name, List<StructureManager.StructureBlock> blocks) {
            this.player = player;
            this.name = name;
            this.blocks = blocks;
            this.currentPos = player.getPosition();

            // Create entities
            Instance instance = player.getInstance();
            for (int i = 0; i < Math.min(blocks.size(), 500); i++) {
                StructureManager.StructureBlock sb = blocks.get(i);
                Entity display = new Entity(EntityType.BLOCK_DISPLAY);
                BlockDisplayMeta meta = (BlockDisplayMeta) display.getEntityMeta();
                
                meta.setBlockState(sb.block());
                meta.setScale(new Vec(0.99, 0.99, 0.99)); // Avoid Z-fighting
                meta.setPosRotInterpolationDuration(2);
                
                display.setNoGravity(true);
                display.setInstance(instance, currentPos.add(sb.relativePos())).join();
                
                // Only visible to the player (Minestom trick: use updateViewers with custom predicate or team)
                // For now, visible to everyone but we could optimize
                displayEntities.add(display);
            }

            // Start update task
            this.updateTask = MinecraftServer.getSchedulerManager().submitTask(() -> {
                if (!player.isOnline() || player.getInstance() != instance) {
                    cancelPreview(player);
                    return TaskSchedule.stop();
                }

                updatePosition();
                return TaskSchedule.tick(1);
            });
        }

        private void updatePosition() {
            Point target = player.getTargetBlockPosition(10);
            if (target == null) {
                // If looking at air, project at a fixed distance
                target = player.getPosition().add(player.getPosition().direction().mul(5));
            } else {
                target = target.add(0, 1, 0); // Place on top of pointed block
            }

            Pos newPos = new Pos(target.blockX(), target.blockY(), target.blockZ());
            if (newPos.equals(currentPos)) return;

            currentPos = newPos;
            for (int i = 0; i < displayEntities.size(); i++) {
                Entity e = displayEntities.get(i);
                e.teleport(currentPos.add(blocks.get(i).relativePos()));
            }
            
            player.sendActionBar(Component.text("Position: " + currentPos.blockX() + ", " + currentPos.blockY() + ", " + currentPos.blockZ(), NamedTextColor.YELLOW));
        }

        public Pos getCurrentPos() {
            return currentPos;
        }

        public void cleanup() {
            if (updateTask != null) updateTask.cancel();
            for (Entity e : displayEntities) {
                e.remove();
            }
            displayEntities.clear();
        }
    }
}
