package me.holypite.games.sheepwars.sheeps;

import me.holypite.manager.projectile.AbstractProjectile;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import net.minestom.server.ServerFlag;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.coordinate.Point;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class SheepProjectile extends AbstractProjectile {

    protected boolean landed = false;

    public SheepProjectile(Entity shooter) {
        super(EntityType.SHEEP, shooter);
        // Setup Sheep Metadata
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setHasNoGravity(false); 
        }
    }
    
    @Override
    public void shoot(@NotNull Point from, @NotNull Point to, double power, double spread) {
        var instance = shooter.getInstance();
        if (instance == null) return;

        float yaw = -shooter.getPosition().yaw();
        float originalPitch = -shooter.getPosition().pitch();
        float pitch = originalPitch - 35f;

        double pitchDiff = pitch - 45;
        if (pitchDiff == 0) pitchDiff = 0.0001;
        double pitchAdjust = pitchDiff * 0.002145329238474369D;

        double dx = to.x() - from.x();
        double dy = to.y() - from.y() + pitchAdjust;
        double dz = to.z() - from.z();
        // Gravity comp
        final double xzLength = Math.sqrt(dx * dx + dz * dz);
        dy += xzLength * 0.20000000298023224D;

        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx /= length;
        dy /= length;
        dz /= length;
        
        Random random = ThreadLocalRandom.current();
        spread *= 0.007499999832361937D;
        dx += random.nextGaussian() * spread;
        dy += random.nextGaussian() * spread;
        dz += random.nextGaussian() * spread;

        final EntityShootEvent shootEvent = new EntityShootEvent(this.shooter, this, from, power, spread);
        EventDispatcher.call(shootEvent);
        if (shootEvent.isCancelled()) {
            remove();
            return;
        }

        final double mul = ServerFlag.SERVER_TICKS_PER_SECOND * power;
        Vec v = new Vec(dx * mul, dy * mul * 0.9, dz * mul);

        // Position sync and velocity
        this.setInstance(instance, new Pos(from.x(), from.y(), from.z(), yaw, originalPitch)).whenComplete((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                synchronizePosition(); 
                setVelocity(v);
            }
        });
    }

    @Override
    public void tick(long time) {
        // Allow custom behavior during flight
        onFlightTick();
        super.tick(time);
    }

    protected void onFlightTick() {
        // Override for flight abilities
    }

    @Override
    protected void handleBlockCollision(Block hitBlock, net.minestom.server.coordinate.Point hitPos, Pos posBefore) {
        super.handleBlockCollision(hitBlock, hitPos, posBefore);
        if (!landed) {
            landed = true;
            onLand();
            // We usually remove the projectile entity after landing logic starts
            // But some sheeps might want to stay as this entity?
            // For now, let's assume we remove this projectile and spawn a "static" sheep AI if needed, 
            // OR we keep this entity and just change its behavior.
            // Keeping it is simpler if we don't need complex pathfinding immediately.
        }
    }

    @Override
    protected boolean handleEntityCollision(net.minestom.server.collision.EntityCollisionResult result, net.minestom.server.coordinate.Point hitPos, Pos posBefore) {
        // Impact with entity = Landed
        if (!landed) {
            landed = true;
            onLand();
        }
        return true; // Stop projectile
    }

    public abstract void onLand();

    // Standard projectile physics (copied from Arrow/Abstract default usually)
    @Override
    protected @NotNull Vec updateVelocity(@NotNull Pos entityPosition, @NotNull Vec currentVelocity, @NotNull Block.@NotNull Getter blockGetter, @NotNull Aerodynamics aerodynamics, boolean positionChanged, boolean entityFlying, boolean entityOnGround, boolean entityNoGravity) {
        // Basic arc physics
        if (!positionChanged) {
            return entityFlying ? Vec.ZERO : new Vec(0.0, -aerodynamics.gravity() * aerodynamics.verticalAirResistance(), 0.0);
        } else {
            double drag = 0.99; // Simple drag
            double gravity = aerodynamics.gravity();
            
            double x = currentVelocity.x() * drag;
            double y = (currentVelocity.y() - gravity) * drag;
            double z = currentVelocity.z() * drag;
            
            return new Vec(Math.abs(x) < 1.0E-6 ? 0.0 : x, Math.abs(y) < 1.0E-6 ? 0.0 : y, Math.abs(z) < 1.0E-6 ? 0.0 : z);
        }
    }
}
