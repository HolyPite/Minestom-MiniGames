package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class HealSheep extends SheepProjectile {

    public HealSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.PINK);
            meta.setCustomName(Component.text("Mouton Soin", TextColor.color(0xFF00AC)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
             if (isRemoved()) return TaskSchedule.stop();
             if (getAliveTicks() > 20 * 5) { // 5 seconds duration
                 remove();
                 return TaskSchedule.stop();
             }
             
             // Heal players every second (20 ticks)
             if (getAliveTicks() % 20 == 0) {
                 TKit.getPlayersInRadius(getInstance(), getPosition(), 8, true).forEach(p -> {
                     p.addEffect(new Potion(PotionEffect.INSTANT_HEALTH, (byte)0, 1));
                 });
                 
                 getInstance().sendGroupedPacket(new ParticlePacket(
                         Particle.HEART,
                         getPosition().add(0, 1, 0),
                         new Vec(1, 1, 1),
                         0f, 5
                 ));
             }

             return TaskSchedule.tick(1);
        });
    }

    @Override
    public String getId() {
        return "heal";
    }
}