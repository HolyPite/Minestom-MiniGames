package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class SeekerSheep extends SheepProjectile {

    public SeekerSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.PURPLE);
            meta.setCustomName(Component.text("Mouton Chercheur", TextColor.color(0x8A2BE2)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        // Find target
        Player target = TKit.getNearestPlayer(getInstance(), getPosition(), 30, true);
        if (target != null) {
            
            MinecraftServer.getSchedulerManager().submitTask(() -> {
                 if (isRemoved()) return TaskSchedule.stop();
                 
                 if (getPosition().distanceSquared(target.getPosition()) < 2*2) {
                     // Boom
                     getInstance().explode((float)getPosition().x(), (float)getPosition().y(), (float)getPosition().z(), 3f, null);
                     remove();
                     return TaskSchedule.stop();
                 }
                 
                 this.getNavigator().setPathTo(target.getPosition());
                 
                 // If stuck or too long
                 if (getAliveTicks() > 20 * 10) {
                     remove();
                     return TaskSchedule.stop();
                 }
                 
                 return TaskSchedule.tick(10);
            });
        } else {
             // No target, explode immediately
             getInstance().explode((float)getPosition().x(), (float)getPosition().y(), (float)getPosition().z(), 3f, null);
             remove();
        }
    }

    @Override
    public String getId() {
        return "seeker";
    }
}