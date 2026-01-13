package me.holypite.manager.explosion;

import me.holypite.manager.damage.DamageSources;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;

public class GameExplosion extends Explosion {

    private final boolean breakBlocks;
    private final boolean fire;

    public GameExplosion(float centerX, float centerY, float centerZ, float strength, boolean breakBlocks, boolean fire) {
        super(centerX, centerY, centerZ, strength);
        this.breakBlocks = breakBlocks;
        this.fire = fire;
    }

    @Override
    protected List<Point> prepare(Instance instance) {
        List<Point> blocks = new ArrayList<>();
        float strength = getStrength();
        Point center = new Vec(getCenterX(), getCenterY(), getCenterZ());
        
        // 1. Block Destruction Logic (Simple Sphere)
        if (breakBlocks) {
            int radius = (int) Math.ceil(strength);
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Point pos = center.add(x, y, z);
                        double distance = pos.distance(center);
                        
                        if (distance <= strength) {
                            Block block = instance.getBlock(pos);
                            if (!block.isAir() && block != Block.BEDROCK) { // Basic protection
                                blocks.add(pos);
                            }
                        }
                    }
                }
            }
        }

        // 2. Entity Damage Logic
        double damageRadius = strength * 2.0;
        instance.getEntities().stream()
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> e.getPosition().distance(center) <= damageRadius)
                .forEach(e -> applyEntityDamage((LivingEntity) e));

        return blocks;
    }

    private void applyEntityDamage(LivingEntity entity) {
        double distance = entity.getPosition().distance(new Vec(getCenterX(), getCenterY(), getCenterZ()));
        double damageRadius = getStrength() * 2.0;
        
        // Linear damage falloff
        double damageFactor = 1.0 - (distance / damageRadius);
        float damageAmount = (float) (damageFactor * getStrength() * 7.0f); // Adjust multiplier as needed

        if (damageAmount > 0) {
            // Use Centralized Damage System
            // Note: We don't have the "attacker" context here easily unless passed to GameExplosion.
            // For now, null attacker.
            entity.damage(DamageSources.explosion(null, null, new Vec(getCenterX(), getCenterY(), getCenterZ()), damageAmount));
        }
    }
}