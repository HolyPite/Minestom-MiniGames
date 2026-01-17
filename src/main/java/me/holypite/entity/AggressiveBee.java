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

import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.utils.time.TimeUnit;
import me.holypite.utils.TKit;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AggressiveBee extends EntityCreature {

    private static final float MAX_HEALTH = 4.0f;
    private static final double ATTACK_VELOCITY = 7.0;
    private static final double RANDOM_FLIGHT_VELOCITY = 3.0;
    private static final float ATTACK_DAMAGE = 4.0f;
    private static final int POISON_DURATION_TICKS = 200;
    private static final int TARGET_RANGE = 15;
    private static final double COLLISION_RADIUS = 1.5;

    private boolean hasStung = false;
    private int ticksSinceLastMove = 0;

    public AggressiveBee() {
        super(EntityType.BEE);
        
        // Set Max Health to 2 hearts (4 HP)
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(MAX_HEALTH);
        setHealth(MAX_HEALTH);
        
        setNoGravity(true); // Bees fly
        
        // Add a goal to keep AI ticking and a target selector
        addAIGroup(
                List.of(new RandomStrollGoal(this, 20)), 
                List.of(new ClosestEntityTarget(this, TARGET_RANGE, Player.class))
        );
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        if (hasStung || getInstance() == null) return;
        
        // Damage Players on Collision
        getInstance().getEntities().stream()
            .filter(e -> e instanceof Player)
            .filter(e -> e.getPosition().distance(getPosition()) < COLLISION_RADIUS)
            .findFirst()
            .ifPresent(e -> {
                LivingEntity target = (LivingEntity) e;
                target.damage(me.holypite.manager.damage.DamageSources.mobAttack(this, ATTACK_DAMAGE));
                target.addEffect(new Potion(PotionEffect.POISON, (byte) 1, POISON_DURATION_TICKS));
                
                hasStung = true;
                setVelocity(Vec.ZERO); 
                
                MinecraftServer.getSchedulerManager().buildTask(this::remove)
                    .delay(TaskSchedule.seconds(5))
                    .schedule();
            });

        if (hasStung) return;

        // Custom Flight AI
        Entity target = getTarget();
        
        // Manual fallback if AI target is null
        if (target == null) {
            target = TKit.getNearestPlayer(getInstance(), getPosition(), TARGET_RANGE, true);
        }
        
        if (target != null) {
            // Fly towards target
            Vec direction = target.getPosition().add(0, 1, 0).sub(getPosition()).asVec().normalize();
            setVelocity(direction.mul(ATTACK_VELOCITY));
            lookAt(target);
        } else {
            // Random flight
            if (ticksSinceLastMove++ > 40) {
                double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
                double verticalAngle = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
                
                Vec randomDir = new Vec(Math.cos(angle), verticalAngle, Math.sin(angle)).normalize();
                setVelocity(randomDir.mul(RANDOM_FLIGHT_VELOCITY));
                
                ticksSinceLastMove = 0;
            }
        }
    }
}