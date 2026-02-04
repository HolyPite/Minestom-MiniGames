package me.holypite.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;

public class MovingPlatform {

    private final Entity displayEntity;
    private final List<LivingEntity> colliders = new ArrayList<>();
    private final Instance instance;
    private final Pos startPos;
    private final Pos endPos;
    private final double speed; // blocks per tick
    private final int width; // X size
    private final int length; // Z size

    private boolean movingForward = true;
    private double progress = 0.0; // 0.0 to 1.0
    private final double totalDistance;

    public MovingPlatform(Instance instance, Pos startPos, Pos endPos, Block displayBlock, int width, int length, double speed) {
        this.instance = instance;
        this.startPos = startPos;
        this.endPos = endPos;
        this.speed = speed;
        this.width = width;
        this.length = length;
        this.totalDistance = startPos.distance(endPos);

        // 1. Create Collider (Happy Ghast) - The Main Driver
        Attribute scaleAttr = Attribute.fromKey("minecraft:generic.scale");
        
        LivingEntity collider = new LivingEntity(EntityType.HAPPY_GHAST);
        collider.setInvisible(true);
        collider.setNoGravity(true);
        
        double scale = width / 4.0; 
        if (scaleAttr != null) {
            collider.getAttribute(scaleAttr).setBaseValue(scale);
        }
        
        double yOffset = 1.0 - width; 
        Pos initialColliderPos = startPos.add(width / 2.0, yOffset, length / 2.0);
        
        // 2. Create Visual (BlockDisplay)
        this.displayEntity = new Entity(EntityType.BLOCK_DISPLAY);
        BlockDisplayMeta meta = (BlockDisplayMeta) this.displayEntity.getEntityMeta();
        meta.setBlockState(displayBlock);
        meta.setScale(new Vec(width, 1, length));
        this.displayEntity.setNoGravity(true);

        // Asynchronously set instance
        collider.setInstance(instance, initialColliderPos).thenAccept(ignored -> {
            this.displayEntity.setInstance(instance, startPos).thenAccept(ignored2 -> {
                startTask();
            });
        });
        
        colliders.add(collider);
    }

    private void startTask() {
        MinecraftServer.getSchedulerManager().buildTask(this::tick)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    private void tick() {
        if (instance == null || colliders.isEmpty() || colliders.get(0).isRemoved()) return;
        
        LivingEntity driver = colliders.get(0);

        // 1. Calculate Progress
        double step = speed / totalDistance;
        
        if (movingForward) {
            progress += step;
            if (progress >= 1.0) {
                progress = 1.0;
                movingForward = false;
            }
        } else {
            progress -= step;
            if (progress <= 0.0) {
                progress = 0.0;
                movingForward = true;
            }
        }

        // 2. Target Positions
        Vec pathVector = endPos.sub(startPos).asVec();
        Pos platformCornerTarget = startPos.add(pathVector.mul(progress));
        
        double yOffset = 1.0 - width; 
        Pos driverTarget = platformCornerTarget.add(width / 2.0, yOffset, length / 2.0);
        
        // 3. Velocity Calculation
        Vec moveVec = driverTarget.sub(driver.getPosition()).asVec();
        Vec velocity = moveVec.mul(20); 

        // 4. Move Driver (Happy Ghast)
        if (driver.getPosition().distanceSquared(driverTarget) > 0.01) {
             driver.teleport(driverTarget);
        }
        driver.setVelocity(velocity);
        
        // 5. Move Visual (BlockDisplay)
        displayEntity.teleport(platformCornerTarget);
    }

    public void remove() {
        displayEntity.remove();
        colliders.forEach(Entity::remove);
    }
}