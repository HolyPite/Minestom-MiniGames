package me.holypite.games.sheepwars.sheeps;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ExplosiveSheep extends SheepProjectile {

    private static final float EXPLOSION_POWER = 3.0f;

    public ExplosiveSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(3);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.RED);
            meta.setCustomName(Component.text("Explosive Sheep", NamedTextColor.RED));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        explode();
    }

    private void explode() {
        if (isRemoved()) return;
        
        if (explosionManager != null) {
            explosionManager.explode(getInstance(), getPosition(), EXPLOSION_POWER, true, shooter, this);
        } else {
            getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), EXPLOSION_POWER, null);
        }
        remove();
    }

    @Override
    public String getId() {
        return "explosive";
    }
}
