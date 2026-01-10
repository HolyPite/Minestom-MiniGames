package me.holypite.manager.projectile;

import me.holypite.manager.explosion.ExplosionManager;
import me.holypite.manager.projectile.entities.ArrowProjectile;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import me.holypite.games.sheepwars.sheeps.ExplosiveSheep;
import net.minestom.server.event.player.PlayerUseItemEvent;

import java.util.concurrent.ThreadLocalRandom;

public class ProjectileManager {

    private static final Tag<Long> CHARGE_SINCE_TAG = Tag.Long("bow_charge_since").defaultValue(Long.MAX_VALUE);
    private static final Tag<Double> BOW_POWER = Tag.Double("bow_power").defaultValue(0.0);
    
    private final EventNode<Event> node;
    private final ExplosionManager explosionManager;

    public ProjectileManager(EventNode<Event> node, ExplosionManager explosionManager) {
        this.node = node;
        this.explosionManager = explosionManager;
        registerBowLogic();
        registerSheepLogic();
        registerCollisionLogic();
    }

    private void registerSheepLogic() {
        node.addListener(PlayerUseItemEvent.class, event -> {
            if (event.getItemStack().material() == Material.RED_WOOL) {
                Player player = event.getPlayer();
                
                // Launch Explosive Sheep
                ExplosiveSheep sheep = new ExplosiveSheep(player);
                sheep.shoot(1.5); 
                
                // Consume item
                // player.getItemInHand(event.getHand()).consume(1); // Need correct API
                player.setItemInHand(event.getHand(), event.getItemStack().withAmount(a -> a - 1));
            }
        });
    }

    private void registerBowLogic() {
        node.addListener(PlayerBeginItemUseEvent.class, event -> {
            if (event.getItemStack().material() != Material.BOW) return;
            event.getPlayer().setTag(CHARGE_SINCE_TAG, System.currentTimeMillis());
        });

        node.addListener(PlayerCancelItemUseEvent.class, event -> {
            if (event.getItemStack().material() != Material.BOW) return;
            Player player = event.getPlayer();
            long duration = System.currentTimeMillis() - player.getTag(CHARGE_SINCE_TAG);
            double power = getPower(duration);

            if (power < 0.1) return;
            player.setTag(BOW_POWER, power);

            // Spawn Arrow
            ArrowProjectile projectile = new ArrowProjectile(EntityType.ARROW, player);
            if (power >= 1) projectile.setCritical(true);

            Pos shootPosition = player.getPosition().add(0, player.getEyeHeight() - 0.1, 0);
            projectile.shoot(shootPosition.asVec(), power * 3, 1f);
            
            player.getInstance().playSound(Sound.sound(SoundEvent.ENTITY_ARROW_SHOOT, Sound.Source.PLAYER, 1f, getRandomPitchFromPower(power)), player);
        });
    }

    private void registerCollisionLogic() {
        node.addListener(ProjectileCollideWithEntityEvent.class, event -> {
            if (!(event.getTarget() instanceof LivingEntity victim)) return;
            if (!(event.getEntity() instanceof AbstractProjectile projectile)) return;
            
            Entity shooter = ((AbstractProjectile) event.getEntity()).shooter;
            
            float damage = 6.0f;
            if (projectile instanceof ArrowProjectile arrow && arrow.isCritical()) {
                damage = 9.0f;
            }
            
            victim.damage(new Damage(DamageType.ARROW, projectile, shooter, projectile.getPosition(), damage));
            
            // KB Direct (impulse from arrow speed)
            Vec directKB = projectile.getVelocity().normalize().mul(8.0).withY(2.0);
            victim.setVelocity(victim.getVelocity().add(directKB));
            
            projectile.remove();
        });
    }

    private double getPower(long duration) {
        double secs = duration / 1000.0;
        double pow = (secs * secs + secs * 2.0) / 3.0;
        if (pow > 1) {
            pow = 1;
        }
        return pow;
    }

    private float getRandomPitchFromPower(double power) {
        return (float) (1.0f / (ThreadLocalRandom.current().nextFloat() * 0.4f + 1.2f) + power * 0.5f);
    }
}
