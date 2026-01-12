package me.holypite.games.sheepwars.sheeps;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class RadioactiveSheep extends SheepProjectile {

    public RadioactiveSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.LIME);
            meta.setCustomName(Component.text("Mouton Radioactif", TextColor.color(0xADFF2F)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Entity cloud = new Entity(EntityType.AREA_EFFECT_CLOUD);
            AreaEffectCloudMeta meta = (AreaEffectCloudMeta) cloud.getEntityMeta();
            meta.setRadius(3f);
            meta.setColor(0x00FF00); // Green
            
            cloud.setInstance(getInstance(), getPosition());
            
            // Manual effect ticker
            MinecraftServer.getSchedulerManager().submitTask(() -> {
                if (cloud.isRemoved()) return TaskSchedule.stop();
                
                cloud.getInstance().getEntities().stream()
                    .filter(e -> e.getPosition().distanceSquared(cloud.getPosition()) < 3*3)
                    .forEach(e -> {
                        if (e instanceof net.minestom.server.entity.LivingEntity living) {
                            living.addEffect(new Potion(PotionEffect.WITHER, (byte)1, 100));
                        }
                    });
                    
                return TaskSchedule.tick(10);
            });
            
            // Remove cloud after 10s
            MinecraftServer.getSchedulerManager().buildTask(cloud::remove).delay(TaskSchedule.seconds(10)).schedule();
            
            getInstance().sendGroupedPacket(new ParticlePacket(
                    Particle.WITCH,
                    getPosition(),
                    new Vec(0.5, 0.5, 0.5),
                    0f, 50
            ));
            
            remove();
        }).delay(TaskSchedule.seconds(1)).schedule();
    }

    @Override
    public String getId() {
        return "radioactive";
    }
}