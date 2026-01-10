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
        setVelocity(new Vec(0, 0.1, 0)); // Levitate a bit initially

        AtomicInteger ticks = new AtomicInteger(0);
        int maxTicks = 5 * 20;

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();

            if (ticks.getAndAdd(2) >= maxTicks) {
                // End
                getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), 2.0f, null);
                remove();
                return TaskSchedule.stop();
            }

            // Attraction Logic
            double radius = 8.0;
            TKit.getEntitiesInRadius(getInstance(), getPosition(), radius).stream()
                    .filter(e -> e != this)
                    .forEach(e -> {
                        Vec direction = getPosition().sub(e.getPosition()).asVec().normalize().mul(0.3);
                        e.setVelocity(e.getVelocity().add(direction));
                    });

            // Particles
            getInstance().sendGroupedPacket(new net.minestom.server.network.packet.server.play.ParticlePacket(
                    Particle.PORTAL,
                    getPosition().add(0, 0.5, 0),
                    new Vec(0.5, 0.5, 0.5),
                    0.5f, 10
            ));

            return TaskSchedule.tick(2);
        });
    }
}
