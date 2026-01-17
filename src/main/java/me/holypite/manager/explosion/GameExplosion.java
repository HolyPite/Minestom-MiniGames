package me.holypite.manager.explosion;

import me.holypite.entity.PrimedTnt;
import me.holypite.manager.damage.DamageSources;
import me.holypite.model.Game;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
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
    private final Entity attacker;
    private final Entity source;
    private Game game;

    public GameExplosion(float centerX, float centerY, float centerZ, float strength, boolean breakBlocks, boolean fire, Entity attacker, Entity source) {
        super(centerX, centerY, centerZ, strength);
        this.breakBlocks = breakBlocks;
        this.fire = fire;
        this.attacker = attacker;
        this.source = source;
    }

    public GameExplosion(float centerX, float centerY, float centerZ, float strength, boolean breakBlocks, boolean fire) {
        this(centerX, centerY, centerZ, strength, breakBlocks, fire, null, null);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    protected List<Point> prepare(Instance instance) {
        Set<Point> blocks = new HashSet<>();
        float strength = getStrength();
        float centerX = getCenterX();
        float centerY = getCenterY();
        float centerZ = getCenterZ();
        Point center = new Vec(centerX, centerY, centerZ);

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
                                        // TNT Propagation Check
                                        if (block == Block.TNT && game != null && game.isTntPropagation()) {
                                            instance.setBlock(blockPos, Block.AIR);
                                            PrimedTnt tnt = new PrimedTnt(attacker, ThreadLocalRandom.current().nextInt(10, 30));
                                            tnt.setInstance(instance, blockPos.add(0.5, 0, 0.5));
                                            
                                            // Launch TNT away from explosion
                                            Vec dir = blockPos.add(0.5, 0.5, 0.5).sub(center).asVec().normalize();
                                            tnt.setVelocity(dir.mul(5.0).withY(4.0));
                                        } else {
                                            blocks.add(blockPos);
                                        }
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
        
        float damageAmount = (float) ((damageFactor * damageFactor + damageFactor) / 2.0 * 7.0 * getStrength() + 1.0);

        if (damageAmount > 1.0f) {
            entity.damage(DamageSources.explosion(attacker, source, center, damageAmount));
            
            // Apply Knockback (scaled by exposure)
            Vec direction = entity.getPosition().sub(center).asVec().normalize();
            entity.setVelocity(entity.getVelocity().add(direction.mul(damageFactor * 20))); 
        }
    }
    
    private double calculateExposure(Instance instance, Point center, LivingEntity entity) {
        Point entityPos = entity.getPosition().add(0, entity.getEyeHeight() / 2, 0);
        Vec direction = entityPos.sub(center).asVec();
        double distance = direction.length();
        direction = direction.normalize();
        
        double stepSize = 0.5; 
        double currentDist = 0;
        
        Point currentPos = center;
        while (currentDist < distance) {
             Block block = instance.getBlock(currentPos);
             if (!block.isAir() && block.registry().explosionResistance() > 0.5) { 
                 return 0.0;
             }
             currentPos = currentPos.add(direction.mul(stepSize));
             currentDist += stepSize;
        }
        
        return 1.0;
    }
}