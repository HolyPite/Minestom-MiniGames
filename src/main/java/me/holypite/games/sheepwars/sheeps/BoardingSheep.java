package me.holypite.games.sheepwars.sheeps;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;

public class BoardingSheep extends SheepProjectile {

    public BoardingSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(1);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.BLUE); // Blue/Cyan like wool manager
            meta.setCustomName(Component.text("Boarding Sheep", NamedTextColor.BLUE));
            meta.setCustomNameVisible(true);
        }
    }
    
    @Override
    public void shoot(double power) {
        // Override power to 60 (insane speed)
        super.shoot(power);
        // Board the shooter!
        if (shooter instanceof Player player) {
            this.addPassenger(player);
        }
    }

    @Override
    public void onLand() {
        // Just stop
        remove();
    }

    @Override
    public String getId() {
        return "boarding";
    }
}
