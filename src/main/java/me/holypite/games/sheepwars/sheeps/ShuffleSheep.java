package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ShuffleSheep extends SheepProjectile {

    public ShuffleSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.PINK); 
            meta.setCustomName(Component.text("Mouton Shuffle", TextColor.color(0xFF69B4)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (getInstance() == null) return;
            
            Instance instance = getInstance();
            List<Point> blocks = TKit.getBlocksInSphere(getPosition(), 10);
            
            // Filter safe spots (Solid block with 2 air blocks above)
            List<Point> safeSpots = blocks.stream().filter(p -> 
                instance.getBlock(p).isSolid() && 
                instance.getBlock(p.add(0, 1, 0)).isAir() && 
                instance.getBlock(p.add(0, 2, 0)).isAir()
            ).collect(Collectors.toList());
            
            if (!safeSpots.isEmpty()) {
                TKit.getPlayersInRadius(instance, getPosition(), 10, true).forEach(p -> {
                    Point randomSpot = safeSpots.get(ThreadLocalRandom.current().nextInt(safeSpots.size()));
                    p.teleport(new Pos(randomSpot.x() + 0.5, randomSpot.y() + 1, randomSpot.z() + 0.5));
                    
                    instance.sendGroupedPacket(new ParticlePacket(
                            Particle.PORTAL,
                            p.getPosition(),
                            new Vec(0.5, 1, 0.5),
                            0f, 50
                    ));
                });
            }
            
            remove();
        }).delay(TaskSchedule.seconds(2)).schedule();
    }

    @Override
    public String getId() {
        return "shuffle";
    }
}