package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class TrappedSheep extends SheepProjectile {

    public TrappedSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.PINK);
            meta.setCustomName(Component.text("Mouton Piégé", TextColor.color(0xFFC0CB)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();
            if (getAliveTicks() > 20 * 15) { // 15 seconds
                remove();
                return TaskSchedule.stop();
            }
            
            // Check players nearby
            boolean playerNearby = !TKit.getPlayersInRadius(getInstance(), getPosition(), 2.5, true).isEmpty();
            
            if (playerNearby) {
                getInstance().explode((float)getPosition().x(), (float)getPosition().y(), (float)getPosition().z(), 3f, null);
                remove();
                return TaskSchedule.stop();
            }
            
            return TaskSchedule.tick(5);
        });
    }

    @Override
    public String getId() {
        return "trapped";
    }
}