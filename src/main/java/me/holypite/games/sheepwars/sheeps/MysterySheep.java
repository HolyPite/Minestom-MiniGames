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
            me.holypite.utils.TKit.spawnParticles(getInstance(), Particle.ENCHANT, getPosition().add(0, 0.5, 0), 0.5f, 0.5f, 0.5f, 0.1f, 5);
        }
    }

    @Override
    public void onLand() {
        // Transform into random sheep
        long elapsed = game != null ? game.getElapsedSeconds() : 0;
        Function<Entity, SheepProjectile> factory = SheepRegistry.getRandomSheepFactory(elapsed, java.util.List.of("mystery"));
        
        if (factory != null) {
            SheepProjectile randomSheep = factory.apply(shooter);
            randomSheep.setExplosionManager(explosionManager);
            if (game != null) randomSheep.setGame(game);
            
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
