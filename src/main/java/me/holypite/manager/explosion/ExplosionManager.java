package me.holypite.manager.explosion;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;

public class ExplosionManager {

    /**
     * Triggers an explosion at the specified location.
     *
     * @param instance    The instance where the explosion happens.
     * @param point       The center of the explosion.
     * @param strength    The radius/power.
     * @param breakBlocks Whether blocks should be destroyed.
     */
    public void explode(Instance instance, Point point, float strength, boolean breakBlocks) {
        GameExplosion explosion = new GameExplosion(
                (float) point.x(), (float) point.y(), (float) point.z(),
                strength, breakBlocks, false
        );
        explosion.apply(instance);
    }

    /**
     * Returns a supplier to be used with Instance#setExplosionSupplier.
     */
    public ExplosionSupplier getSupplier(boolean breakBlocks) {
        return (centerX, centerY, centerZ, strength, additionalData) -> 
                new GameExplosion(centerX, centerY, centerZ, strength, breakBlocks, false);
    }
}
