package me.holypite.games.sheepwars.sheeps;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.DyeColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

import java.util.List;

public class RainbowSheep extends SheepProjectile {

    private static final List<DyeColor> RAINBOW_COLORS = List.of(
            DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW,
            DyeColor.LIME, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA
    );
    
    private int colorIndex = 0;
    private int tickCounter = 0;

    public RainbowSheep(Entity shooter) {
        super(shooter);
        setNoGravity(true); // Override super's default
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setHasNoGravity(true);
            meta.setCustomName(Component.text("Mouton Arc-en-ciel", NamedTextColor.LIGHT_PURPLE));
            meta.setCustomNameVisible(true);
            meta.setColor(DyeColor.RED);
        }
    }

    @Override
    public void shoot(double power) {
        super.shoot(power);
        
        // Constant slow velocity like GluttonSheep
        Vec initialVelocity = getVelocity();
        double speed = 3.0; // Slightly faster than Glutton (5) to cover more distance
        
        setVelocity(initialVelocity.normalize().mul(speed));
        
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();
            
            // Lifetime check: 4 seconds (4 * 20 ticks)
            if (getAliveTicks() > 4 * 20) {
                remove();
                return TaskSchedule.stop();
            }
            
            // Maintain constant velocity
            setVelocity(initialVelocity.normalize().mul(speed));
            
            // Logic previously in onFlightTick()
            if (tickCounter++ % 2 == 0) {
                updateColor();
            }
            createBridge();
            
            return TaskSchedule.tick(1);
        });
    }

    @Override
    protected void onFlightTick() {
        // Handled in the scheduler task in shoot()
    }
    
    private void updateColor() {
        colorIndex = (colorIndex + 1) % RAINBOW_COLORS.size();
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(RAINBOW_COLORS.get(colorIndex));
        }
    }
    
    private void createBridge() {
        Instance instance = getInstance();
        if (instance == null) return;
        
        Point pos = getPosition();
        Block blockAt = instance.getBlock(pos);
        
        // Only place if air to avoid destroying map
        if (blockAt.isAir()) {
            DyeColor color = RAINBOW_COLORS.get(colorIndex);
            // Construct the block name: minecraft:red_stained_glass
            String blockName = "minecraft:" + color.name().toLowerCase() + "_stained_glass";
            Block glassBlock = Block.fromKey(blockName);
            
            if (glassBlock != null) {
                instance.setBlock(pos, glassBlock);
                
                // Schedule removal after 15 seconds
                MinecraftServer.getSchedulerManager().buildTask(() -> {
                    // Only remove if it's still the same block (simple check)
                    // In a real game, you might want more robust checking or an owner tag
                    if (instance.getBlock(pos).compare(glassBlock)) {
                        instance.setBlock(pos, Block.AIR);
                    }
                }).delay(TaskSchedule.seconds(15)).schedule();
            }
        }
    }

    @Override
    public void onLand() {
        // Just vanish when hitting a wall or stopping
        remove();
    }

    @Override
    public String getId() {
        return "rainbow";
    }
}
