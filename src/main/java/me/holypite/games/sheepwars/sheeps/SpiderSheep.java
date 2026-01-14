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

    public SpiderSheep(Entity shooter) {
        super(shooter);
        setActivationDelay(3);
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

        double radius = 4.0;
        Point center = getPosition();

        // 1. Cobwebs
        int r = (int) Math.ceil(radius);
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    Point pos = center.add(x, y, z);
                    if (pos.distance(center) <= radius) {
                        if (getInstance().getBlock(pos) == Block.AIR && ThreadLocalRandom.current().nextDouble() < 0.2) {
                            getInstance().setBlock(pos, Block.COBWEB);
                        }
                    }
                }
            }
        }

        // 2. Poison Players
        getInstance().getEntities().stream()
                .filter(e -> e instanceof Player)
                .filter(e -> e.getPosition().distance(center) <= radius)
                .forEach(e -> ((LivingEntity) e).addEffect(new Potion(PotionEffect.POISON, (byte) 1, 4 * 20))); // Poison II, 4s

        remove();
    }

    @Override
    public String getId() {
        return "spider";
    }
}
