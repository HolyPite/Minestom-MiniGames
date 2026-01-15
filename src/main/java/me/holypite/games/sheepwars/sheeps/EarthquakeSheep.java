package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class EarthquakeSheep extends SheepProjectile {

    public EarthquakeSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(3);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.RED); // Dark Red
            meta.setCustomName(Component.text("Earthquake Sheep", TextColor.fromHexString("#8B0000")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        AtomicInteger ticks = new AtomicInteger(0);
        int maxTicks = 3 * 20;

        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();

            if (ticks.getAndAdd(20) >= maxTicks) {
                // Final Explosion
                if (explosionManager != null) {
                    explosionManager.explode(getInstance(), getPosition(), 1.0f, true, shooter, this);
                } else {
                    getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), 1.0f, null);
                }
                remove();
                return TaskSchedule.stop();
            }

            // Quake Effect
            double radius = 5.0;
            
            // 1. Toss Players
            List<Player> players = TKit.getPlayersInRadius(getInstance(), getPosition(), radius, true);
            for (Player p : players) {
                Vec randomDir = new Vec(
                        ThreadLocalRandom.current().nextDouble(-0.5, 0.5),
                        0.8,
                        ThreadLocalRandom.current().nextDouble(-0.5, 0.5)
                );
                p.setVelocity(randomDir.mul(20)); // Adjust power
            }

            // 2. Break Blocks
            List<Point> blocks = TKit.getBlocksInSphere(getPosition(), radius);
            for (Point pos : blocks) {
                if (TKit.chance(0.2)) {
                    getInstance().setBlock(pos, Block.AIR);
                }
            }

            // 3. Particles
            net.minestom.server.particle.Particle blockParticle = net.minestom.server.particle.Particle.BLOCK.withBlock(Block.DIRT);
            
            TKit.spawnParticles(getInstance(), blockParticle, getPosition(), (float) radius/2, 0.5f, (float) radius/2, 0.1f, 100);

            return TaskSchedule.seconds(1);
        });
    }

    @Override
    public String getId() {
        return "earthquake";
    }
}
