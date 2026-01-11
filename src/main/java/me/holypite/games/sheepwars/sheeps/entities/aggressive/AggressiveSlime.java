package me.holypite.games.sheepwars.sheeps.entities.aggressive;

import me.holypite.utils.TKit;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.other.SlimeMeta;

public class AggressiveSlime extends EntityCreature {

    private Player target;
    private int ticksSinceAttack = 0;

    public AggressiveSlime(int size) {
        super(EntityType.SLIME);
        if (getEntityMeta() instanceof SlimeMeta meta) {
            meta.setSize(size);
        }
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        if (target == null || target.isRemoved() || !target.isOnline()) {
            findTarget();
        }

        if (target != null) {
            // Move towards target
            Vec direction = target.getPosition().sub(getPosition()).asVec().normalize();
            double speed = 0.2;
            
            // Standard Slime movement (jumps later? For now simple glide)
            setVelocity(getVelocity().lerp(direction.mul(speed * 20), 0.1));
            
            lookAt(target);

            // Attack
            if (getPosition().distance(target.getPosition()) < 1.5 && ticksSinceAttack > 20) {
                target.damage(DamageType.MOB_ATTACK, 3.0f);
                ticksSinceAttack = 0;
            }
        }
        
        ticksSinceAttack++;
    }

    private void findTarget() {
        target = TKit.getNearestPlayer(getInstance(), getPosition(), 15.0, true);
    }
}
