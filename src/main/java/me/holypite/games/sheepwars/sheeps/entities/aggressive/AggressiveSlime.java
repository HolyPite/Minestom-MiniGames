package me.holypite.games.sheepwars.sheeps.entities.aggressive;

import me.holypite.utils.TKit;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.SlimeMeta;

public class AggressiveSlime extends AggressiveMob {

    public AggressiveSlime(int size) {
        super(EntityType.SLIME);
        this.damage = 3.0f;
        this.attackRadius = 0.5; // Small margin for slime
        if (getEntityMeta() instanceof SlimeMeta meta) {
            meta.setSize(size);
        }
    }

    @Override
    public void update(long time) {
        super.update(time); // Uses base AI logic

        if (target != null && isOnGround() && getAliveTicks() % 40 == 0) {
            // Leap Attack
            Vec direction = target.getPosition().sub(getPosition()).asVec().normalize();
            setVelocity(direction.mul(10).withY(15));
        }
    }
}
