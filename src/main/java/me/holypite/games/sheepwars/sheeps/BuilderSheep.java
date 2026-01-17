package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.color.DyeColor;

public class BuilderSheep extends SheepProjectile {

    public BuilderSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.LIME);
            meta.setCustomName(Component.text("Mouton Constructeur", NamedTextColor.GREEN));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        if (isRemoved()) return;

        Instance gameWorld = getInstance();
        InstanceContainer sourceWorld = game.getSourceInstance();

        if (sourceWorld != null) {
            TKit.getBlocksInSphere(getPosition(), 6).forEach(pos -> {
                // Ensure chunk is loaded in source if needed (usually handled by getBlock)
                Block originalBlock = sourceWorld.getBlock(pos);
                Block currentBlock = gameWorld.getBlock(pos);
                
                // If different, restore to original
                if (!originalBlock.compare(currentBlock)) {
                    gameWorld.setBlock(pos, originalBlock);
                    
                    // Visuals
                    if (originalBlock.isAir()) {
                         // Removed something (e.g. wool placed by player)
                         TKit.spawnParticles(gameWorld, Particle.CLOUD, pos.add(0.5, 0.5, 0.5), 0, 0, 0, 0, 1);
                    } else {
                         // Restored a block
                         TKit.spawnParticles(gameWorld, Particle.HAPPY_VILLAGER, pos.add(0.5, 0.5, 0.5), 0.2f, 0.2f, 0.2f, 0, 1);
                    }
                }
            });
            
            TKit.playSound(gameWorld, getPosition(), "entity.experience_orb.pickup", net.kyori.adventure.sound.Sound.Source.NEUTRAL, 1.0f, 1.0f);
        }

        remove();
    }

    @Override
    public String getId() {
        return "builder";
    }
}
