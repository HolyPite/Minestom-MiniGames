package me.holypite.games.sheepwars.sheeps;

import me.holypite.games.sheepwars.SheepRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.particle.Particle;

import java.util.function.Function;

public class MysterySheep extends SheepProjectile {

    public MysterySheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.YELLOW);
            meta.setCustomName(Component.text("Mystery Sheep", NamedTextColor.YELLOW));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    protected void onFlightTick() {
        // Particles
        if (getAliveTicks() % 5 == 0) {
            getInstance().sendGroupedPacket(new net.minestom.server.network.packet.server.play.ParticlePacket(
                    Particle.ENCHANT,
                    getPosition().add(0, 0.5, 0),
                    new net.minestom.server.coordinate.Vec(0.5, 0.5, 0.5),
                    0.1f, 5
            ));
        }
    }

    @Override
    public void onLand() {
        // Transform into random sheep
        Function<Entity, SheepProjectile> factory = SheepRegistry.getRandomSheepFactory();
        if (factory != null) {
            SheepProjectile randomSheep = factory.apply(shooter);
            randomSheep.setInstance(getInstance(), getPosition());
            
            // Trigger landing logic immediately
            randomSheep.onLand();
        }
        
        remove();
    }

    @Override
    public String getId() {
        return "mystery";
    }
}
