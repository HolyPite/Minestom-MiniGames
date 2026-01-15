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

    public IncendiarySheep(Entity shooter) {
        super(shooter);
        setActivationDelay(3);
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
        float power = 2.0f;
        if (explosionManager != null) {
            explosionManager.explode(getInstance(), getPosition(), power, false, shooter, this);
        } else {
            getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), power, null);
        }

        double radius = 5.0;
        
        // 2. Burn Entities
        TKit.getEntitiesInRadius(getInstance(), getPosition(), radius).stream()
                .filter(e -> e instanceof LivingEntity)
                .forEach(e -> ((LivingEntity) e).setFireTicks(100)); // 5 seconds

        // 3. Set Fire (30% of blocks)
        List<Point> blocks = TKit.getBlocksInSphere(getPosition(), radius);
        for (Point pos : blocks) {
            Block current = getInstance().getBlock(pos);
            Point above = pos.add(0, 1, 0);
            Block blockAbove = getInstance().getBlock(above);
            
            if (current.isSolid() && blockAbove.isAir()) {
                if (TKit.chance(0.3)) { // 30% chance
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