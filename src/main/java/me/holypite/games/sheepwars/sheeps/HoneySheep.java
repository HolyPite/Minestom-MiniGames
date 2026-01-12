package me.holypite.games.sheepwars.sheeps;

import me.holypite.games.sheepwars.sheeps.entities. aggressive.CombatBee;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class HoneySheep extends SheepProjectile {

    public HoneySheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.ORANGE);
            meta.setCustomName(Component.text("Honey Sheep", TextColor.fromHexString("#FFD700")));
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
        
        // 1. Honey Blocks
        for (Point pos : TKit.getBlocksInSphere(getPosition(), radius)) {
            Block block = getInstance().getBlock(pos);
            if (block.isSolid() && TKit.chance(0.7)) {
                getInstance().setBlock(pos, Block.HONEY_BLOCK);
            }
        }

        // 2. Spawn Bees
        for (int i = 0; i < 2; i++) {
            CombatBee bee = new CombatBee();
            bee.setInstance(getInstance(), getPosition().add(0, 1, 0));
        }

        remove();
    }

    @Override
    public String getId() {
        return "honey";
    }
}