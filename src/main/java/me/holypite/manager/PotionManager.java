package me.holypite.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.entity.EntityPotionAddEvent;
import net.minestom.server.event.entity.EntityPotionRemoveEvent;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.entity.damage.DamageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PotionManager {
    
    private static final Set<EntityType> UNDEAD_TYPES = Set.of(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
            EntityType.PHANTOM, EntityType.SKELETON, EntityType.WITHER_SKELETON,
            EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE, EntityType.WITHER,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.STRAY
    );

    public PotionManager() {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(EntityTickEvent.class, this::onEntityTick);
        globalEventHandler.addListener(EntityPotionAddEvent.class, this::onPotionAdd);
        globalEventHandler.addListener(EntityPotionRemoveEvent.class, this::onPotionRemove);
    }

    private void onPotionAdd(EntityPotionAddEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        PotionEffect effect = event.getPotion().effect();

        if (effect == PotionEffect.GLOWING) {
            entity.setGlowing(true);
        } else if (effect == PotionEffect.INVISIBILITY) {
            entity.setInvisible(true);
        }
    }

    private void onPotionRemove(EntityPotionRemoveEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        PotionEffect effect = event.getPotion().effect();

        if (effect == PotionEffect.GLOWING) {
            entity.setGlowing(false);
        } else if (effect == PotionEffect.INVISIBILITY) {
            entity.setInvisible(false);
        }
    }

    private void onEntityTick(EntityTickEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.getActiveEffects().isEmpty()) return;

        // Use a copy to avoid concurrent modification if we remove effects
        List<TimedPotion> effects = new ArrayList<>(entity.getActiveEffects());
        long tickCount = entity.getAliveTicks();
        
        for (TimedPotion timedPotion : effects) {
            applyEffectLogic(entity, timedPotion.potion(), tickCount);
        }
    }

    private void applyEffectLogic(LivingEntity entity, Potion potion, long tickCount) {
        PotionEffect effect = potion.effect();
        byte amplifier = (byte) potion.amplifier();
        
        // Instant effects (handled immediately and removed)
        if (isInstant(effect)) {
            applyInstantEffect(entity, effect, amplifier);
            entity.removeEffect(effect);
            return;
        }

        // Periodic effects
        if (shouldApplyEffectTickThisTick(effect, tickCount, amplifier)) {
            applyPeriodicEffect(entity, effect, amplifier);
        }
    }
    
    private boolean isInstant(PotionEffect effect) {
        return effect == PotionEffect.INSTANT_HEALTH || effect == PotionEffect.INSTANT_DAMAGE || effect == PotionEffect.SATURATION;
    }

    private boolean shouldApplyEffectTickThisTick(PotionEffect effect, long tickCount, int amplifier) {
        if (effect == PotionEffect.REGENERATION) {
             int interval = 50 >> amplifier;
             return interval > 0 ? tickCount % interval == 0 : true;
        }
        if (effect == PotionEffect.POISON) {
             int interval = 25 >> amplifier;
             return interval > 0 ? tickCount % interval == 0 : true;
        }
        if (effect == PotionEffect.WITHER) {
             int interval = 40 >> amplifier;
             return interval > 0 ? tickCount % interval == 0 : true;
        }
        if (effect == PotionEffect.HUNGER) {
            return true;
        }
        return false;
    }

    private void applyInstantEffect(LivingEntity entity, PotionEffect effect, int amplifier) {
        boolean isUndead = isUndead(entity);
        float maxHealth = (float) entity.getAttribute(Attribute.MAX_HEALTH).getValue();

        if (effect == PotionEffect.INSTANT_HEALTH) {
            if (isUndead) {
                entity.damage(me.holypite.manager.damage.DamageSources.magic((float) (6 << amplifier)));
            } else {
                entity.setHealth(Math.min(entity.getHealth() + (float) Math.max(4 << amplifier, 0), maxHealth));
            }
        } else if (effect == PotionEffect.INSTANT_DAMAGE) {
            if (isUndead) {
                 entity.setHealth(Math.min(entity.getHealth() + (float) Math.max(4 << amplifier, 0), maxHealth));
            } else {
                entity.damage(me.holypite.manager.damage.DamageSources.magic((float) (6 << amplifier)));
            }
        } else if (effect == PotionEffect.SATURATION) {
            if (entity instanceof Player player) {
                int amount = amplifier + 1;
                player.setFood(Math.min(player.getFood() + amount, 20));
                player.setFoodSaturation(Math.min(player.getFoodSaturation() + amount, 20f));
            }
        }
    }

    private void applyPeriodicEffect(LivingEntity entity, PotionEffect effect, int amplifier) {
        // Skip damage for spectators
        if (entity instanceof Player p && p.getGameMode() == net.minestom.server.entity.GameMode.SPECTATOR) return;

        float maxHealth = (float) entity.getAttribute(Attribute.MAX_HEALTH).getValue();

        if (effect == PotionEffect.REGENERATION) {
            if (entity.getHealth() < maxHealth) {
                entity.setHealth(Math.min(entity.getHealth() + 1, maxHealth));
            }
        } else if (effect == PotionEffect.POISON) {
            if (entity.getHealth() > 1.0f) {
                entity.damage(me.holypite.manager.damage.DamageSources.magic(1.0f));
            }
        } else if (effect == PotionEffect.WITHER) {
            entity.damage(me.holypite.manager.damage.DamageSources.wither(1.0f));
        } else if (effect == PotionEffect.HUNGER) {
            if (entity instanceof Player player) {
                // Simplified hunger
            }
        }
    }
    
    private boolean isUndead(LivingEntity entity) {
        return UNDEAD_TYPES.contains(entity.getEntityType());
    }
}