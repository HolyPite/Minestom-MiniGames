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
        teleport(getPosition().add(0,-1,0));
        
        // Constant slow velocity like GluttonSheep
        Vec initialVelocity = getVelocity();
        double speed = 4.0; // Slightly faster than Glutton (5) to cover more distance
        
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
            if (tickCounter++ % 5 == 0) {
                updateColor();
            }
            createBridge();
            
            return TaskSchedule.tick(1);
        });
    }

    @Override
    protected void onFlightTick() {}
    
    private void updateColor() {
        colorIndex = (colorIndex + 1) % RAINBOW_COLORS.size();
        if (getEntityMeta() instanceof SheepMeta meta) {
            meta.setColor(RAINBOW_COLORS.get(colorIndex));
        }
    }
    
    private void createBridge() {
        Instance instance = getInstance();
        if (instance == null) return;
        
        Point centerPos = getPosition();
        Vec velocity = getVelocity();
        
        // Calculate perpendicular vector for width (Cross product with UP)
        // If velocity is vertical (rare), this might be weird, but for this sheep it's fine.
        Vec direction = velocity.normalize();
        Vec right = direction.cross(new Vec(0, 1, 0)).normalize();
        
        // Loop for 3 blocks: Center, Left, Right
        // Offsets: 0 (Center), 1 (Right), -1 (Left)
        for (int i = -1; i <= 1; i++) {
            Point targetPos = centerPos.add(right.mul(i));
            
            // Snap to block grid
            targetPos = new net.minestom.server.coordinate.Pos(
                Math.floor(targetPos.x()),
                Math.floor(targetPos.y()),
                Math.floor(targetPos.z())
            );

            Block blockAt = instance.getBlock(targetPos);
            
            // Only place if air to avoid destroying map
            if (blockAt.isAir()) {
                DyeColor color = RAINBOW_COLORS.get(colorIndex);
                String blockName = "minecraft:" + color.name().toLowerCase() + "_stained_glass";
                Block glassBlock = Block.fromKey(blockName);
                
                if (glassBlock != null) {
                    instance.setBlock(targetPos, glassBlock);
                    
                    // Schedule removal after 15 seconds
                    final Point finalPos = targetPos;
                    MinecraftServer.getSchedulerManager().buildTask(() -> {
                        if (instance.getBlock(finalPos).compare(glassBlock)) {
                            instance.setBlock(finalPos, Block.AIR);
                        }
                    }).delay(TaskSchedule.seconds(15)).schedule();
                }
            }
        }
    }

    @Override
    public void onLand() {}

    @Override
    public String getId() {
        return "rainbow";
    }
}
