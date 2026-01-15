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

    public HoneySheep(Entity shooter) {
        super(shooter);
        setActivationDelay(3);
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
