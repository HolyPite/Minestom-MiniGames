package me.holypite.games.sheepwars.sheeps.entities.aggressive;

import me.holypite.utils.TKit;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;

public class CombatBee extends EntityCreature {

    private Player target;
    private int ticksSinceAttack = 0;
    private boolean hasStung = false;
    private int deathTimer = 0;

    public CombatBee() {
        super(EntityType.BEE);
        setNoGravity(true); 
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        if (hasStung) {
            handlePostSting();
            return;
        }

        if (target == null || target.isRemoved() || !target.isOnline() || target.getPosition().distance(getPosition()) > 20) {
            findTarget();
        }

        if (target != null) {
            moveSmoothlyTowards(target.getPosition().add(0, target.getEyeHeight(), 0));
            lookAt(target);

            // Attack logic
            if (getPosition().distance(target.getPosition()) < 1.2 && ticksSinceAttack > 20) {
                sting(target);
                ticksSinceAttack = 0;
            }
        } else {
            // Idle hover (Sine wave)
            double hover = Math.sin(getAliveTicks() * 0.2) * 0.05;
            setVelocity(new Vec(0, hover, 0));
        }
        
        ticksSinceAttack++;
    }

    private void findTarget() {
        target = TKit.getNearestPlayer(getInstance(), getPosition(), 15.0, true);
    }

    private void moveSmoothlyTowards(net.minestom.server.coordinate.Point goal) {
        Vec direction = goal.sub(getPosition()).asVec();
        if (direction.length() > 0) {
            // Acceleration towards target
            Vec currentVel = getVelocity();
            Vec targetVel = direction.normalize().mul(6.0); // Speed
            setVelocity(currentVel.lerp(targetVel, 0.1)); // Smooth transition
        }
    }

    private void sting(Player player) {
        player.damage(me.holypite.manager.damage.DamageSources.sting(this, 2.0f)); // Vanilla damage
        player.addEffect(new Potion(PotionEffect.POISON, (byte) 0, 10 * 20)); // 10s Poison
        
        hasStung = true;
        deathTimer = 600; // 30 seconds to live after stinging (standard)
        
        // Animation
        getInstance().sendGroupedPacket(new EntityAnimationPacket(getEntityId(), EntityAnimationPacket.Animation.SWING_MAIN_ARM));
    }

    private void handlePostSting() {
        // Fly away randomly before dying
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
