package me.holypite.manager.explosion;

import me.holypite.manager.damage.DamageSources;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
        Set<Point> blocks = new HashSet<>();
        float strength = getStrength();
        float centerX = getCenterX();
        float centerY = getCenterY();
        float centerZ = getCenterZ();

        // 1. Block Destruction Logic (Vanilla Raycasting)
        if (breakBlocks) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    for (int i1 = 0; i1 < 16; ++i1) {
                        if (k == 0 || k == 15 || l == 0 || l == 15 || i1 == 0 || i1 == 15) {
                            double d0 = (float) k / 15.0F * 2.0F - 1.0F;
                            double d1 = (float) l / 15.0F * 2.0F - 1.0F;
                            double d2 = (float) i1 / 15.0F * 2.0F - 1.0F;
                            double length = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                            d0 /= length;
                            d1 /= length;
                            d2 /= length;
                            
                            float rayStrength = strength * (0.7F + ThreadLocalRandom.current().nextFloat() * 0.6F);
                            double posX = centerX;
                            double posY = centerY;
                            double posZ = centerZ;

                            for (; rayStrength > 0.0F; rayStrength -= 0.22500001F) {
                                Point blockPos = new Vec(Math.floor(posX), Math.floor(posY), Math.floor(posZ));
                                Block block = instance.getBlock(blockPos);

                                if (!block.isAir()) {
                                    float resistance = block.registry().explosionResistance();
                                    rayStrength -= (resistance + 0.3F) * 0.3F;
                                }

                                if (rayStrength > 0.0F) {
                                    // Don't break Bedrock or Air
                                    if (block != Block.BEDROCK && !block.isAir()) {
                                        blocks.add(blockPos);
                                    }
                                }

                                posX += d0 * 0.30000001192092896D;
                                posY += d1 * 0.30000001192092896D;
                                posZ += d2 * 0.30000001192092896D;
                            }
                        }
                    }
                }
            }
        }

        // 2. Entity Damage Logic
        double damageRadius = strength * 2.0;
        Point center = new Vec(centerX, centerY, centerZ);
        
        instance.getEntities().stream()
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> e.getPosition().distance(center) <= damageRadius)
                .forEach(e -> applyEntityDamage(instance, (LivingEntity) e));

        return new ArrayList<>(blocks);
    }

    private void applyEntityDamage(Instance instance, LivingEntity entity) {
        Point center = new Vec(getCenterX(), getCenterY(), getCenterZ());
        double distance = entity.getPosition().distance(center);
        double damageRadius = getStrength() * 2.0;
        
        if (distance > damageRadius) return;
        
        double exposure = calculateExposure(instance, center, entity);
        double damageFactor = (1.0 - (distance / damageRadius)) * exposure;
        
        // (factor * factor + factor) / 2.0 * 7.0 * strength + 1.0
        // Simplified Vanilla formula approx:
        float damageAmount = (float) ((damageFactor * damageFactor + damageFactor) / 2.0 * 7.0 * getStrength() + 1.0);

        if (damageAmount > 1.0f) {
            entity.damage(DamageSources.explosion(null, null, center, damageAmount));
            
            // Apply Knockback (scaled by exposure)
            Vec direction = entity.getPosition().sub(center).asVec().normalize();
            entity.setVelocity(entity.getVelocity().add(direction.mul(damageFactor * 20))); // 20 is arbitrary KB scale
        }
    }
    
    /**
     * Calculates how much of the entity is exposed to the explosion center.
     * Raytraces from center to entity bounding box points.
     * Simple implementation: Raytrace to eye and feet.
     */
    private double calculateExposure(Instance instance, Point center, LivingEntity entity) {
        // Simplified exposure: Raycast to eye position only
        // Vanilla checks multiple points on the bounding box.
        // Here we just check if line of sight exists to center of entity for perf.
        
        Point entityPos = entity.getPosition().add(0, entity.getEyeHeight() / 2, 0);
        Vec direction = entityPos.sub(center).asVec();
        double distance = direction.length();
        direction = direction.normalize();
        
        double stepSize = 0.5; // Precision
        double currentDist = 0;
        
        // Raycast from Explosion Center -> Entity
        // If we hit a block, exposure is 0 (occluded). 
        // This is binary exposure (0 or 1). Vanilla is gradient (0.0 to 1.0).
        // For gradient, we need multiple rays.
        
        // Let's do a simple binary check for now to enable "hiding behind walls".
        Point currentPos = center;
        while (currentDist < distance) {
             Block block = instance.getBlock(currentPos);
             if (!block.isAir() && block.registry().explosionResistance() > 0.5) { // Assuming some resistance blocks view
                 return 0.0;
             }
             currentPos = currentPos.add(direction.mul(stepSize));
             currentDist += stepSize;
        }
        
        return 1.0;
    }
}
