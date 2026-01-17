package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;

import java.util.List;

public class IncendiarySheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 3;
    private static final float EXPLOSION_POWER = 2.0f;
    private static final double RADIUS = 5.0;
    private static final int FIRE_TICKS = 100;
    private static final double FIRE_CHANCE = 0.3;

    public IncendiarySheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.ORANGE);
            meta.setCustomName(Component.text("Mouton Incendiaire", TextColor.fromHexString("#FF4500")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        activate();
    }

    private void activate() {
        if (isRemoved()) return;

        // 1. Explosion (Visual + Fire) - Smaller than 3.0
        if (explosionManager != null) {
            explosionManager.explode(getInstance(), getPosition(), EXPLOSION_POWER, true, shooter, this);
        } else {
            getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), EXPLOSION_POWER, null);
        }

        // 2. Burn Entities
        TKit.getEntitiesInRadius(getInstance(), getPosition(), RADIUS).stream()
                .filter(e -> e instanceof LivingEntity)
                .forEach(e -> ((LivingEntity) e).setFireTicks(FIRE_TICKS)); // 5 seconds

        // 3. Set Fire (30% of blocks)
        List<Point> blocks = TKit.getBlocksInSphere(getPosition(), RADIUS);
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            Point above = pos.add(0, 1, 0);
            Block blockAbove = getInstance().getBlock(above);
            
            if (current.isSolid() && blockAbove.isAir()) {
                if (TKit.chance(FIRE_CHANCE)) { // 30% chance
                    getInstance().setBlock(above, Block.FIRE);
                }
            }
        }

        remove();
    }

    @Override
    public String getId() {
        return "incendiary";
    }
}