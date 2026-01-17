package me.holypite.games.sheepwars.sheeps;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class RadioactiveSheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 1;
    private static final int DURATION = 20 * 10;
    private static final Vec RADIUS_VEC = new Vec(3.0, 2.0, 3.0);

    public RadioactiveSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.LIME);
            meta.setCustomName(Component.text("Mouton Radioactif", TextColor.color(0xADFF2F)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        if (getInstance() == null) return;
        
        me.holypite.utils.TKit.spawnFakeEffectCloud3D(
            getInstance(),
            getPosition(),
            RADIUS_VEC, // 3D Ellipsoid
            DURATION, // 10 seconds
            Particle.WITCH,
            new PotionEffect[]{PotionEffect.WITHER},
            new short[]{100},
            new byte[]{1}
        );
        
        remove();
    }

    @Override
    public String getId() {
        return "radioactive";
    }
}