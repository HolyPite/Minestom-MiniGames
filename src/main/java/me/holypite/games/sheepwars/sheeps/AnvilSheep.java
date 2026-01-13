package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

public class AnvilSheep extends SheepProjectile {

    public AnvilSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.GRAY);
            meta.setCustomName(Component.text("Mouton Enclume", TextColor.color(0x696969)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (getInstance() == null) return;
            
            List<Point> blocks = TKit.getBlocksInSphere(getPosition(), 6);
            
            for (Point p : blocks) {
                if (TKit.chance(0.1) && !getInstance().getBlock(p).isAir() && getInstance().getBlock(p.add(0, 1, 0)).isAir()) {
                    Entity anvil = new Entity(EntityType.FALLING_BLOCK);
                    FallingBlockMeta meta = (FallingBlockMeta) anvil.getEntityMeta();
                    meta.setBlock(Block.ANVIL);
                    meta.setSpawnPosition(p.add(0, 10, 0)); // Start high
                    
                    anvil.setInstance(getInstance(), p.add(0.5, 10, 0.5));
                    
                    // Add particles at spawn
                    getInstance().sendGroupedPacket(new ParticlePacket(
                            Particle.CLOUD,
                            anvil.getPosition(),
                            new Vec(0, 0, 0),
                            0f, 5
                    ));
                    
                    // Damage and Landing Logic
                    MinecraftServer.getSchedulerManager().submitTask(() -> {
                        if (anvil.isRemoved()) return TaskSchedule.stop();
                        
                        // Check entity collision
                        for (Entity target : anvil.getInstance().getEntities()) {
                            if (target instanceof net.minestom.server.entity.LivingEntity living && target != anvil) {
                                if (anvil.getBoundingBox().intersectEntity(anvil.getPosition(), target)) {
                                    living.damage(me.holypite.manager.damage.DamageSources.generic(10.0f));
                                    anvil.remove();
                                    return TaskSchedule.stop();
                                }
                            }
                        }
                        
                        if (anvil.isOnGround()) {
                            anvil.getInstance().setBlock(anvil.getPosition(), Block.ANVIL);
                            anvil.remove();
                            return TaskSchedule.stop();
                        }
                        return TaskSchedule.tick(1);
                    });
                }
            }
            
            remove();
        }).delay(TaskSchedule.seconds(1)).schedule();
    }

    @Override
    public String getId() {
        return "anvil";
    }
}