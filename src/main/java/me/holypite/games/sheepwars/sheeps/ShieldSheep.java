package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.kyori.adventure.text.Component;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.format.TextColor;

public class ShieldSheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 0;
    private static final int DURATION_SECONDS = 5;
    private static final double PUSH_RADIUS = 5.0;
    private static final double PUSH_STRENGTH = 2.5;

    public ShieldSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY); // I suspect it should start instantly and REMOVE after 5s.
         if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.GRAY);
            meta.setCustomName(Component.text("Mouton Bouclier", TextColor.color(0x808080)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        // Active for 5 seconds
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();
            if (getAliveTicks() > 20 * (DURATION_SECONDS * 2)) { // Safety timeout (10s)
                 remove();
                 return TaskSchedule.stop();
            }
            
            // Push entities
             TKit.getEntitiesInRadius(getInstance(), getPosition(), PUSH_RADIUS).forEach(e -> {
                 if (e != this && !(e instanceof Player)) {
                     Vec dir = e.getPosition().sub(getPosition()).asVec().normalize().mul(PUSH_STRENGTH);
                     dir = dir.withY(0.5); // Add some lift
                     e.setVelocity(dir.mul(20)); // Minestom velocity scale
                 }
             });
             
             // Particles - Only every 10 ticks
             if (getAliveTicks() % 10 == 0) {
                TKit.spawnParticles(getInstance(), Particle.SONIC_BOOM, getPosition().add(0, 1, 0), 0, 0, 0, 0f, 1);
             }

             return TaskSchedule.tick(1);
        });
        
        // Remove after 5 seconds
        MinecraftServer.getSchedulerManager().buildTask(this::remove)
            .delay(TaskSchedule.seconds(DURATION_SECONDS))
            .schedule();
    }

    @Override
    public String getId() {
        return "shield";
    }
}