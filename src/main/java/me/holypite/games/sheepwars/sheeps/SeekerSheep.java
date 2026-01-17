package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import net.minestom.server.entity.ai.goal.FollowTargetGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.List;

public class SeekerSheep extends SheepProjectile {

    private static final float MOVEMENT_SPEED = 0.3f;
    private static final float EXPLOSION_POWER = 3.0f;
    private static final int TIMEOUT_TICKS = 20 * 15;
    private static final double TRIGGER_RADIUS = 2.0;

    public SeekerSheep(Entity shooter) {
        super(shooter);
        
        // Speed is required for Navigator to work
        getAttribute(Attribute.MAX_HEALTH).setBaseValue(8f);
        setHealth(8f);
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(MOVEMENT_SPEED);

        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.PURPLE);
            meta.setCustomName(Component.text("Mouton Chercheur", TextColor.color(0x8A2BE2)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        // Use AI API instead of manual task
        addAIGroup(
                List.of(new FollowTargetGoal(this, Duration.ofMillis(500))),
                List.of(new ClosestEntityTarget(this, 30, Player.class))
        );
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (!landed || isRemoved()) return;

        // Explode on contact with any player
        TKit.getPlayersInRadius(getInstance(), getPosition(), TRIGGER_RADIUS, true).stream()
                .filter(p -> p != shooter)
                .findFirst()
                .ifPresent(p -> explode());

        // Timeout
        if (getAliveTicks() > TIMEOUT_TICKS) {
            explode();
        }
    }

    private void explode() {
        if (isRemoved()) return;
        if (explosionManager != null) {
            explosionManager.explode(getInstance(), getPosition(), EXPLOSION_POWER, true, shooter, this);
        } else {
            getInstance().explode((float) getPosition().x(), (float) getPosition().y(), (float) getPosition().z(), EXPLOSION_POWER, null);
        }
        remove();
    }

    @Override
    public String getId() {
        return "seeker";
    }
}