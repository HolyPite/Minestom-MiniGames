package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.concurrent.ThreadLocalRandom;

public class FragmentationSheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 1;
    private static final float EXPLOSION_POWER = 2.5f;

    public FragmentationSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.ORANGE);
            meta.setCustomName(Component.text("Mouton Fragmentation", TextColor.color(0xFF8C00)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
            // Main explosion
            if (explosionManager != null) {
                explosionManager.explode(getInstance(), getPosition(), EXPLOSION_POWER, true, shooter, this);
            } else {
                getInstance().explode((float)getPosition().x(), (float)getPosition().y(), (float)getPosition().z(), EXPLOSION_POWER, null);
            }
            
            // Spawn cluster
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int count = random.nextInt(4, 11); // 4 to 10
            
            for (int i = 0; i < count; i++) {
                double baseAngle = i * (2 * Math.PI / count);
                // Add random offset to angle (-15 to +15 degrees approx)
                double angle = baseAngle + (random.nextDouble() - 0.5) * (Math.PI / 6);
                
                double x = Math.cos(angle);
                double z = Math.sin(angle);
                
                InstantSheep baby = new InstantSheep(shooter);
                if (baby.getEntityMeta() instanceof SheepMeta babyMeta) {
                    babyMeta.setColor(TKit.getRandomDyeColor());
                }
                
                baby.setInstance(getInstance(), getPosition().add(0, 1, 0));
                
                // Randomize power (10 to 20)
                double power = random.nextDouble(10, 21);
                // Randomize vertical lift slightly
                double yLift = random.nextDouble(0.3, 0.8);
                
                baby.setVelocity(new Vec(x, yLift, z).normalize().mul(power));
            }
            
            remove();
    }

    @Override
    public String getId() {
        return "fragmentation";
    }
}