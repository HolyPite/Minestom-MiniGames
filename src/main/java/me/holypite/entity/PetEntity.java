package me.holypite.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.worldseed.multipart.GenericModel;
import net.worldseed.multipart.GenericModelImpl;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class PetEntity extends EntityCreature {

    private final GenericModelImpl model;
    private final Player owner;

    public PetEntity(EntityType entityType, String modelId, Player owner) {
        super(entityType);
        this.owner = owner;
        this.setInvisible(true);

        // Instantiate the WSEE model
        this.model = new GenericModelImpl() {
            @Override
            public String getId() {
                return modelId;
            }
        };
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        model.addViewer(player);
    }

    @Override
    public void update(long time) {
        super.update(time);
        
        // Sync model position and rotation with base entity
        if (model.getInstance() != null) {
            try {
                model.setPosition(getPosition());
                model.setGlobalRotation(getPosition().yaw(), getPosition().pitch());
                model.setHeadRotation("head", getPosition().yaw()); 
                model.draw();
            } catch (Exception e) {}
        }
        
        // Simple follow logic
        if (owner != null && owner.getInstance() == getInstance()) {
            double distSq = getPosition().distanceSquared(owner.getPosition());
            if (distSq > 3 * 3) {
                getNavigator().setPathTo(owner.getPosition());
                // TODO: Trigger animations via model.triggerAnimationEnd() or internal logic
                // WSEE handles animations via API, e.g. model.triggerAnimation("walk");
                // Check if 'walk' animation exists in your .bbmodel
            }
        }
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        return super.setInstance(instance, spawnPosition).thenAccept(v -> {
            // Initialize model when entity enters instance
            try {
                model.init(instance, spawnPosition);
                if (owner != null) {
                    model.addViewer(owner);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void remove() {
        super.remove();
        if (model != null) {
            model.destroy();
        }
    }
}
