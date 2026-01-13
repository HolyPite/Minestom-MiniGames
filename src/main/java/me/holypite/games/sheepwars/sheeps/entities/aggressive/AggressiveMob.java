package me.holypite.games.sheepwars.sheeps.entities.aggressive;

import me.holypite.utils.TKit;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;

public abstract class AggressiveMob extends EntityCreature {

    protected Player target;
    protected int ticksSinceAttack = 0;
    protected double searchRadius = 15.0;
    protected double attackRadius = 1.5;
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
            Vec direction = target.getPosition().sub(getPosition()).asVec().normalize();
            double speed = getAttributeValue(net.minestom.server.entity.attribute.Attribute.MOVEMENT_SPEED);
            
            if (hasNoGravity()) { // For flying mobs
                setVelocity(getVelocity().lerp(direction.mul(speed * 20), 0.1));
            } else {
                // Ground pathfinding
                if (getPosition().distance(target.getPosition()) > attackRadius) {
                    getNavigator().setPathTo(target.getPosition());
                }
            }
            
            lookAt(target);

            if (getPosition().distance(target.getPosition()) < attackRadius && ticksSinceAttack > 20) {
                target.damage(me.holypite.manager.damage.DamageSources.mobAttack(this, damage));
                ticksSinceAttack = 0;
            }
        }
        
        ticksSinceAttack++;
    }

    protected void findTarget() {
        target = TKit.getNearestPlayer(getInstance(), getPosition(), searchRadius, true);
    }
}
