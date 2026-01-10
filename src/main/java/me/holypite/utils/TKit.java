package me.holypite.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TKit {

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

    public static Component createGradientText(String text, TextColor startColor, TextColor endColor) {
        int length = text.length();
        Component gradientText = Component.empty();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            int red = interpolate(startColor.red(), endColor.red(), ratio);
            int green = interpolate(startColor.green(), endColor.green(), ratio);
            int blue = interpolate(startColor.blue(), endColor.blue(), ratio);

            TextColor color = TextColor.color(red, green, blue);
            gradientText = gradientText.append(Component.text(String.valueOf(text.charAt(i))).color(color));
        }
        return gradientText;
    }

    private static int interpolate(int start, int end, float ratio) {
        return (int) (start + (end - start) * ratio);
    }

    public static boolean chance(double chance) {
        return ThreadLocalRandom.current().nextDouble() < chance;
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

    public static List<Entity> getEntitiesInRadius(Instance instance, Point center, double radius) {
        if (instance == null) return new ArrayList<>();
        // Minestom doesn't have a direct "getNearbyEntities" like Bukkit easily accessible on instance without chunk check.
        // But we can iterate over entities or use chunk lookups.
        // Optimization: Use getEntities() and filter distance.
        // For production: Use spatial partition or chunk-based search.
        
        // Simple implementation:
        return instance.getEntities().stream()
                .filter(e -> e.getPosition().distanceSquared(center) <= radius * radius)
                .collect(Collectors.toList());
    }

    public static List<Player> getPlayersInRadius(Instance instance, Point center, double radius, boolean ignoreSpectators) {
        return getEntitiesInRadius(instance, center, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .filter(p -> !ignoreSpectators || p.getGameMode() != GameMode.SPECTATOR)
                .collect(Collectors.toList());
    }

    public static Vec getDirection(Point from, Point to) {
        return to.sub(from).asVec().normalize();
    }
}
