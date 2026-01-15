package me.holypite.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.color.DyeColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TKit {

    // --- Audio & Visuals ---

    public static void playSound(Instance inst, Point pos, String soundName, Sound.Source source, float volume, float pitch) {
        if (inst == null) return;
        inst.playSound(Sound.sound(Key.key(soundName), source, volume, pitch), pos.x(), pos.y(), pos.z());
    }

    public static void spawnParticles(Instance inst, Particle particle, Point pos, float offsetX, float offsetY, float offsetZ, float maxSpeed, int number) {
        if (inst == null) return;
        inst.sendGroupedPacket(new ParticlePacket(particle, pos.x(), pos.y(), pos.z(), offsetX, offsetY, offsetZ, maxSpeed, number));
    }

    public static void sendStyledMessage(Player player, String message, TextColor color) {
        player.sendMessage(Component.text(message).color(color));
    }

    public static Component createGradientText(String text, TextColor startColor, TextColor endColor) {
        int length = text.length();
        Component gradientText = Component.empty();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / Math.max(1, length - 1);
            int red = interpolate(startColor.red(), endColor.red(), ratio);
            int green = interpolate(startColor.green(), endColor.green(), ratio);
            int blue = interpolate(startColor.blue(), endColor.blue(), ratio);

            TextColor color = TextColor.color(red, green, blue);
            gradientText = gradientText.append(Component.text(String.valueOf(text.charAt(i))).color(color));
        }
        return gradientText;
    }

    public static String extractPlainText(Component component) {
        StringBuilder plainText = new StringBuilder();
        if (component instanceof TextComponent textComponent) {
            plainText.append(textComponent.content());
        }
        for (Component child : component.children()) {
            plainText.append(extractPlainText(child));
        }
        return plainText.toString();
    }

    private static int interpolate(int start, int end, float ratio) {
        return (int) (start + (end - start) * ratio);
    }

    // --- Items & Inventory ---

    public static void drop(Instance instance, ItemStack item, Point pos) {
        ItemEntity itemEntity = new ItemEntity(item);
        itemEntity.setPickupDelay(Duration.ofMillis(500));
        itemEntity.setInstance(instance, pos);
    }

    public static void dropItemsInCircle(Instance instance, Point center, ItemStack[] items, double radius) {
        if (items.length == 0) return;
        double angleStep = 360.0 / items.length;
        for (int i = 0; i < items.length; i++) {
            double angle = Math.toRadians(i * angleStep);
            double x = center.x() + radius * Math.cos(angle);
            double z = center.z() + radius * Math.sin(angle);
            Pos dropPos = new Pos(x, center.y(), z);
            drop(instance, items[i], dropPos);
        }
    }

    public static void giveItems(Player player, ItemStack... items) {
        for (ItemStack item : items) {
            boolean added = player.getInventory().addItemStack(item);
            if (!added) {
                drop(player.getInstance(), item, player.getPosition());
            }
        }
    }

    public static DyeColor getRandomDyeColor() {
        DyeColor[] colors = DyeColor.values();
        return colors[ThreadLocalRandom.current().nextInt(colors.length)];
    }

    // --- Math & Utility ---

    public static boolean chance(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    public static double distanceSquared(Point a, Point b) {
        return a.distanceSquared(b);
    }

    public static double distance(Point a, Point b) {
        return a.distance(b);
    }

    public static Vec getDirection(Point from, Point to) {
        return to.sub(from).asVec().normalize();
    }

    // --- World & Blocks ---

    public static Block getBlockUnder(Instance instance, Point pos) {
        return instance.getBlock(pos.sub(0, 1, 0));
    }

    public static Block getBlockAbove(Instance instance, Point pos) {
        return instance.getBlock(pos.add(0, 1, 0));
    }

    public static List<Point> getBlocksInSphere(Point center, double radius) {
        List<Point> blocks = new ArrayList<>();
        double radiusSquared = radius * radius;
        int r = (int) Math.ceil(radius);

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    Point blockPos = center.add(x, y, z);
                    if (blockPos.distanceSquared(center) <= radiusSquared) {
                        blocks.add(blockPos);
                    }
                }
            }
        }
        return blocks;
    }

    public static List<Block> getBlocksInCube(Instance instance, Point center, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(instance.getBlock(center.add(x, y, z)));
                }
            }
        }
        return blocks;
    }

    public static boolean areAllBlocksAir(List<Block> blocks) {
        for (Block block : blocks) {
            if (!block.isAir()) return false;
        }
        return true;
    }

    // --- Entities ---

    public static List<Entity> getEntitiesInRadius(Instance instance, Point center, double radius) {
        if (instance == null) return new ArrayList<>();
        double radiusSq = radius * radius;
        // Optimization: Use getEntities() and filter distance.
        // For production: Use spatial partition or chunk-based search if available.
        return instance.getEntities().stream()
                .filter(e -> e.getPosition().distanceSquared(center) <= radiusSq)
                .collect(Collectors.toList());
    }

    public static List<LivingEntity> getLivingEntitiesInRadius(Instance instance, Point center, double radius) {
        if (instance == null) return new ArrayList<>();
        double radiusSq = radius * radius;
        return instance.getEntities().stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> !(e instanceof Player p) || p.getGameMode() != GameMode.SPECTATOR)
                .filter(e -> e.getPosition().distanceSquared(center) <= radiusSq)
                .collect(Collectors.toList());
    }

    public static List<Player> getPlayersInRadius(Instance instance, Point center, double radius, boolean ignoreSpectators) {
        return getEntitiesInRadius(instance, center, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .filter(p -> !ignoreSpectators || p.getGameMode() != GameMode.SPECTATOR)
                .collect(Collectors.toList());
    }

    public static Player getNearestPlayer(Instance instance, Point center, double maxDistance, boolean ignoreSpectators) {
        Player nearest = null;
        double minDistanceSq = maxDistance * maxDistance;

        for (Player p : instance.getPlayers()) {
            if (ignoreSpectators && p.getGameMode() == GameMode.SPECTATOR) continue;

            double distSq = p.getPosition().distanceSquared(center);
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                nearest = p;
            }
        }
        return nearest;
    }
    
    public static void messageNearestPlayer(Instance instance, Point pos, String message) {
        Player player = getNearestPlayer(instance, pos, Double.MAX_VALUE, true);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    public static void spawnFakeEffectCloud2D(Instance inst,
                                            Point center,
                                            float radius,
                                            int lifetimeTicks,
                                            Particle particle,
                                            PotionEffect[] types,
                                            short[] dur,
                                            byte[] amp) {

        /* Repeated task every 2 ticks (~0.1s) */
        Task t = inst.scheduler().buildTask(() -> {

            // 1) Visual: Circle of particles (Flat)
            spawnParticles(inst, particle, center, radius / 2, 0.2f, radius / 2, 0, (int) (20 * radius * radius));

            // 2) Application: Effects
            getLivingEntitiesInRadius(inst, center, radius).forEach((le) -> {
                applyEffects(le, types, dur, amp);
            });

        }).repeat(TaskSchedule.tick(2))
                .schedule();

        /* Stop after lifetimeTicks */
        inst.scheduler()
                .buildTask(t::cancel)
                .delay(TaskSchedule.tick(lifetimeTicks))
                .schedule();
    }

    public static void spawnFakeEffectCloud3D(Instance inst,
                                              Point center,
                                              Vec radii,
                                              int lifetimeTicks,
                                              Particle particle,
                                              PotionEffect[] types,
                                              short[] dur,
                                              byte[] amp) {

        Task t = inst.scheduler().buildTask(() -> {

            // 1) Visual: Ellipsoid of particles
            // Offsets in X/Y/Z act as a gaussian spread for particles
            spawnParticles(inst, particle, center, (float) radii.x() / 2f, (float) radii.y() / 2f, (float) radii.z() / 2f, 0, (int) (10 * (radii.x() + radii.y() + radii.z())));

            // 2) Application: Effects inside Ellipsoid
            getLivingEntitiesInEllipsoid(inst, center, radii).forEach((le) -> {
                applyEffects(le, types, dur, amp);
            });

        }).repeat(TaskSchedule.tick(2))
                .schedule();

        inst.scheduler()
                .buildTask(t::cancel)
                .delay(TaskSchedule.tick(lifetimeTicks))
                .schedule();
    }

    public static List<LivingEntity> getLivingEntitiesInEllipsoid(Instance instance, Point center, Vec radii) {
        if (instance == null) return new ArrayList<>();
        // Pre-calculate max radius for fast spherical check
        double maxR = Math.max(radii.x(), Math.max(radii.y(), radii.z()));
        double maxRSq = maxR * maxR;

        return instance.getEntities().stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> !(e instanceof Player p) || p.getGameMode() != GameMode.SPECTATOR)
                // 1. Fast Bounding Sphere Check
                .filter(e -> e.getPosition().distanceSquared(center) <= maxRSq)
                // 2. Precise Ellipsoid Check: (x/rx)^2 + (y/ry)^2 + (z/rz)^2 <= 1
                .filter(e -> {
                    Point p = e.getPosition();
                    double dx = (p.x() - center.x()) / radii.x();
                    double dy = (p.y() - center.y()) / radii.y();
                    double dz = (p.z() - center.z()) / radii.z();
                    return (dx * dx + dy * dy + dz * dz) <= 1.0;
                })
                .collect(Collectors.toList());
    }

    private static void applyEffects(LivingEntity le, PotionEffect[] types, short[] dur, byte[] amp) {
        if (types == null || dur == null || amp == null) return;
        int len = Math.min(types.length, Math.min(dur.length, amp.length));
        
        for (int i = 0; i < len; i++) {
            // Minestom Potion constructor: Potion(PotionEffect effect, byte amplifier, int duration)
            le.addEffect(new Potion(types[i], amp[i], dur[i]));
        }
    }
}