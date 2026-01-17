package me.holypite.games.sheepwars.sheeps;

import me.holypite.entity.AggressiveBee;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;

public class HoneySheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 3;
    private static final double RADIUS = 5.0;
    private static final double HONEY_CHANCE = 0.7;
    private static final int BEE_COUNT = 2;

    public HoneySheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.ORANGE);
            meta.setCustomName(Component.text("Honey Sheep", TextColor.fromHexString("#FFD700")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        activate();
    }

    private void activate() {
        if (isRemoved()) return;

        
        // 1. Honey Blocks
        for (Point pos : TKit.getBlocksInSphere(getPosition(), RADIUS)) {
            Block block = getInstance().getBlock(pos);
            if (block.isSolid() && TKit.chance(HONEY_CHANCE)) {
                getInstance().setBlock(pos, Block.HONEY_BLOCK);
            }
        }

        // 2. Spawn Bees
        for (int i = 0; i < BEE_COUNT; i++) {
            AggressiveBee bee = new AggressiveBee();
            bee.setInstance(getInstance(), getPosition().add(0, 1, 0));
        }

        remove();
    }

    @Override
    public String getId() {
        return "honey";
    }
}
