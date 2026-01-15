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

    public AggressiveLarva(EntityType type) {
        super(type);
        
        // 4 HP (2 Hearts)
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(4f);
        setHealth(4f);
        
        // Basic AI: Wander and Attack Players
        addAIGroup(
                List.of(
                        new MeleeAttackGoal(this, 1.2, 20, TimeUnit.SERVER_TICK), // Attack with small range
                        new RandomStrollGoal(this, 20) // Wander around
                ), 
                List.of(
                        new ClosestEntityTarget(this, 15, Player.class)
                )
        );
    }
}
