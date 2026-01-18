package me.holypite.entity;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.utils.time.TimeUnit;

import java.util.List;

public class AggressiveLarva extends EntityCreature {

    private static final float MAX_HEALTH = 4.0f;
    private static final float MOVEMENT_SPEED = 0.45f;
    private static final double ATTACK_SPEED_MULTIPLIER = 1.8;
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private static final int TARGET_RANGE = 15;

    public AggressiveLarva(EntityType type) {
        super(type);
        
        // 4 HP (2 Hearts)
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(MAX_HEALTH);
        setHealth(MAX_HEALTH);

        // Increase base movement speed
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);
        
        // Basic AI: Wander and Attack Players
        addAIGroup(
                List.of(
                        new MeleeAttackGoal(this, ATTACK_SPEED_MULTIPLIER, ATTACK_COOLDOWN_TICKS, TimeUnit.SERVER_TICK),
                        new RandomStrollGoal(this, 20) // Wander around
                ), 
                List.of(
                        new ClosestEntityTarget(this, TARGET_RANGE, Player.class)
                )
        );
    }
}
