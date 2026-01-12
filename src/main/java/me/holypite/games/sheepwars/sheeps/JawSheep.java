package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class JawSheep extends SheepProjectile {

    public JawSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.BROWN);
            meta.setCustomName(Component.text("Mouton Mâchoire", TextColor.color(0x800000)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            TKit.getPlayersInRadius(getInstance(), getPosition(), 8, true).forEach(p -> {
                 Entity fangs = new Entity(EntityType.EVOKER_FANGS);
                 fangs.setInstance(getInstance(), p.getPosition());
                 
                 MinecraftServer.getSchedulerManager().buildTask(() -> {
                     if (p.getInstance() != null && p.getPosition().distance(fangs.getPosition()) < 1.5) {
                         p.damage(net.minestom.server.entity.damage.DamageType.MAGIC, 6);
                     }
                     fangs.remove();
                 }).delay(TaskSchedule.tick(10)).schedule();
            });
            
            getInstance().sendGroupedPacket(new ParticlePacket(
                    Particle.LARGE_SMOKE,
                    getPosition(),
                    new Vec(0.5, 0.5, 0.5),
                    0f, 20
            ));
            
            remove();
        }).delay(TaskSchedule.seconds(2)).schedule();
    }

    @Override
    public String getId() {
        return "jaw";
    }
}