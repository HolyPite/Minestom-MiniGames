package me.holypite.games.sheepwars.sheeps;

import me.holypite.entity.AggressiveBee;
import me.holypite.entity.AggressiveLarva;
import me.holypite.entity.AggressiveSlime;
import me.holypite.games.sheepwars.SheepRegistry;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class CloneSheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 3;
    private static final double RADIUS = 5.0;

    public CloneSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.CYAN);
            meta.setCustomName(Component.text("Clone Sheep", TextColor.fromHexString("#40E0D0")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        activate();
    }

    private void activate() {
        if (getInstance() == null || isRemoved()) return;

        List<Entity> nearby = TKit.getEntitiesInRadius(getInstance(), getPosition(), RADIUS);
        
        for (Entity e : nearby) {
            if (e == this) continue;
            
            Point randomPos = getRandomPosition(getPosition(), RADIUS);

            // Clone Sheep or Monsters
            if (e.getEntityType() == EntityType.SHEEP) {
                cloneSheep(e, randomPos);
            } else if (isMonster(e.getEntityType())) {
                cloneEntity(e, randomPos);
            }
        }

        remove();
    }

    private Point getRandomPosition(Point center, double radius) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double angle = rnd.nextDouble() * Math.PI * 2;
        double r = rnd.nextDouble() * radius;
        
        // Pick a random spot around center on the same Y (since we are on ground)
        return center.add(r * Math.cos(angle), 0.5, r * Math.sin(angle));
    }

    private void cloneSheep(Entity original, Point spawnPos) {
        if (original instanceof SheepProjectile sheep) {
            String id = sheep.getId();
            if (id.equals("clone")) return; // Don't clone clones
            
            ItemStack item = SheepRegistry.getSheepItemById(id);
            Optional<SheepRegistry.SheepEntry> entry = SheepRegistry.getSheepByItem(item);
            
            if (entry.isPresent()) {
                SheepProjectile clone = entry.get().factory().apply(shooter);
                clone.setInstance(getInstance(), spawnPos);
                
                // Visual effect at spawn
                playCloneEffect(spawnPos);
            }
        }
    }

    private void cloneEntity(Entity original, Point spawnPos) {
        Entity clone;
        
        if (original.getEntityType() == EntityType.BEE) {
            clone = new AggressiveBee();
        } else if (original.getEntityType() == EntityType.SLIME) {
            int size = 1;
            if (original instanceof AggressiveSlime slime && slime.getEntityMeta() instanceof SlimeMeta meta) {
                size = meta.getSize();
            } else if (original.getEntityMeta() instanceof SlimeMeta meta) {
                size = meta.getSize();
            }
            clone = new AggressiveSlime(size);
        } else if (original.getEntityType() == EntityType.SILVERFISH || original.getEntityType() == EntityType.ENDERMITE) {
            clone = new AggressiveLarva(original.getEntityType());
        } else {
            // Fallback for vanilla mobs or unknown types
            clone = new Entity(original.getEntityType());
        }
        
        clone.setInstance(getInstance(), spawnPos);
        playCloneEffect(spawnPos);
    }

    private void playCloneEffect(Point pos) {
        if (getInstance() == null) return;
        TKit.spawnParticles(getInstance(), Particle.PORTAL, pos.add(0, 0.5, 0), 0.2f, 0.2f, 0.2f, 0.1f, 30);
    }

    private boolean isMonster(EntityType type) {
        return type == EntityType.ZOMBIE || type == EntityType.SKELETON || type == EntityType.CREEPER || 
               type == EntityType.SLIME || type == EntityType.BEE || type == EntityType.ENDERMITE || type == EntityType.SILVERFISH;
    }

    @Override
    public String getId() {
        return "clone";
    }
}
