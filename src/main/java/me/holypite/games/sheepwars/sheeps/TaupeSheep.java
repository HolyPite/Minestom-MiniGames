package me.holypite.games.sheepwars.sheeps;

import me.holypite.utils.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.block.Block;

public class TaupeSheep extends SheepProjectile {

    private static final int LIFETIME_TICKS = 5 * 20;
    private static final double GRAVITY = 0.005;
    private static final double DIG_RADIUS = 2.0;

    private int ticksLeft = LIFETIME_TICKS;

    public TaupeSheep(Entity shooter) {
        super(shooter);
        // Reduce gravity to make it fall slower (default is 0.08)
        getAttribute(Attribute.GRAVITY).setBaseValue(GRAVITY);

        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(net.minestom.server.color.DyeColor.BROWN);
            meta.setCustomName(Component.text("Taupe Sheep", NamedTextColor.GOLD));
            meta.setCustomNameVisible(true);
        }
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        // Custom Lifetime
        if (ticksLeft-- <= 0) {
            remove();
        }
    }

    @Override
    public void onLand() {
        // Do nothing, stay and dig
    }
    
    @Override
    protected void onFlightTick() {
        // Destroy blocks around while flying AND while on ground (digging)
        destroyBlocks(DIG_RADIUS);
    }

    private void destroyBlocks(double radius) {
        for (Point pos : TKit.getBlocksInSphere(getPosition(), radius)) {
            Block block = getInstance().getBlock(pos);
            if (block != Block.AIR && block != Block.BEDROCK) {
                getInstance().setBlock(pos, Block.AIR);
            }
        }
    }

    @Override
    public String getId() {
        return "taupe";
    }
}