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
        
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            if (current.isSolid()) {
                // 30% chance to be Powder Snow, else Snow Block
                if (TKit.chance(0.7)) {
                    getInstance().setBlock(pos, TKit.chance(0.3) ? Block.POWDER_SNOW : Block.SNOW_BLOCK);
                }
            }
        }

        remove();
    }
}
