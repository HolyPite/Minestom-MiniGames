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
    protected float activationDelay = 0; // Seconds
    protected me.holypite.manager.explosion.ExplosionManager explosionManager;
    protected me.holypite.model.Game game;

    public SheepProjectile(Entity shooter) {
        super(EntityType.SHEEP);
        this.shooter = shooter;
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setHasNoGravity(false); 
        }
    }
    
    public void setGame(me.holypite.model.Game game) {
        this.game = game;
    }

    public void setExplosionManager(me.holypite.manager.explosion.ExplosionManager explosionManager) {
        this.explosionManager = explosionManager;
    }

    protected void setActivationDelay(float seconds) {
        this.activationDelay = seconds;
    }
    
    public void shoot(double power) {
        if (shooter.getInstance() == null) return;
        
        // Spawn at eye height
        Point spawnPos = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        this.setInstance(shooter.getInstance(), spawnPos);
        
        // Velocity
        Vec direction = shooter.getPosition().direction();
        this.setVelocity(direction.mul(power * 25)); 
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        // Lifetime Check (1 minute = 1200 ticks)
        if (getAliveTicks() > 1200) {
            remove();
            return;
        }

        // Void Check
        if (game != null && getPosition().y() < game.getVoidY()) {
            remove();
            return;
        }

        // Flight Hook
        if (!landed) {
            onFlightTick();
        }
        
        if (!landed) {
            if (isOnGround() || (getVelocity().length() < 0.1 && getAliveTicks() > 5)) { // Grace period for launch
                landed = true;
                setVelocity(Vec.ZERO);
                
                if (activationDelay > 0) {
                     net.minestom.server.MinecraftServer.getSchedulerManager().buildTask(this::onLand)
                        .delay(net.minestom.server.timer.TaskSchedule.millis((long)(activationDelay * 1000)))
                        .schedule();
                } else {
                    onLand();
                }
            }
        }
    }

    public abstract void onLand();
    
    protected void onFlightTick() {}
    
    public abstract String getId();
}