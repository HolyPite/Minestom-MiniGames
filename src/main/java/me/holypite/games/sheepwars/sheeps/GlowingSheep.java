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

    private static final float ACTIVATION_DELAY = 1;
    private static final double SEARCH_RADIUS = 10.0;
    private static final int EFFECT_DURATION = 200;

    public GlowingSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.YELLOW);
            meta.setCustomName(Component.text("Mouton Glowing", TextColor.color(0xFFFFE0)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
            TKit.getPlayersInRadius(getInstance(), getPosition(), SEARCH_RADIUS, true).forEach(p -> {
                 p.addEffect(new Potion(PotionEffect.GLOWING, (byte)0, EFFECT_DURATION));
            });
            
            TKit.spawnParticles(getInstance(), Particle.END_ROD, getPosition(), 0.5f, 0.5f, 0.5f, 0f, 50);
            
            remove();
    }

    @Override
    public String getId() {
        return "glowing";
    }
}