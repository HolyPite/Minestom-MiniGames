package me.holypite.games.sheepwars.sheeps;

import me.holypite.entity.AggressiveSlime;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;

import java.util.List;

public class StickySheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 3;
    private static final double RADIUS = 5.0;
    private static final double SLIME_BLOCK_CHANCE = 0.7;
    private static final int SLIME_COUNT = 3;
    private static final double SMALL_SLIME_CHANCE = 0.7;

    public StickySheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.LIME);
            meta.setCustomName(Component.text("Sticky Sheep", TextColor.fromHexString("#32CD32")));
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
        
        // 1. Transform blocks
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            if (current.isSolid()) {
                if (TKit.chance(SLIME_BLOCK_CHANCE)) {
                    getInstance().setBlock(pos, Block.SLIME_BLOCK);
                }
            }
        }

        // 2. Spawn Slimes
        for (int i = 0; i < SLIME_COUNT; i++) {
            AggressiveSlime slime = new AggressiveSlime(TKit.chance(SMALL_SLIME_CHANCE) ? 1 : 2);
            slime.setInstance(getInstance(), getPosition());
        }

        remove();
    }

    @Override
    public String getId() {
        return "sticky";
    }
}