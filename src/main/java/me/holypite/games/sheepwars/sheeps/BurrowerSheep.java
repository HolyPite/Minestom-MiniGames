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

public class BurrowerSheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 3;
    private static final double SEARCH_RADIUS = 5.0;
    private static final double TNT_CHANCE = 0.05;

    public BurrowerSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.BROWN);
            meta.setCustomName(Component.text("Burrower Sheep", TextColor.fromHexString("#8B4513")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        activate();
    }

    private void activate() {
        if (isRemoved()) return;

        List<Point> blocks = TKit.getBlocksInSphere(getPosition(), SEARCH_RADIUS);
        
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            if (current.isSolid()) {
                if (TKit.chance(TNT_CHANCE)) { // 5% chance
                    getInstance().setBlock(pos, Block.TNT);
                }
            }
        }

        remove();
    }

    @Override
    public String getId() {
        return "burrower";
    }
}