package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class GretaSheep extends SheepProjectile {

    public GretaSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.GREEN);
            meta.setCustomName(Component.text("Mouton Greta", TextColor.color(0x228B22)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        if (getInstance() == null) return;
        
        Point pos = getPosition();
        Instance instance = getInstance();
        
        // Effects
        instance.sendGroupedPacket(new ParticlePacket(
                Particle.HAPPY_VILLAGER,
                pos,
                new Vec(0.5, 0.5, 0.5),
                0f, 20
        ));

        // Simple Tree Generation
        // Trunk
        for (int i = 0; i < 5; i++) {
             instance.setBlock(pos.add(0, i, 0), Block.MANGROVE_LOG);
        }
        
        // Leaves (Sphere)
        int radius = 2;
        Point leavesCenter = pos.add(0, 4, 0);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                     Point leafPos = leavesCenter.add(x, y, z);
                     if (leafPos.distanceSquared(leavesCenter) <= radius * radius) {
                         if (instance.getBlock(leafPos).isAir()) {
                             instance.setBlock(leafPos, Block.MANGROVE_LEAVES);
                         }
                     }
                }
            }
        }
        
        remove();
    }

    @Override
    public String getId() {
        return "greta";
    }
}