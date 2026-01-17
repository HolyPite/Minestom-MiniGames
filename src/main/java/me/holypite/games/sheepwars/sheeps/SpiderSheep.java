package me.holypite.games.sheepwars.sheeps;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.concurrent.ThreadLocalRandom;

public class SpiderSheep extends SheepProjectile {

    private static final float ACTIVATION_DELAY = 3;
    private static final double RADIUS = 4.0;
    private static final double WEB_CHANCE = 0.2;
    private static final int POISON_DURATION_TICKS = 4 * 20;

    public SpiderSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(ACTIVATION_DELAY);
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.GREEN);
            meta.setCustomName(Component.text("Spider Sheep", TextColor.fromHexString("#556B2F")));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void onLand() {
        activate();
    }

    private void activate() {
        if (isRemoved()) return;

        Point center = getPosition();

        // 1. Cobwebs
        int r = (int) Math.ceil(RADIUS);
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    Point pos = center.add(x, y, z);
                    if (pos.distance(center) <= RADIUS) {
                        if (getInstance().getBlock(pos) == Block.AIR && ThreadLocalRandom.current().nextDouble() < WEB_CHANCE) {
                            getInstance().setBlock(pos, Block.COBWEB);
                        }
                    }
                }
            }
        }

        // 2. Poison Players
        getInstance().getEntities().stream()
                .filter(e -> e instanceof Player)
                .filter(e -> e.getPosition().distance(center) <= RADIUS)
                .forEach(e -> ((LivingEntity) e).addEffect(new Potion(PotionEffect.POISON, (byte) 1, POISON_DURATION_TICKS))); // Poison II, 4s

        remove();
    }

    @Override
    public String getId() {
        return "spider";
    }
}
