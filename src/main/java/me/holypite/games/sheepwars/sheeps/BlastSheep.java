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

    public BlastSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(1);
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
                
                TKit.getPlayersInRadius(getInstance(), getPosition(), 10, true).forEach(p -> {
                    getInstance().explode((float)p.getPosition().x(), (float)p.getPosition().y(), (float)p.getPosition().z(), 0.5f, null);
                });
                
                if (getAliveTicks() > 20 * 4) { // Timeout
                    remove();
                    return TaskSchedule.stop();
                }
                
                return TaskSchedule.tick(20); // Delay between blasts
            });
            
            // Auto remove after bursts (approx 3 * 20 = 60 ticks)
            MinecraftServer.getSchedulerManager().buildTask(this::remove).delay(TaskSchedule.tick(80)).schedule();
    }

    @Override
    public String getId() {
        return "blast";
    }
}