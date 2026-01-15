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
import java.util.concurrent.ThreadLocalRandom;

public class IcySheep extends SheepProjectile {

    public IcySheep(Entity shooter) {
        super(shooter);
        setActivationDelay(3);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.CYAN);
            meta.setCustomName(Component.text("Icy Sheep", TextColor.fromHexString("#87CEEB")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        activate();
    }

    private void activate() {
        if (isRemoved()) return;

        double radius = 5.0;
        List<Point> blocks = TKit.getBlocksInSphere(getPosition(), radius);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            
            // 1. Solid blocks transformation
            if (!current.isLiquid() && !current.isAir()) {
                double r = rnd.nextDouble();
                Block newBlock;
                if (r < 0.80) newBlock = Block.SNOW_BLOCK;
                else if (r < 0.90) newBlock = Block.ICE;
                else if (r < 0.95) newBlock = Block.BLUE_ICE;
                else newBlock = Block.POWDER_SNOW;
                
                getInstance().setBlock(pos, newBlock);
                continue;
            }
            
            // 2. Snow Layer on top
            Block below = getInstance().getBlock(pos.sub(0, 1, 0));
            if (below.isSolid() && rnd.nextDouble() < 0.30) {
                getInstance().setBlock(pos, Block.SNOW);
            }
        }

        remove();
    }

    @Override
    public String getId() {
        return "icy";
    }
}