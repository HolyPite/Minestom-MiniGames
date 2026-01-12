package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class IncendiarySheep extends SheepProjectile {

    public IncendiarySheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.ORANGE);
            meta.setCustomName(Component.text("Incendiary Sheep", TextColor.fromHexString("#FF4500")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().buildTask(this::activate)
                .delay(TaskSchedule.seconds(3))
                .schedule();
    }

    private void activate() {
        if (isRemoved()) return;

        double radius = 5.0;
        List<Point> blocks = TKit.getBlocksInSphere(getPosition(), radius);
        
        // 1. Set Fire
        for (Point pos : blocks) {
            Block block = getInstance().getBlock(pos);
            Point above = pos.add(0, 1, 0);
            Block blockAbove = getInstance().getBlock(above);
            
            if (block.isSolid() && blockAbove.isAir()) {
                if (TKit.chance(0.7)) {
                    getInstance().setBlock(above, Block.FIRE);
                }
            }
        }

        // 2. Burn Entities
        TKit.getEntitiesInRadius(getInstance(), getPosition(), radius).stream()
                .filter(e -> e instanceof LivingEntity)
                .forEach(e -> ((LivingEntity) e).setFireTicks(100)); // 5 seconds

        // 3. Explosion (Visual + Fire)
        // Note: Minestom explosion doesn't set fire by default unless implemented in GameExplosion.
        // My GameExplosion has a 'fire' boolean field but logic isn't implemented for fire spreading yet.
        // But since I manually set fire above, a visual explosion is enough.
        
        getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), 2.0f, null);

        remove();
    }

    @Override
    public String getId() {
        return "incendiary";
    }
}
