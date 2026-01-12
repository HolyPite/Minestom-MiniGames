package me.holypite.games.sheepwars.sheeps;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;

public class InstantSheep extends SheepProjectile {

    public InstantSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.RED);
            meta.setCustomName(Component.text("Mouton Instantané"));
            meta.setBaby(true); // Baby sheep
        }
    }

    @Override
    public void onLand() {
        if (getInstance() != null) {
            getInstance().explode((float)getPosition().x(), (float)getPosition().y(), (float)getPosition().z(), 2f, null);
        }
        remove();
    }

    @Override
    public String getId() {
        return "instant";
    }
}
