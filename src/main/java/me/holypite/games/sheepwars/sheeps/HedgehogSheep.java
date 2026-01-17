package me.holypite.games.sheepwars.sheeps;

import me.holypite.manager.projectile.entities.ArrowProjectile;
import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
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
        
        // Freeze the sheep to prevent physics glitching with arrows
        setNoGravity(true);
        setVelocity(Vec.ZERO);
        
        List<ArrowProjectile> arrows = new ArrayList<>();
        
        // Spawn dome of arrows
        double phi = Math.PI * (3. - Math.sqrt(5.));

        for (int i = 0; i < ARROW_COUNT; i++) {
            double y = 1 - (i / (float) (ARROW_COUNT - 1)) * 2;
            double radius = Math.sqrt(1 - y * y);

            double theta = phi * i;

            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;
            
            Vec direction = new Vec(x, y, z).normalize();
            
            ArrowProjectile arrow = new ArrowProjectile(EntityType.ARROW, shooter);
            arrow.setNoGravity(true); // Suspend them
            
            net.minestom.server.coordinate.Point center = getPosition().add(0, 1.5, 0);
            net.minestom.server.coordinate.Point arrowPosPoint = center.add(direction.mul(SPHERE_RADIUS));
            
            // Calculate orientation
            // Minestom Pitch: -90 (Up) to 90 (Down)
            // atan2(y, horizontal) gives angle from horizontal.
            double horizontal = Math.sqrt(direction.x() * direction.x() + direction.z() * direction.z());
            float pitch = (float) -Math.toDegrees(Math.atan2(direction.y(), horizontal));
            float yaw = (float) -Math.toDegrees(Math.atan2(direction.x(), direction.z()));
            
            Pos spawnPos = Pos.fromPoint(arrowPosPoint).withView(yaw, pitch);
            
            arrow.setInstance(getInstance(), spawnPos);
            arrow.setVelocity(Vec.ZERO);
                        
            arrows.add(arrow);
        }
        
        TKit.playSound(getInstance(), getPosition(), "entity.arrow.hit_player", net.kyori.adventure.sound.Sound.Source.NEUTRAL, 1f, 0.5f);

        // Shoot them after delay
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (isRemoved()) { 
                // Handle cleanup if needed
            }
            
            for (ArrowProjectile arrow : arrows) {
                if (!arrow.isRemoved()) {
                    // Recalculate direction based on arrow position relative to center
                    // (Assuming sheep hasn't moved much, which is true since we froze it)
                    Vec dir = arrow.getPosition().asVec().sub(getPosition().add(0, 1.5, 0)).normalize();
                    arrow.setVelocity(dir.mul(ARROW_SPEED * 20)); 
                }
            }
            
            TKit.playSound(getInstance(), getPosition(), "entity.arrow.shoot", net.kyori.adventure.sound.Sound.Source.NEUTRAL, 2f, 1f);
            remove(); 
            
        }).delay(TaskSchedule.tick(SHOOT_DELAY_TICKS)).schedule();
    }

    @Override
    public String getId() {
        return "hedgehog";
    }
}
