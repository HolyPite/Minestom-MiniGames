package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IslandSheep extends SheepProjectile {

    private static final double RADIUS = 5.0;
    private static final double TELEPORT_HEIGHT = 20.0;

    public IslandSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(0.5f); // Short delay to see it land
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.LIME);
            meta.setCustomName(Component.text("Mouton Island", NamedTextColor.GREEN));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        triggerIsland();
        remove();
    }

    private void triggerIsland() {
        Instance instance = getInstance();
        if (instance == null) return;
        Point center = getPosition();

        // 1. Teleport Entities
        List<Entity> entities = TKit.getEntitiesInRadius(instance, center, RADIUS);
        for (Entity entity : entities) {
            if (entity == this) continue;
            entity.teleport(entity.getPosition().add(0, TELEPORT_HEIGHT, 0));
        }

        // 2. Move Blocks
        List<Point> blockPositions = TKit.getBlocksInSphere(center, RADIUS);
        Map<Point, Block> blocksToMove = new HashMap<>();

        // Capture blocks
        for (Point pos : blockPositions) {
            Block block = instance.getBlock(pos);
            if (!block.isAir()) {
                blocksToMove.put(pos, block);
            }
        }

        // Clear old blocks (Set to Air)
        for (Point pos : blocksToMove.keySet()) {
            instance.setBlock(pos, Block.AIR);
        }

        // Set new blocks (Shifted Up)
        for (Map.Entry<Point, Block> entry : blocksToMove.entrySet()) {
            Point newPos = entry.getKey().add(0, TELEPORT_HEIGHT, 0);
            instance.setBlock(newPos, entry.getValue());
        }
    }

    @Override
    public String getId() {
        return "island";
    }
}
