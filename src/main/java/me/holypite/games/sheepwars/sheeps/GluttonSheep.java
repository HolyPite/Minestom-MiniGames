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
        // Make it slow
        Vec velocity = getVelocity();
        double mul = 5;
        setVelocity(velocity.normalize().mul(mul));
        setNoGravity(true);
        
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();
            if (getAliveTicks() > 20 * 8) {
                remove();
                return TaskSchedule.stop();
            }
            
            // Eat blocks
            TKit.getBlocksInSphere(getPosition(), 2.5).forEach(p -> {
                 if (!getInstance().getBlock(p).isAir()) {
                     getInstance().setBlock(p, Block.AIR);
                     TKit.spawnParticles(getInstance(), Particle.POOF, p.add(0.5, 0.5, 0.5), 0, 0, 0, 0f, 3);
                 }
            });

            setVelocity(velocity.normalize().mul(mul));
            
            return TaskSchedule.tick(1);
        });
    }

    @Override
    public void onLand() {
        getInstance().explode((float)getPosition().x(), (float)getPosition().y(), (float)getPosition().z(), 3f, null);
    }

    @Override
    public String getId() {
        return "glutton";
    }
}