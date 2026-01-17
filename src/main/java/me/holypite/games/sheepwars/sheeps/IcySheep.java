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

    private static final float ACTIVATION_DELAY = 3;
    private static final double RADIUS = 5.0;
    
    // Probabilities (Cumulative)
    private static final double CHANCE_SNOW_BLOCK = 0.80;
    private static final double CHANCE_ICE = 0.90;
    private static final double CHANCE_BLUE_ICE = 0.95;
    // Remainder is POWDER_SNOW
    
    private static final double CHANCE_SNOW_LAYER = 0.30;

    public IcySheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
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

        List<Point> blocks = TKit.getBlocksInSphere(getPosition(), RADIUS);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            
            // 1. Solid blocks transformation
            if (!current.isLiquid() && !current.isAir()) {
                double r = rnd.nextDouble();
                Block newBlock;
                if (r < CHANCE_SNOW_BLOCK) newBlock = Block.SNOW_BLOCK;
                else if (r < CHANCE_ICE) newBlock = Block.ICE;
                else if (r < CHANCE_BLUE_ICE) newBlock = Block.BLUE_ICE;
                else newBlock = Block.POWDER_SNOW;
                
                getInstance().setBlock(pos, newBlock);
                continue;
            }
            
            // 2. Snow Layer on top
            Block below = getInstance().getBlock(pos.sub(0, 1, 0));
            if (below.isSolid() && rnd.nextDouble() < CHANCE_SNOW_LAYER) {
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