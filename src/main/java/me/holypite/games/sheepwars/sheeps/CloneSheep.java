package me.holypite.games.sheepwars.sheeps;

import me.holypite.games.sheepwars.SheepRegistry;
import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class CloneSheep extends SheepProjectile {

    public CloneSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.CYAN);
            meta.setCustomName(Component.text("Clone Sheep", TextColor.fromHexString("#40E0D0")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().buildTask(this::activate)
                .delay(TaskSchedule.seconds(3))
                .schedule();
    }

    private void activate() {
        if (isRemoved()) return;

        double radius = 5.0;
        List<Entity> nearby = TKit.getEntitiesInRadius(getInstance(), getPosition(), radius);
        
        for (Entity e : nearby) {
            if (e == this) continue;
            
            // Clone Sheep or Monsters
            if (e.getEntityType() == EntityType.SHEEP) {
                cloneSheep(e);
            } else if (isMonster(e.getEntityType())) {
                cloneEntity(e);
            }
        }

        remove();
    }

    private void cloneSheep(Entity original) {
        if (original instanceof SheepProjectile sheep) {
            String id = sheep.getId();
            if (id.equals("clone")) return; // Don't clone clones
            
            ItemStack item = SheepRegistry.getSheepItemById(id);
            Optional<SheepRegistry.SheepEntry> entry = SheepRegistry.getSheepByItem(item);
            
            if (entry.isPresent()) {
                SheepProjectile clone = entry.get().factory().apply(shooter); // Owner is me? or original owner? Let's say me (shooter of clone sheep)
                clone.setInstance(getInstance(), original.getPosition());
                // No velocity, just spawn
            }
        }
    }

    private void cloneEntity(Entity original) {
        Entity clone = new Entity(original.getEntityType());
        clone.setInstance(getInstance(), original.getPosition());
    }

    private boolean isMonster(EntityType type) {
        // Basic list of monsters to clone
        return type == EntityType.ZOMBIE || type == EntityType.SKELETON || type == EntityType.CREEPER || 
               type == EntityType.SLIME || type == EntityType.BEE || type == EntityType.ENDERMITE || type == EntityType.SILVERFISH;
    }

    @Override
    public String getId() {
        return "clone";
    }
}
