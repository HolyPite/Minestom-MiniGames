package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.atomic.AtomicInteger;

public class BlackHoleSheep extends SheepProjectile {

    public BlackHoleSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.BLACK);
            meta.setCustomName(Component.text("Black Hole Sheep", NamedTextColor.BLACK));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        setNoGravity(true);
        teleport(getPosition().add(0, 1.5, 0)); // Lift off ground immediately
        setVelocity(new Vec(0, 0.3, 0));

        AtomicInteger ticks = new AtomicInteger(0);
        int maxTicks = 5 * 20;

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();

            if (ticks.getAndAdd(1) >= maxTicks) {
                // End
                getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), 1.0f, null);
                remove();
                return TaskSchedule.stop();
            }

            setVelocity(new Vec(0, 0.8, 0));

            // Attraction Logic
            double radius = 8.0;
            TKit.getEntitiesInRadius(getInstance(), getPosition(), radius).stream()

                    .filter(e -> e != this)

                    .forEach(e -> {
                        Vec diff = getPosition().sub(e.getPosition()).asVec();

                        // Horizontal attraction
                        Vec horizontal = diff.withY(0).normalize().mul(0.4);

                        // Vertical lift (counteracting gravity)
                        Vec vertical = new Vec(0, diff.y(), 0).mul(0.8);

                        // Random jitter
                        double jitter = 0.5;
                        Vec randomVec = new Vec(
                                (Math.random() - 0.5) * jitter,
                                0,
                                (Math.random() - 0.5) * jitter
                        );
                        e.setVelocity(e.getVelocity().add(horizontal).add(vertical).add(randomVec));

                    });

            // Particles
            TKit.spawnParticles(getInstance(), Particle.PORTAL, getPosition().add(0, 0.5, 0), 0.5f, 0.5f, 0.5f, 0.5f, 10);

            return TaskSchedule.tick(1);
        });
    }

    @Override
    public String getId() {
        return "black_hole";
    }
}