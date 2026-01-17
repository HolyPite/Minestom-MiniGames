package me.holypite.manager.projectile.entities;

import me.holypite.manager.projectile.AbstractProjectile;
import me.holypite.utils.TKit;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MeteorProjectile extends AbstractProjectile {

    private final List<Entity> visualBlocks = new ArrayList<>();
    private me.holypite.manager.explosion.ExplosionManager explosionManager;
    private int tickCounter = 0;

    public MeteorProjectile(Entity shooter) {
        super(EntityType.SNOWBALL, shooter);
        setup();
        // Invisible snowball
        if (getEntityMeta() instanceof ProjectileMeta meta) {
            meta.setShooter(this.shooter);
        }
        setInvisible(true);
        setNoGravity(false);
    }
    
    public void setExplosionManager(me.holypite.manager.explosion.ExplosionManager explosionManager) {
        this.explosionManager = explosionManager;
    }

    private void setup() {
        this.collidesWithEntities = true;
    }

    public void initVisuals() {
        if (getInstance() == null) return;
        
        // Center Block: Magma
        addVisualBlock(Block.MAGMA_BLOCK, new Vec(1.5, 1.5, 1.5), Vec.ZERO);
        
        // Random surrounding blocks
        Random rng = ThreadLocalRandom.current();
        int count = 2 + rng.nextInt(3); // 2 to 4 extra blocks
        
        Block[] types = {Block.OBSIDIAN, Block.CRYING_OBSIDIAN, Block.COBBLESTONE, Block.NETHERRACK};
        
        for (int i = 0; i < count; i++) {
            Block type = types[rng.nextInt(types.length)];
            Vec offset = new Vec(
                rng.nextDouble() - 0.5,
                rng.nextDouble() - 0.5,
                rng.nextDouble() - 0.5
            ).normalize().mul(0.8); // Offset from center
            
            addVisualBlock(type, new Vec(0.8, 0.8, 0.8), offset);
        }
    }
    
    private void addVisualBlock(Block block, Vec scale, Vec translation) {
        Entity display = new Entity(EntityType.BLOCK_DISPLAY);
        if (display.getEntityMeta() instanceof BlockDisplayMeta meta) {
            meta.setBlockState(block);
            meta.setScale(scale);
            // Center the block display (it anchors at corner)
            // If scale is 1.5, we want to shift by -0.75
            meta.setTranslation(translation.sub(scale.x()/2, scale.y()/2, scale.z()/2));
        }
        display.setInstance(getInstance(), getPosition());
        this.addPassenger(display);
        visualBlocks.add(display);
    }

    @Override
    public void tick(long time) {
        if (removed || inBlock) return;
        super.tick(time);
        
        tickCounter++;
        
        // Particles
        if (getInstance() != null) {
            TKit.spawnParticles(getInstance(), Particle.FLAME, getPosition(), 0.5f, 0.5f, 0.5f, 0.05f, 5);
            TKit.spawnParticles(getInstance(), Particle.LARGE_SMOKE, getPosition(), 0.5f, 0.5f, 0.5f, 0.05f, 3);
        }
        
        // Rotate Visuals (Pseudo-rotation via passengers?) 
        // BlockDisplays don't rotate with parent automatically in all axes, mostly yaw.
        // We can manually rotate them if we want, but simple tumbling might be hard without teleporting passengers.
        // Minestom handles passenger position, but we can update their yaw/pitch relative to us.
        // Let's just spin the main entity
        float spinSpeed = 15f;
        setView(getPosition().yaw() + spinSpeed, getPosition().pitch() + spinSpeed);
    }
    
    @Override
    public void remove() {
        for (Entity e : visualBlocks) {
            e.remove();
        }
        visualBlocks.clear();
        super.remove();
    }

    private void explode() {
        if (removed) return;
        if (getInstance() != null) {
             if (explosionManager != null) {
                // Fire explosion!
                explosionManager.explode(getInstance(), getPosition(), 4.0f, true, shooter, this); // fire=true
            } else {
                getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), 4.0f, null);
            }
        }
        remove();
    }

    @Override
    protected void handleBlockCollision(Block hitBlock, Point hitPos, Pos posBefore) {
        explode();
    }

    @Override
    protected boolean handleEntityCollision(net.minestom.server.collision.EntityCollisionResult result, Point hitPos, Pos posBefore) {
        // Don't collide with self or passengers
        if (visualBlocks.contains(result.entity())) return false;
        
        explode();
        return true;
    }

    public void shoot(Point from, Point to, double power, double spread) {
        // Similar to ArrowProjectile but simpler
         var instance = shooter.getInstance();
        if (instance == null) return;

        Vec direction = to.sub(from).asVec().normalize();
        Vec velocity = direction.mul(power * ServerFlag.SERVER_TICKS_PER_SECOND);
        
        this.setInstance(instance, Pos.fromPoint(from)).whenComplete((res, err) -> {
            if (err == null) {
                initVisuals(); // Spawn blocks once in instance
                setVelocity(velocity);
            }
        });
    }

    @Override
    protected @NotNull Vec updateVelocity(@NotNull Pos entityPosition, @NotNull Vec currentVelocity, @NotNull Block.@NotNull Getter blockGetter, @NotNull Aerodynamics aerodynamics, boolean positionChanged, boolean entityFlying, boolean entityOnGround, boolean entityNoGravity) {
        // Standard gravity
        if (!positionChanged) {
            return entityFlying ? Vec.ZERO : new Vec(0.0, entityNoGravity ? 0.0 : -aerodynamics.gravity() * aerodynamics.verticalAirResistance(), 0.0);
        } else {
            double drag = aerodynamics.horizontalAirResistance();
            double gravity = entityFlying ? 0.0 : aerodynamics.gravity();
            double gravityDrag = entityFlying ? 0.6 : aerodynamics.verticalAirResistance();
            
            double x = currentVelocity.x() * drag;
            double y = entityNoGravity ? currentVelocity.y() : (currentVelocity.y() - gravity) * gravityDrag;
            double z = currentVelocity.z() * drag;
            
            return new Vec(Math.abs(x) < 1.0E-6 ? 0.0 : x, Math.abs(y) < 1.0E-6 ? 0.0 : y, Math.abs(z) < 1.0E-6 ? 0.0 : z);
        }
    }
}
