package me.holypite.games.sheepwars.sheeps;

import me.holypite.manager.projectile.entities.ArrowProjectile;
import me.holypite.utils.TKit;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.color.DyeColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class HedgehogSheep extends SheepProjectile {

    private static final int LIFETIME_SECONDS = 5;
    private static final double SEARCH_RADIUS = 10.0;
    private static final double ARROW_SPEED = 2.0;
    private static final double ARROW_SPREAD = 1.0;

    public HedgehogSheep(Entity shooter) {
        super(shooter);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(DyeColor.BROWN);
            meta.setCustomName(Component.text("Mouton Hérisson", TextColor.color(0xB8860B)));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();
            if (getAliveTicks() > 20 * LIFETIME_SECONDS) {
                remove();
                return TaskSchedule.stop();
            }
            
            // Shoot arrows every second
            if (getAliveTicks() % 20 == 0) {
                 TKit.getPlayersInRadius(getInstance(), getPosition(), SEARCH_RADIUS, true).forEach(p -> {
                     if (game != null && game.isSameTeam(shooter, p)) return;

                     net.minestom.server.coordinate.Vec direction = p.getPosition().add(0, 1, 0).sub(getPosition().add(0, 1, 0)).asVec().normalize();
                     net.minestom.server.coordinate.Point spawnPos = getPosition().add(0, 1, 0).add(direction.mul(1.5)); // Spawn 1.5 blocks away

                     ArrowProjectile arrow = new ArrowProjectile(EntityType.ARROW, shooter);
                     arrow.shoot(getInstance(), spawnPos, p.getPosition().add(0, 1, 0), ARROW_SPEED, ARROW_SPREAD);
                 });
            }
            
            return TaskSchedule.tick(1);
        });
    }

    @Override
    public String getId() {
        return "hedgehog";
    }
}