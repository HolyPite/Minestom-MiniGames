package me.holypite.games.sheepwars.sheeps;

import me.holypite.manager.projectile.entities.ArrowProjectile;
import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.List;

public class HedgehogSheep extends SheepProjectile {

    private static final int ARROW_COUNT = 60;
    private static final double SPHERE_RADIUS = 1.5;
    private static final double ARROW_SPEED = 2.5;
    private static final int SHOOT_DELAY_TICKS = 20; // 1 second

    public HedgehogSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.BROWN);
            meta.setCustomName(Component.text("Mouton Hérisson", TextColor.color(0xB8860B)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        if (getInstance() == null) return;
        
        List<ArrowProjectile> arrows = new ArrayList<>();
        
        // Spawn dome of arrows
        // Using Fibonacci Sphere algorithm for even distribution
        double phi = Math.PI * (3. - Math.sqrt(5.));  // Golden angle

        for (int i = 0; i < ARROW_COUNT; i++) {
            double y = 1 - (i / (float) (ARROW_COUNT - 1)) * 2;  // y goes from 1 to -1
            double radius = Math.sqrt(1 - y * y);  // Radius at y

            double theta = phi * i;  // Golden angle increment

            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;
            
            Vec direction = new Vec(x, y, z).normalize();
            
            // Only upper hemisphere looks better on ground? 
            // Or full sphere if it bounces? Let's do full sphere for chaos.
            
            ArrowProjectile arrow = new ArrowProjectile(EntityType.ARROW, shooter);
            arrow.setNoGravity(true); // Suspend them
            
            // Spawn around sheep
            arrow.setInstance(getInstance(), getPosition().add(0, 0.5, 0).add(direction.mul(SPHERE_RADIUS)));
            
            // Orient arrow outwards (Velocity 0 but looking at direction)
            // Minestom doesn't have easy lookAt for non-living? 
            // We can set velocity to epsilon to orient it, or calculate yaw/pitch
            
            // Hack: Shoot with 0 power to orient, then stop?
            // Or manually set View.
            // AbstractProjectile.shoot sets view based on To-From.
            arrow.shoot(getInstance(), 
                        getPosition().add(0, 0.5, 0).add(direction.mul(SPHERE_RADIUS)), 
                        getPosition().add(0, 0.5, 0).add(direction.mul(SPHERE_RADIUS * 2)), 
                        0.001, 0); // Tiny speed to orient
                        
            arrows.add(arrow);
        }
        
        TKit.playSound(getInstance(), getPosition(), "entity.arrow.hit_player", net.kyori.adventure.sound.Sound.Source.NEUTRAL, 1f, 0.5f);

        // Shoot them after delay
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (isRemoved()) { // If sheep died, remove arrows? No, let them shoot!
                // But if arrows are removed?
            }
            
            for (ArrowProjectile arrow : arrows) {
                if (!arrow.isRemoved()) {
                    arrow.setNoGravity(false);
                    // Get current direction from arrow velocity or recalculate
                    // Since we spawned them in a circle, we know the vector relative to center
                    Vec dir = arrow.getPosition().sub(getPosition().add(0, 0.5, 0)).asVec().normalize();
                    arrow.setVelocity(dir.mul(ARROW_SPEED * 20)); // Minestom Ticks
                }
            }
            
            TKit.playSound(getInstance(), getPosition(), "entity.arrow.shoot", net.kyori.adventure.sound.Sound.Source.NEUTRAL, 2f, 1f);
            remove(); // Remove sheep
            
        }).delay(TaskSchedule.tick(SHOOT_DELAY_TICKS)).schedule();
    }

    @Override
    public String getId() {
        return "hedgehog";
    }
}