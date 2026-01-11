package me.holypite.games.sheepwars.sheeps;

import me.holypite.games.sheepwars.sheeps.entities.aggressive.AggressiveEndermite;
import me.holypite.games.sheepwars.sheeps.entities.aggressive.AggressiveSilverfish;
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

public class ParasiteSheep extends SheepProjectile {

    public ParasiteSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.PURPLE);
            meta.setCustomName(Component.text("Parasite Sheep", TextColor.fromHexString("#4B0082")));
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
        
        // 1. Infested Blocks
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            if (current.isSolid()) {
                if (TKit.chance(0.2)) {
                    getInstance().setBlock(pos, Block.INFESTED_STONE);
                }
            }
        }

        // 2. Spawn Creatures
        AggressiveEndermite endermite = new AggressiveEndermite();
        endermite.setInstance(getInstance(), getPosition().add(0, 1, 0));
        
        AggressiveSilverfish silverfish = new AggressiveSilverfish();
        silverfish.setInstance(getInstance(), getPosition().add(0, 1, 0));

        remove();
    }
}
