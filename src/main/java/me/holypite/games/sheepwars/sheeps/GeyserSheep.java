package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class GeyserSheep extends SheepProjectile {

    public GeyserSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.LIGHT_BLUE);
            meta.setCustomName(Component.text("Geyser Sheep", TextColor.fromHexString("#1E90FF")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().buildTask(this::activate)
                .delay(TaskSchedule.seconds(3))
                .schedule();
    }

    private void activate() {
        if (isRemoved()) return;

        double radius = 8.0;
        List<Player> players = TKit.getPlayersInRadius(getInstance(), getPosition(), radius, true);
        
        for (Player p : players) {
            // Project Up
            p.setVelocity(new Vec(0, 50, 0)); // Strong upward velocity
            
            // Particles
            getInstance().sendGroupedPacket(new net.minestom.server.network.packet.server.play.ParticlePacket(
                    Particle.SPLASH,
                    p.getPosition(),
                    new Vec(0.5, 0.5, 0.5),
                    0.1f, 50
            ));

            // Project Down after 1s
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (!p.isRemoved()) {
                    p.setVelocity(p.getVelocity().add(0, -100, 0)); // Slam down
                }
            }).delay(TaskSchedule.seconds(1)).schedule();
        }

        remove();
    }

    @Override
    public String getId() {
        return "geyser";
    }
}
