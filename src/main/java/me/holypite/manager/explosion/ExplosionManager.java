package me.holypite.manager.explosion;

import me.holypite.model.Game;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;

public class ExplosionManager {

    private Game game;

    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Triggers an explosion at the specified location.
     *
     * @param instance    The instance where the explosion happens.
     * @param point       The center of the explosion.
     * @param strength    The radius/power.
     * @param breakBlocks Whether blocks should be destroyed.
     * @param attacker    The entity that triggered the explosion (e.g. player).
     * @param source      The entity that caused the explosion (e.g. sheep).
     */
    public void explode(Instance instance, Point point, float strength, boolean breakBlocks, Entity attacker, Entity source) {
        GameExplosion explosion = new GameExplosion(
                (float) point.x(), (float) point.y(), (float) point.z(),
                strength, breakBlocks, false, attacker, source
        );
        explosion.setGame(game);
        explosion.apply(instance);
    }

    public void explode(Instance instance, Point point, float strength, boolean breakBlocks) {
        explode(instance, point, strength, breakBlocks, null, null);
    }

    /**
     * Returns a supplier to be used with Instance#setExplosionSupplier.
     */
    public ExplosionSupplier getSupplier(boolean breakBlocks) {
        return (centerX, centerY, centerZ, strength, additionalData) -> {
            GameExplosion explosion = new GameExplosion(centerX, centerY, centerZ, strength, breakBlocks, false);
            explosion.setGame(game);
            return explosion;
        };
    }
}
