package me.holypite.games.sheepwars.sheeps;

import me.holypite.entity.AggressiveLarva;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;

import java.util.List;

public class ParasiteSheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 3;
    private static final double RADIUS = 5.0;
    private static final double INFEST_CHANCE = 0.2;

    public ParasiteSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.PURPLE);
            meta.setCustomName(Component.text("Parasite Sheep", TextColor.fromHexString("#4B0082")));
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
        
        // 1. Infested Blocks
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            if (current.isSolid()) {
                if (TKit.chance(INFEST_CHANCE)) {
                    getInstance().setBlock(pos, Block.INFESTED_STONE);
                }
            }
        }

        // 2. Spawn Creatures
        AggressiveLarva endermite = new AggressiveLarva(EntityType.ENDERMITE);
        endermite.setInstance(getInstance(), getPosition().add(0, 1, 0));
        
        AggressiveLarva silverfish = new AggressiveLarva(EntityType.SILVERFISH);
        silverfish.setInstance(getInstance(), getPosition().add(0, 1, 0));

        remove();
    }

    @Override
    public String getId() {
        return "parasite";
    }
}