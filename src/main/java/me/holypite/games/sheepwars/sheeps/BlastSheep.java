package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class BlastSheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 1;
    private static final double BLAST_RADIUS = 10.0;
    private static final float BLAST_POWER = 0.4f;
    private static final int LIFETIME_TICKS = 20 * 4;
    private static final int BLAST_INTERVAL_TICKS = 20;

    public BlastSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.ORANGE);
            meta.setCustomName(Component.text("Mouton Déflagration", TextColor.color(0xFFD700)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
            MinecraftServer.getSchedulerManager().submitTask(() -> {
                if (isRemoved()) return TaskSchedule.stop();
                
                TKit.getPlayersInRadius(getInstance(), getPosition(), BLAST_RADIUS, true).forEach(p -> {
                    if (explosionManager != null) {
                        explosionManager.explode(getInstance(), p.getPosition(), BLAST_POWER, true, shooter, this);
                    } else {
                        getInstance().explode((float)p.getPosition().x(), (float)p.getPosition().y(), (float)p.getPosition().z(), BLAST_POWER, null);
                    }
                });
                
                if (getAliveTicks() > LIFETIME_TICKS) { // Timeout
                    remove();
                    return TaskSchedule.stop();
                }
                
                return TaskSchedule.tick(BLAST_INTERVAL_TICKS); // Delay between blasts
            });
            
            // Auto remove after bursts (approx 3 * 20 = 60 ticks)
            MinecraftServer.getSchedulerManager().buildTask(this::remove).delay(TaskSchedule.tick(LIFETIME_TICKS)).schedule();
    }

    @Override
    public String getId() {
        return "blast";
    }
}