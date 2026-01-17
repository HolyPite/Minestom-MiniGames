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

    private static final double SPEED_MULTIPLIER = 5.0;
    private static final int LIFETIME_TICKS = 20 * 8;
    private static final double EAT_RADIUS = 2.5;
    private static final float EXPLOSION_POWER = 3.0f;

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
        setVelocity(velocity.normalize().mul(SPEED_MULTIPLIER));
        setNoGravity(true);
        
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();
            if (getAliveTicks() > LIFETIME_TICKS) {
                remove();
                return TaskSchedule.stop();
            }
            
            // Eat blocks
            TKit.getBlocksInSphere(getPosition(), EAT_RADIUS).forEach(p -> {
                 if (!getInstance().getBlock(p).isAir()) {
                     getInstance().setBlock(p, Block.AIR);
                     TKit.spawnParticles(getInstance(), Particle.POOF, p.add(0.5, 0.5, 0.5), 0, 0, 0, 0f, 3);
                 }
            });

            setVelocity(velocity.normalize().mul(SPEED_MULTIPLIER));
            
            return TaskSchedule.tick(1);
        });
    }

    @Override
    public void onLand() {
        getInstance().explode((float)getPosition().x(), (float)getPosition().y(), (float)getPosition().z(), EXPLOSION_POWER, null);
    }

    @Override
    public String getId() {
        return "glutton";
    }
}