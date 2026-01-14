package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class GlowingSheep extends SheepProjectile {

    public GlowingSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(1);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.YELLOW);
            meta.setCustomName(Component.text("Mouton Glowing", TextColor.color(0xFFFFE0)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
            TKit.getPlayersInRadius(getInstance(), getPosition(), 10, true).forEach(p -> {
                 p.addEffect(new Potion(PotionEffect.GLOWING, (byte)0, 200));
            });
            
            getInstance().sendGroupedPacket(new ParticlePacket(
                    Particle.END_ROD,
                    getPosition(),
                    new Vec(0.5, 0.5, 0.5),
                    0f, 50
            ));
            
            remove();
    }

    @Override
    public String getId() {
        return "glowing";
    }
}