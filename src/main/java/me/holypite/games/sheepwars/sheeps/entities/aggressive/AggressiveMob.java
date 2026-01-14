package me.holypite.games.sheepwars.sheeps.entities.aggressive;

import me.holypite.utils.TKit;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;

public abstract class AggressiveMob extends EntityCreature {

    protected Player target;
    protected int ticksSinceAttack = 0;
    protected double searchRadius = 15.0;
    protected double attackRadius = 0.8; // Relative to hitboxes
    protected float damage = 2.0f;

    public AggressiveMob(EntityType type) {
        super(type);
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        if (target == null || target.isRemoved() || !target.isOnline()) {
            findTarget();
        }

        if (target != null) {
            lookAt(target);

            // Movement Logic
            if (!hasNoGravity()) {
                // Ground pathfinding - Update path only if target moved significantly or every 10 ticks
                if (getAliveTicks() % 10 == 0) {
                    getNavigator().setPathTo(target.getPosition());
                }
            } else {
                // Flying movement
                Vec direction = target.getPosition().add(0, target.getEyeHeight(), 0).sub(getPosition()).asVec().normalize();
                double speed = getAttributeValue(net.minestom.server.entity.attribute.Attribute.MOVEMENT_SPEED);
                setVelocity(getVelocity().lerp(direction.mul(speed * 20), 0.1));
            }
            
            // Attack Logic (Bounding Box aware)
            if (canReach(target) && ticksSinceAttack > 20) {
                performAttack(target);
                ticksSinceAttack = 0;
            }
        }
        
        ticksSinceAttack++;
    }

    protected boolean canReach(Entity target) {
        // Precise intersection check with a small margin for reach
        return getBoundingBox().expand(attackRadius, attackRadius, attackRadius)
                .intersectEntity(getPosition(), target);
    }

    protected void performAttack(Player target) {
        target.damage(me.holypite.manager.damage.DamageSources.mobAttack(this, damage));
        // Visual feedback
        getInstance().sendGroupedPacket(new EntityAnimationPacket(getEntityId(), EntityAnimationPacket.Animation.SWING_MAIN_ARM));
    }

    protected void findTarget() {
        target = TKit.getNearestPlayer(getInstance(), getPosition(), searchRadius, true);
    }
}
