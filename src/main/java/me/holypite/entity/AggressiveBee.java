package me.holypite.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AggressiveBee extends EntityCreature {

    private boolean hasStung = false;
    private int ticksSinceLastMove = 0;

    public AggressiveBee() {
        super(EntityType.BEE);
        
        // Set Max Health to 2 hearts (4 HP)
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(4f);
        setHealth(4f);
        
        setNoGravity(true); // Bees fly
        
        // Target Selector only
        addAIGroup(
                List.of(), 
                List.of(
                        new ClosestEntityTarget(this, 15, Player.class)
                )
        );
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        if (hasStung) return; // Stop AI if stung
        
        // Damage Players on Collision
        if (getInstance() != null) {
            getInstance().getEntities().stream()
                .filter(e -> e instanceof Player)
                .filter(e -> e.getPosition().distance(getPosition()) < 1.5)
                .findFirst()
                .ifPresent(e -> {
                    LivingEntity target = (LivingEntity) e;
                    target.damage(me.holypite.manager.damage.DamageSources.mobAttack(this, 4.0f));
                    target.addEffect(new Potion(PotionEffect.POISON, (byte) 1, 200)); // 10s Poison
                    
                    hasStung = true;
                    setVelocity(Vec.ZERO); // Stop moving immediately
                    
                    // Die after 5 seconds
                    MinecraftServer.getSchedulerManager().buildTask(this::remove)
                        .delay(TaskSchedule.seconds(5))
                        .schedule();
                });
        }
        
        // Custom Flight AI
        Entity target = getTarget();
        
        if (target != null) {
            // Fly towards target
            Vec direction = target.getPosition().add(0, 1, 0).sub(getPosition()).asVec().normalize();
            setVelocity(direction.mul(0.6)); // Reasonable speed
            lookAt(target);
        } else {
            // Random flight
            if (ticksSinceLastMove++ > 40) { // Change direction every 2s
                double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
                double verticalAngle = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
                
                Vec randomDir = new Vec(Math.cos(angle), verticalAngle, Math.sin(angle)).normalize();
                setVelocity(randomDir.mul(0.4));
                
                ticksSinceLastMove = 0;
            }
        }
    }
}