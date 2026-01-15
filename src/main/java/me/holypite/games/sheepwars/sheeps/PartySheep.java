package me.holypite.games.sheepwars.sheeps;

import me.holypite.games.sheepwars.SheepRegistry;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class PartySheep extends SheepProjectile {

    public PartySheep(Entity shooter) {
        super(shooter);
        setActivationDelay(0);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.MAGENTA);
            meta.setCustomName(Component.text("Party Sheep!!!", TextColor.fromHexString("#FF00FF")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        AtomicInteger cycles = new AtomicInteger(0);
        int maxCycles = 15; // 3 seconds approx (if tick=4)

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();
            
            // 1. Change wool blocks around
            double radius = 4.0;
            List<Point> blocks = TKit.getBlocksInSphere(getPosition(), radius);
            for (Point pos : blocks) {
                Block block = getInstance().getBlock(pos);
                if (block.isSolid() && getInstance().getBlock(pos.add(0, 1, 0)).isAir()) {
                    getInstance().setBlock(pos, getRandomWool());
                }
            }

            if (cycles.getAndIncrement() >= maxCycles) {
                // 2. Random Sheeps Spawn
                int numSheep = ThreadLocalRandom.current().nextInt(3, 6);
                double step = 2 * Math.PI / numSheep;
                
                // Blacklist problematic sheeps for Party Sheep
                List<String> blacklist = List.of("mystery", "party", "clone", "glutton", "instant");
                long elapsed = game != null ? game.getElapsedSeconds() : 0;
                
                for (int i = 0; i < numSheep; i++) {
                    Function<Entity, SheepProjectile> factory = SheepRegistry.getRandomSheepFactory(elapsed, blacklist);
                    if (factory != null) {
                        double theta = i * step;
                        double r = ThreadLocalRandom.current().nextBoolean() ? 2 : 3;
                        Point spawnPos = getPosition().add(r * Math.cos(theta), 0, r * Math.sin(theta));
                        
                        SheepProjectile s = factory.apply(shooter);
                        s.setInstance(getInstance(), spawnPos);
                        s.onLand(); // Activate them!
                    }
                }
                
                remove();
                return TaskSchedule.stop();
            }
            
            return TaskSchedule.tick(4);
        });
    }

    private Block getRandomWool() {
        Block[] wools = {Block.WHITE_WOOL, Block.ORANGE_WOOL, Block.MAGENTA_WOOL, Block.LIGHT_BLUE_WOOL, 
                         Block.YELLOW_WOOL, Block.LIME_WOOL, Block.PINK_WOOL, Block.GRAY_WOOL, 
                         Block.LIGHT_GRAY_WOOL, Block.CYAN_WOOL, Block.PURPLE_WOOL, Block.BLUE_WOOL, 
                         Block.BROWN_WOOL, Block.GREEN_WOOL, Block.RED_WOOL, Block.BLACK_WOOL};
        return wools[ThreadLocalRandom.current().nextInt(wools.length)];
    }

    @Override
    public String getId() {
        return "party";
    }
}