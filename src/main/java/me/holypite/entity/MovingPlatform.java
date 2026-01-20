package me.holypite.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.golem.ShulkerMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;

public class MovingPlatform {

    private final Entity displayEntity;
    private final List<Entity> colliders = new ArrayList<>();
    private final Instance instance;
    private final Pos startPos;
    private final Pos endPos;
    private final double speed; // blocks per tick
    private final int width; // X size
    private final int length; // Z size
    // Height is fixed to 1 block for simplicity, represented by Shulkers

    private boolean movingForward = true;
    private double progress = 0.0; // 0.0 to 1.0
    private double totalDistance;
    private Vec direction;

    public MovingPlatform(Instance instance, Pos startPos, Pos endPos, Block displayBlock, int width, int length, double speed) {
        this.instance = instance;
        this.startPos = startPos;
        this.endPos = endPos;
        this.speed = speed;
        this.width = width;
        this.length = length;
        this.totalDistance = startPos.distance(endPos);
        this.direction = endPos.sub(startPos).asVec().normalize();

        // 1. Create Visual (Block Display)
        this.displayEntity = new Entity(EntityType.BLOCK_DISPLAY);
        BlockDisplayMeta meta = (BlockDisplayMeta) this.displayEntity.getEntityMeta();
        meta.setBlockState(displayBlock);
        
        // Scale to match width/length. 
        // BlockDisplay is 1x1x1 by default centered on its corner? No, usually corner.
        // We need to scale it.
        meta.setScale(new Vec(width, 1, length));
        // Center the display relative to the pivot if needed, but easier to keep corner aligned with shulkers
        // Shulkers are 1x1x1. If width=3, we have 3 shulkers.
        
        this.displayEntity.setNoGravity(true);
        this.displayEntity.setInstance(instance, startPos);

        // 2. Create Colliders (Shulkers)
        createColliders();

        // 3. Start Movement Task
        startTask();
    }

    private void createColliders() {
        // We fill the area [0, width] x [0, length] with Shulkers
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                Entity shulker = new Entity(EntityType.SHULKER);
                ShulkerMeta meta = (ShulkerMeta) shulker.getEntityMeta();
                shulker.setInvisible(true);
                shulker.setNoGravity(true);
                // meta.setPeekHeight(0); // Removed: Method not found and default behavior is acceptable
                
                // Important: Shulker AI must be disabled to prevent teleporting/peeking logic interference
                // But Minestom entities don't have AI by default unless we add it.
                
                // Offset relative to platform origin
                // Platform origin (startPos) is the corner (min X, min Z)
                Pos relativePos = new Pos(x, 0, z);
                
                // Set initial pos
                shulker.setInstance(instance, startPos.add(relativePos));
                
                // Tag it to find it later if needed, or just store in list
                colliders.add(shulker);
            }
        }
    }

    private void startTask() {
        MinecraftServer.getSchedulerManager().buildTask(this::tick)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    private void tick() {
        if (instance == null) return; // Should handle cleanup

        // Update progress tracking (Virtual position)
        double step = speed / totalDistance;
        
        // Calculate Velocity Vector
        // direction is normalized. speed is blocks/tick.
        Vec velocity = direction.mul(movingForward ? speed : -speed);

        if (movingForward) {
            progress += step;
            if (progress >= 1.0) {
                // End of path reached
                progress = 1.0;
                movingForward = false;
                
                // RE-SYNC: Teleport to exact end position to fix drift
                reSync(endPos);
                return; 
            }
        } else {
            progress -= step;
            if (progress <= 0.0) {
                // Start of path reached
                progress = 0.0;
                movingForward = true;
                
                // RE-SYNC: Teleport to exact start position to fix drift
                reSync(startPos);
                return;
            }
        }

        // Apply Velocity (For Client Prediction - Smoothness)
        displayEntity.setVelocity(velocity);
        
        // Push Players
        Pos platformPos = displayEntity.getPosition();
        for (net.minestom.server.entity.Player player : instance.getPlayers()) {
            Pos pPos = player.getPosition();
            
            // Check if player is on the platform
            // Platform base is at platformPos. Shulkers are 1 block high.
            // Player feet should be around platformPos.y + 1
            
            boolean inX = pPos.x() >= platformPos.x() - 0.5 && pPos.x() <= platformPos.x() + width + 0.5;
            boolean inZ = pPos.z() >= platformPos.z() - 0.5 && pPos.z() <= platformPos.z() + length + 0.5;
            boolean inY = pPos.y() >= platformPos.y() + 0.5 && pPos.y() <= platformPos.y() + 2.5; // Tolerance
            
            if (inX && inZ && inY) {
                // Apply same velocity to player to carry them
                // We set the velocity directly. The client handles the addition to their own movement (walking).
                player.setVelocity(velocity);
            }
        }
        
        // Apply to Colliders
        // Note: Minestom physics must update their server-side position for collisions to work!
        for (Entity shulker : colliders) {
            shulker.setVelocity(velocity);
        }
    }

    private void reSync(Pos targetBasePos) {
        // Stop movement momentarily ensuring exact position
        displayEntity.setVelocity(Vec.ZERO);
        displayEntity.teleport(targetBasePos);
        
        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                if (i < colliders.size()) {
                    Entity shulker = colliders.get(i++);
                    shulker.setVelocity(Vec.ZERO);
                    shulker.teleport(targetBasePos.add(x, 0, z));
                }
            }
        }
    }
    
    public void remove() {
        displayEntity.remove();
        colliders.forEach(Entity::remove);
    }
}
