package me.holypite.entity;

import me.holypite.manager.explosion.ExplosionManager;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

public class PrimedTnt extends Entity {

    private final Entity attacker;
    private int fuse;

    public PrimedTnt(@Nullable Entity attacker) {
        super(EntityType.TNT);
        this.attacker = attacker;
        this.fuse = 80; // 4 seconds (vanilla default)
        
        // Vanilla-like aerodynamics
        setAerodynamics(new Aerodynamics(0.04, 0.98, 0.98));
        
        if (getEntityMeta() instanceof PrimedTntMeta meta) {
            meta.setFuseTime(fuse);
        }
    }

    public PrimedTnt(@Nullable Entity attacker, int fuse) {
        this(attacker);
        this.fuse = fuse;
        if (getEntityMeta() instanceof PrimedTntMeta meta) {
            meta.setFuseTime(fuse);
        }
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        if (onGround) {
            setVelocity(getVelocity().mul(0.7, -0.5, 0.7)); // Bounce/Friction
        }

        fuse--;
        if (getEntityMeta() instanceof PrimedTntMeta meta) {
            meta.setFuseTime(fuse);
        }

        if (fuse <= 0) {
            explode();
        }
    }

    private void explode() {
        Instance instance = getInstance();
        if (instance == null) return;

        Point pos = getPosition();
        ExplosionManager explosionManager = new ExplosionManager();
        
        // Trigger a standard strength 4 explosion (vanilla TNT)
        explosionManager.explode(instance, pos, 4.0f, true, attacker, this);
        
        remove();
    }
}