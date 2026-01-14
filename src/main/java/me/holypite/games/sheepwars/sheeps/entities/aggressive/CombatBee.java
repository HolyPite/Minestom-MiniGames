package me.holypite.games.sheepwars.sheeps.entities.aggressive;

import me.holypite.utils.TKit;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;

public class CombatBee extends AggressiveMob {

    private boolean hasStung = false;
    private int deathTimer = 0;

    public CombatBee() {
        super(EntityType.BEE);
        setNoGravity(true);
        this.attackRadius = 0.5;
    }

    @Override
    public void update(long time) {
        if (hasStung) {
            super.update(time);
            handlePostSting();
            return;
        }

        super.update(time);

        if (target == null) {
            // Idle hover
            double hover = Math.sin(getAliveTicks() * 0.2) * 0.05;
            setVelocity(new Vec(0, hover, 0));
        }
    }

    @Override
    protected void performAttack(Player player) {
        player.damage(me.holypite.manager.damage.DamageSources.sting(this, 2.0f));
        player.addEffect(new Potion(PotionEffect.POISON, (byte) 0, 10 * 20)); // 10s Poison
        
        hasStung = true;
        deathTimer = 600; // 30 seconds to live after stinging
        
        // Animation
        getInstance().sendGroupedPacket(new EntityAnimationPacket(getEntityId(), EntityAnimationPacket.Animation.SWING_MAIN_ARM));
    }

    private void handlePostSting() {
        if (deathTimer-- <= 0) {
            remove();
        } else {
            // Wander away
            if (getAliveTicks() % 40 == 0) {
                setVelocity(new Vec(Math.random() - 0.5, 0.1, Math.random() - 0.5).mul(5));
            }
        }
    }
}
