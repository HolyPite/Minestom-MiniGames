package me.holypite.games.sheepwars.sheeps;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ExplosiveSheep extends SheepProjectile {

    public ExplosiveSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.RED);
            meta.setCustomName(Component.text("Explosive Sheep", NamedTextColor.RED));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        // Start Countdown
        MinecraftServer.getSchedulerManager().buildTask(this::explode)
                .delay(TaskSchedule.seconds(3))
                .schedule();
    }

    private void explode() {
        if (isRemoved()) return;
        
        // Manual explosion logic or usage of ExplosionManager if available
        // Since we are in an entity, we don't have direct access to Game's explosionManager easily without static access or injection.
        // For now, I'll do a raw explosion effect + damage to show it works.
        // Ideally, we should pass ExplosionManager to the Sheep constructor.
        
        getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), 3.0f, null);
        remove();
    }

    @Override
    public String getId() {
        return "explosive";
    }
}
