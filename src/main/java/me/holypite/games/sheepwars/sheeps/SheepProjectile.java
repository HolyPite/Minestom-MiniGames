package me.holypite.games.sheepwars.sheeps;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import org.jetbrains.annotations.NotNull;

public abstract class SheepProjectile extends EntityCreature {

    protected boolean landed = false;
    protected final Entity shooter;

    public SheepProjectile(Entity shooter) {
        super(EntityType.SHEEP);
        this.shooter = shooter;
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setHasNoGravity(false); 
        }
    }
    
    public void shoot(double power) {
        if (shooter.getInstance() == null) return;
        
        // Spawn at eye height
        Point spawnPos = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        this.setInstance(shooter.getInstance(), spawnPos);
        
        // Velocity
        Vec direction = shooter.getPosition().direction();
        this.setVelocity(direction.mul(power * 20)); // Minestom velocity is approx blocks/sec ? Let's try raw power first.
        // Actually Minestom/Minecraft velocity is often in blocks/tick for packets, but setVelocity takes server units.
        // Standard launch power is usually around 1.0 - 3.0.
        // If we multiply by 20, it might be huge. Let's try power * 1.5 first (similar to snowball).
        this.setVelocity(direction.mul(power * 25)); 
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        // Flight Hook
        if (!landed) {
            onFlightTick();
        }
        
        // Check Landing
        // isOnGround() detects floor.
        // Collided with wall? Velocity drops to 0.
        
        if (!landed) {
            if (isOnGround() || (getVelocity().length() < 0.1 && getAliveTicks() > 5)) { // Grace period for launch
                landed = true;
                onLand();
                // We don't remove, we just trigger land logic. 
                // Subclasses can remove themselves or start countdown.
                // Stop movement
                setVelocity(Vec.ZERO);
            }
        }
    }

    public abstract void onLand();
    
    protected void onFlightTick() {}
}
