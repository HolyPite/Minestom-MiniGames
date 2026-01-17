package me.holypite.games.sheepwars.sheeps;

import me.holypite.manager.projectile.entities.MeteorProjectile;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.color.DyeColor;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.DimensionType;

import java.util.concurrent.ThreadLocalRandom;

public class ApocalypseSheep extends SheepProjectile {

    private long previousTime = 6000;

    public ApocalypseSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.BLACK); // Dark sheep
            meta.setCustomName(Component.text("Mouton Apocalypse", NamedTextColor.DARK_RED));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        if (isRemoved()) return;

        Instance instance = getInstance();
        if (instance != null) {
            // Set Night
            this.previousTime = instance.getTime();
            instance.setTime(18000);
            
            // Sound Effect
            TKit.playSound(instance, getPosition(), "entity.wither.spawn", net.kyori.adventure.sound.Sound.Source.HOSTILE, 1.0f, 0.5f);
            TKit.messageNearestPlayer(instance, getPosition(), "§4§lL'APOCALYPSE COMMENCE !");

            // Rain of Meteors (10 seconds)
            Point center = getPosition();
            int[] waves = {0};
            
            MinecraftServer.getSchedulerManager().submitTask(() -> {
                if (instance.getPlayers().isEmpty()) return TaskSchedule.stop();
                if (waves[0] >= 20) return TaskSchedule.stop(); // 10s total (20 * 0.5s)
                
                // Spawn Meteor
                spawnMeteor(instance, center);
                waves[0]++;
                
                return TaskSchedule.tick(10); // Every 0.5s
            });
            
            // Restore time after 10s
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                instance.setTime(previousTime);
            }).delay(TaskSchedule.seconds(10)).schedule();
        }

        remove();
    }
    
    private void spawnMeteor(Instance instance, Point center) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        
        // Random position in 20 block radius
        double angle = rng.nextDouble() * 2 * Math.PI;
        double radius = rng.nextDouble() * 20;
        double x = center.x() + radius * Math.cos(angle);
        double z = center.z() + radius * Math.sin(angle);
        double y = center.y() + 30;
        
        Pos spawnPos = new Pos(x, y, z);
        Pos targetPos = new Pos(x + rng.nextGaussian() * 5, center.y(), z + rng.nextGaussian() * 5); // Target ground roughly below
        
        MeteorProjectile meteor = new MeteorProjectile(shooter);
        meteor.setExplosionManager(explosionManager);
        meteor.shoot(spawnPos, targetPos, 1.5, 0); // Speed 1.5
    }

    @Override
    public String getId() {
        return "apocalypse";
    }
}
