package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class GluttonSheep extends SheepProjectile {

    public GluttonSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.GREEN);
            meta.setCustomName(Component.text("Mouton Glouton", TextColor.color(0x0d3d1b)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void shoot(double power) {
        super.shoot(power);
        setNoGravity(true);
        
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();
            if (getAliveTicks() > 20 * 8) {
                remove();
                return TaskSchedule.stop();
            }
            
            // Eat blocks
            TKit.getBlocksInSphere(getPosition(), 2.5).forEach(p -> {
                 if (getInstance().getBlock(p).isSolid()) {
                     getInstance().setBlock(p, Block.AIR);
                     
                     // Minestom doesn't support BLOCK particle data easily in this packet wrapper without data arg?
                     // Actually ParticlePacket has data field but it's not exposed in simple constructor?
                     // Or pass data via specialized packet constructor.
                     // For simplicity, use generic particle or check how to pass block state.
                     // Particle.BLOCK needs block state.
                     // Let's use POOF for now to avoid complex packet construction if TKit doesn't help.
                     getInstance().sendGroupedPacket(new ParticlePacket(
                             Particle.POOF,
                             p.add(0.5, 0.5, 0.5),
                             new Vec(0, 0, 0),
                             0f, 3
                     ));
                 }
            });
            
            Vec vel = getVelocity();
            if (vel.length() < 10) { 
                 setVelocity(vel.normalize().mul(20));
            }
            
            return TaskSchedule.tick(1);
        });
    }

    @Override
    public void onLand() {
        getInstance().explode((float)getPosition().x(), (float)getPosition().y(), (float)getPosition().z(), 3f, null);
        remove();
    }

    @Override
    public String getId() {
        return "glutton";
    }
}