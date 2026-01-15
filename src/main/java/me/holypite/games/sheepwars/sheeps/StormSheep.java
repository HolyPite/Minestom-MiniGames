package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class StormSheep extends SheepProjectile {

    public StormSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(3);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.GRAY); // Or CYAN/LIGHT_BLUE mix
            meta.setCustomName(Component.text("Storm Sheep", TextColor.fromHexString("#708090")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        activate();
    }

    private void activate() {
        if (isRemoved()) return;

        double radius = 6.0;
        List<Player> players = TKit.getPlayersInRadius(getInstance(), getPosition(), radius, true);
        
        for (Player p : players) {
            // Lightning
            Entity lightning = new Entity(EntityType.LIGHTNING_BOLT);
            lightning.setInstance(getInstance(), p.getPosition());
            // Lightning auto-removes usually or we remove it? Minestom lightning is just visual/sound if no logic
            // But EntityType.LIGHTNING_BOLT should play sound.
            
            // Push away
            Vec direction = p.getPosition().sub(getPosition()).asVec().normalize();
            p.setVelocity(p.getVelocity().add(direction.mul(30.0).withY(10.0)));
        }

        // Visual Cloud
        TKit.spawnParticles(getInstance(), Particle.CLOUD, getPosition().add(0, 1, 0), 1, 0.5f, 1, 0.1f, 50);

        remove();
    }

    @Override
    public String getId() {
        return "storm";
    }
}
