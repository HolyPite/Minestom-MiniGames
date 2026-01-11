package me.holypite.games.sheepwars.sheeps;

import me.holypite.entity.AggressiveSlime;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class StickySheep extends SheepProjectile {

    public StickySheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.LIME);
            meta.setCustomName(Component.text("Sticky Sheep", TextColor.fromHexString("#32CD32")));
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
        
        // 1. Transform blocks
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            if (current.isSolid()) {
                if (TKit.chance(0.7)) {
                    getInstance().setBlock(pos, Block.SLIME_BLOCK);
                }
            }
        }

        // 2. Spawn Slimes
        for (int i = 0; i < 3; i++) {
            AggressiveSlime slime = new AggressiveSlime();
            if (slime.getEntityMeta() instanceof SlimeMeta meta) {
                meta.setSize(TKit.chance(0.7) ? 1 : 2);
            }
            slime.setInstance(getInstance(), getPosition());
        }

        remove();
    }
}
