package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class IcySheep extends SheepProjectile {

    public IcySheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.CYAN);
            meta.setCustomName(Component.text("Icy Sheep", TextColor.fromHexString("#87CEEB")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().buildTask(this::activate)
                .delay(TaskSchedule.seconds(3))
                .schedule();
    }

    private void activate() {
        if (isRemoved()) return;

        double radius = 5.0;
        List<Point> blocks = TKit.getBlocksInSphere(getPosition(), radius);
        
        // We need to iterate all blocks in radius, not just solids returned by getBlocksInSphere (if it filters)
        // TKit.getBlocksInSphere currently returns SOLID blocks only? Let's check TKit.
        // TKit implementation: if (block.getType().isSolid()) blocks.add(block);
        // So I need a new method or modify logic here.
        // I will implement manual loop here for full control.
        
        Point center = getPosition();
        int r = (int) Math.ceil(radius);
        
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    Point pos = center.add(x, y, z);
                    if (pos.distance(center) <= radius) {
                        Block current = getInstance().getBlock(pos);
                        
                        if (!current.isAir()) {
                            // Transform solid blocks
                            if (TKit.chance(0.7)) {
                                getInstance().setBlock(pos, TKit.chance(0.3) ? Block.POWDER_SNOW : Block.SNOW_BLOCK);
                            }
                        } else if (current.isAir()) {
                            // Add snow layer on ground
                            Block below = getInstance().getBlock(pos.sub(0, 1, 0));
                            if (below.isSolid() && TKit.chance(0.5)) {
                                getInstance().setBlock(pos, Block.SNOW);
                            }
                        }
                    }
                }
            }
        }

        remove();
    }
}
