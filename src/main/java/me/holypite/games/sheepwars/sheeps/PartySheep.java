package me.holypite.games.sheepwars.sheeps;

import me.holypite.games.sheepwars.SheepRegistry;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class PartySheep extends SheepProjectile {

    public PartySheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.MAGENTA);
            meta.setCustomName(Component.text("Party Sheep!!!", TextColor.fromHexString("#FF00FF")));
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

        // 1. Random Sheeps
        int numSheep = ThreadLocalRandom.current().nextInt(3, 8);
        for (int i = 0; i < numSheep; i++) {
            Function<Entity, SheepProjectile> factory = SheepRegistry.getRandomSheepFactory();
            if (factory != null) {
                SheepProjectile s = factory.apply(shooter);
                s.setInstance(getInstance(), getPosition().add(Math.random()-0.5, 1, Math.random()-0.5));
                s.onLand(); // Activate them!
            }
        }

        // 2. Change wool blocks around
        double radius = 3.0;
        List<Point> blocks = TKit.getBlocksInSphere(getPosition(), radius);
        for (Point pos : blocks) {
            Block block = getInstance().getBlock(pos);
            if (block.name().contains("wool")) {
                getInstance().setBlock(pos, getRandomWool());
            }
        }

        remove();
    }

    private Block getRandomWool() {
        Block[] wools = {Block.WHITE_WOOL, Block.ORANGE_WOOL, Block.MAGENTA_WOOL, Block.LIGHT_BLUE_WOOL, 
                         Block.YELLOW_WOOL, Block.LIME_WOOL, Block.PINK_WOOL, Block.GRAY_WOOL, 
                         Block.LIGHT_GRAY_WOOL, Block.CYAN_WOOL, Block.PURPLE_WOOL, Block.BLUE_WOOL, 
                         Block.BROWN_WOOL, Block.GREEN_WOOL, Block.RED_WOOL, Block.BLACK_WOOL};
        return wools[ThreadLocalRandom.current().nextInt(wools.length)];
    }
}
