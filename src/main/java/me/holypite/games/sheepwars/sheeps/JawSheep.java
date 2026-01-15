package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.sound.Sound;

public class JawSheep extends SheepProjectile {

    public JawSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(2);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.BROWN);
            meta.setCustomName(Component.text("Mouton Mâchoire", TextColor.color(0x800000)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        if (getInstance() == null) return;

        TKit.getPlayersInRadius(getInstance(), getPosition(), 8, true).forEach(p -> {
            // Create Fangs
            Entity fangs = new Entity(EntityType.EVOKER_FANGS);
            
            // Set rotation to face the player or just default
            // Evoker fangs usually spawn slightly in front or at player feet
            fangs.setInstance(getInstance(), p.getPosition());
            
            // Trigger Bite Animation (Status 4 in Minecraft Protocol)
            // This is essential for visibility and the "bite" look
            fangs.triggerStatus((byte) 4);
            
            // Play Sound
            TKit.playSound(getInstance(), p.getPosition(), "entity.evoker_fangs.attack", Sound.Source.HOSTILE, 1f, 1f);

            // Apply Damage after animation warmup (approx 10 ticks / 0.5s)
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (!p.isRemoved() && p.getInstance() != null) {
                    if (p.getPosition().distanceSquared(fangs.getPosition()) < 2*2) {
                        p.damage(me.holypite.manager.damage.DamageSources.magic(6.0f));
                    }
                }
                fangs.remove();
            }).delay(TaskSchedule.tick(10)).schedule();
        });

        // Visual cloud at sheep location
        TKit.spawnParticles(getInstance(), Particle.LARGE_SMOKE, getPosition(), 0.5f, 0.5f, 0.5f, 0.05f, 20);
        
        remove();
    }

    @Override
    public String getId() {
        return "jaw";
    }
}
