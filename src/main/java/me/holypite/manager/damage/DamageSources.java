package me.holypite.manager.damage;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.Nullable;

public class DamageSources {

    public static Damage playerAttack(Player attacker, float amount) {
        return new Damage(DamageType.PLAYER_ATTACK, attacker, attacker, attacker.getPosition(), amount);
    }

    public static Damage mobAttack(LivingEntity attacker, float amount) {
        return new Damage(DamageType.MOB_ATTACK, attacker, attacker, attacker.getPosition(), amount);
    }

    public static Damage explosion(@Nullable Entity source, @Nullable Entity attacker, Point center, float amount) {
        // source is the explosive entity (e.g. TNT), attacker is the igniter (Player)
        return new Damage(DamageType.EXPLOSION, source, attacker, center, amount);
    }
    
    public static Damage arrow(Entity arrow, @Nullable Entity shooter, float amount) {
        return new Damage(DamageType.ARROW, arrow, shooter, arrow.getPosition(), amount);
    }
    
    public static Damage magic(float amount) {
        return new Damage(DamageType.MAGIC, null, null, null, amount);
    }
    
    public static Damage fall(float amount) {
        return new Damage(DamageType.FALL, null, null, null, amount);
    }
    
    public static Damage voidDamage(float amount) {
        return new Damage(DamageType.OUT_OF_WORLD, null, null, null, amount);
    }
    
    public static Damage generic(float amount) {
        return new Damage(DamageType.GENERIC, null, null, null, amount);
    }
    
    public static Damage indirectMagic(Entity source, @Nullable Entity attacker, float amount) {
        return new Damage(DamageType.INDIRECT_MAGIC, source, attacker, source.getPosition(), amount);
    }
    
    public static Damage thorns(Entity source, float amount) {
        return new Damage(DamageType.THORNS, source, source, source.getPosition(), amount);
    }
    
    public static Damage wither(float amount) {
        return new Damage(DamageType.WITHER, null, null, null, amount);
    }

    public static Damage sting(LivingEntity mob, float amount) {
        return new Damage(DamageType.STING, mob, mob, mob.getPosition(), amount);
    }
}
