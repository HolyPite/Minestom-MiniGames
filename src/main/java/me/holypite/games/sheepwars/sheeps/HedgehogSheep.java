package me.holypite.games.sheepwars.sheeps;

import me.holypite.manager.projectile.entities.ArrowProjectile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HedgehogSheep extends SheepProjectile {

    public HedgehogSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.BROWN);
            meta.setCustomName(Component.text("Mouton Hérisson", TextColor.fromHexString("#B8860B")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        if (isRemoved()) return;

        List<HedgehogArrow> arrows = new ArrayList<>();
        double radius = 1.5;
        int arrowCount = 20;

        for (int i = 0; i < arrowCount; i++) {
            // Random point on upper hemisphere
            double x = ThreadLocalRandom.current().nextDouble(-1, 1);
            double y = ThreadLocalRandom.current().nextDouble(0.1, 1); // Upwards
            double z = ThreadLocalRandom.current().nextDouble(-1, 1);
            Vec dir = new Vec(x, y, z).normalize();
            
            // Calculate spawn position
            Vec pos = getPosition().add(0, getEyeHeight(), 0).asVec().add(dir.mul(radius));
            
            // Use shooter (player) for kill credit
            HedgehogArrow arrow = new HedgehogArrow(shooter); 
            arrow.setInstance(getInstance(), pos.asPosition());
            
            // Calculate rotation to point outward
            float yaw = (float) -Math.toDegrees(Math.atan2(dir.x(), dir.z()));
            float pitch = (float) -Math.toDegrees(Math.atan2(dir.y(), Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z())));
            
            arrow.setView(yaw, pitch);
            
            arrows.add(arrow);
        }

        // Schedule launch after 1 second
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (HedgehogArrow arrow : arrows) {
                if (arrow.isRemoved()) continue;
                
                // Unfreeze and launch
                arrow.setFrozen(false);
                
                // Direction is where the arrow is looking
                Vec direction = arrow.getPosition().direction();
                arrow.setVelocity(direction.mul(40.0)); // High speed
            }
        }).delay(TaskSchedule.seconds(1)).schedule();
        
        remove();
    }

    @Override
    public String getId() {
        return "hedgehog";
    }

    private static class HedgehogArrow extends ArrowProjectile {
        private boolean frozen = true;

        public HedgehogArrow(Entity shooter) {
            super(EntityType.ARROW, shooter);
            setNoGravity(true);
        }

        public void setFrozen(boolean frozen) {
            this.frozen = frozen;
            setNoGravity(frozen);
        }

        @Override
        public void tick(long time) {
            if (frozen) {
                if (removed || inBlock) return;
                // Do not update position or physics while frozen
                return;
            }
            super.tick(time);
        }
    }
}
