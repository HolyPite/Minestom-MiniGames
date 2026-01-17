package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.item.Material;
import net.minestom.server.color.DyeColor;

public class GiantSheep extends SheepProjectile {

    private static final float SCALE = 3.0f;
    private static final int MAX_BOUNCES = 3;
    private static final float BOUNCE_EXPLOSION_POWER = 3.0f;
    private static final float DEATH_EXPLOSION_POWER = 6.0f;
    private static final double BOUNCE_FACTOR = 1.2;

    private int bounceCount = 0;
    private Vec lastVelocity = Vec.ZERO;

    public GiantSheep(Entity shooter) {
        super(shooter);
        
        // Make it giant!
        getAttribute(Attribute.SCALE).setBaseValue(SCALE);
        
        // Immune to own explosions
        setTag(net.minestom.server.tag.Tag.Boolean("explosion_immune"), true);
        
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.WHITE);
            meta.setCustomName(Component.text("Mouton Géant", NamedTextColor.WHITE));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void update(long time) {
        // Capture velocity before potential landing wipes it
        if (!landed) {
            this.lastVelocity = getVelocity();
        }
        super.update(time);
    }

    @Override
    public void onLand() {
        if (isRemoved()) return;

        bounceCount++;
        
        if (bounceCount < MAX_BOUNCES) {
            // Impact effects (Medium explosion)
            triggerImpact(BOUNCE_EXPLOSION_POWER);
            
            // Bounce logic using the captured velocity
            Vec currentVel = lastVelocity;
            
            // Invert Y and dampen, keep some X/Z momentum
            this.setVelocity(currentVel.withY(Math.abs(currentVel.y())).mul(BOUNCE_FACTOR)); // Force positive Y for bounce
            
            // Force physics update
            this.landed = false;
            // Push up slightly to ensure we leave ground
            this.teleport(getPosition().add(0, 0.5, 0));
            this.setVelocity(this.getVelocity().add(0, 10, 0)); // Reduced explicit impulse, rely on reflection
            
            TKit.playSound(getInstance(), getPosition(), "entity.iron_golem.step", net.kyori.adventure.sound.Sound.Source.HOSTILE, 2.0f, 0.5f);
            
        } else {
            // Final Impact (Big explosion)
            triggerImpact(DEATH_EXPLOSION_POWER);
            remove();
        }
    }
    
    private void triggerImpact(float power) {
         if (explosionManager != null) {
            explosionManager.explode(getInstance(), getPosition(), power, true, shooter, this);
        } else {
            getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), power, null);
        }
    }

    @Override
    public String getId() {
        return "giant";
    }
}
