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

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class HedgehogSheep extends SheepProjectile {

    private static final double SPHERE_RADIUS = 1.0;
    private static final int ARROW_COUNT = 30;
    private static final int SHOOT_INTERVAL_TICKS = 1; // 0.05 seconds
    private static final double ARROW_SPEED = 15.0;

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
        AtomicInteger arrowsShot = new AtomicInteger(0);

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved() || getInstance() == null) {
                return TaskSchedule.stop();
            }

            if (arrowsShot.get() >= ARROW_COUNT) {
                remove();
                return TaskSchedule.stop();
            }

            shootSingleArrow();
            arrowsShot.incrementAndGet();

            return TaskSchedule.tick(SHOOT_INTERVAL_TICKS);
        });
    }

    private void shootSingleArrow() {
        // Random point on upper hemisphere
        double x = ThreadLocalRandom.current().nextDouble(-1, 1);
        double y = ThreadLocalRandom.current().nextDouble(0.1, 1); // Upwards
        double z = ThreadLocalRandom.current().nextDouble(-1, 1);
        Vec dir = new Vec(x, y, z).normalize();

        // Calculate spawn position
        Vec pos = getPosition().add(0, getEyeHeight(), 0).asVec().add(dir.mul(SPHERE_RADIUS));

        // Use shooter (player) for kill credit
        ArrowProjectile arrow = new ArrowProjectile(EntityType.ARROW, shooter);

        // Calculate rotation to point outward
        float yaw = (float) -Math.toDegrees(Math.atan2(dir.x(), dir.z()));
        float pitch = (float) -Math.toDegrees(Math.atan2(dir.y(), Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z())));

        // Spawn with correct rotation
        arrow.setInstance(getInstance(), new net.minestom.server.coordinate.Pos(pos.x(), pos.y(), pos.z(), yaw, pitch));
        
        // Launch immediately
        arrow.setVelocity(dir.mul(ARROW_SPEED));
    }

    @Override
    public String getId() {
        return "hedgehog";
    }
}