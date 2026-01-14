package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class FragmentationSheep extends SheepProjectile {

    public FragmentationSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(1);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.ORANGE);
            meta.setCustomName(Component.text("Mouton Fragmentation", TextColor.color(0xFF8C00)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
            // Main explosion
            getInstance().explode((float)getPosition().x(), (float)getPosition().y(), (float)getPosition().z(), 2.5f, null);
            
            // Spawn cluster
            int count = 8;
            for (int i = 0; i < count; i++) {
                double angle = i * (2 * Math.PI / count);
                double x = Math.cos(angle);
                double z = Math.sin(angle);
                
                InstantSheep baby = new InstantSheep(shooter);
                baby.setInstance(getInstance(), getPosition().add(0, 1, 0));
                baby.setVelocity(new Vec(x, 0.5, z).normalize().mul(15));
            }
            
            remove();
    }

    @Override
    public String getId() {
        return "fragmentation";
    }
}