package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

public class ThornySheep extends SheepProjectile {

    public ThornySheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.GREEN);
            meta.setCustomName(Component.text("Mouton Épineux", TextColor.color(0x008000)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (getInstance() == null) return;
            
            List<Point> blocks = TKit.getBlocksInSphere(getPosition(), 5);
            for (Point p : blocks) {
                if (TKit.chance(0.6) && getInstance().getBlock(p).isSolid() && getInstance().getBlock(p.add(0, 1, 0)).isAir()) {
                    getInstance().setBlock(p.add(0, 1, 0), Block.SWEET_BERRY_BUSH);
                }
            }
            
            getInstance().sendGroupedPacket(new ParticlePacket(
                    Particle.COMPOSTER,
                    getPosition(),
                    new Vec(1, 1, 1),
                    0f, 30
            ));
            
            remove();
        }).delay(TaskSchedule.seconds(1)).schedule();
    }

    @Override
    public String getId() {
        return "thorny";
    }
}