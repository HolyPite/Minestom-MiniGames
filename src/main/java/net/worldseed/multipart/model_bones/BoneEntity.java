package net.worldseed.multipart.model_bones;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.tag.Tag;
import net.worldseed.multipart.GenericModel;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Custom override of WSEE BoneEntity to fix binary incompatibility with recent Minestom versions.
 */
public class BoneEntity extends LivingEntity {
    private final GenericModel model;
    private final String name;

    public BoneEntity(@NotNull EntityType entityType, GenericModel model, String name) {
        super(entityType);
        this.setAutoViewable(false);
        setTag(Tag.String("WSEE"), "part");
        this.model = model;
        this.name = name;

        this.setNoGravity(true);
        this.setSynchronizationTicks(Long.MAX_VALUE);
    }

    @Override
    public @NotNull Set<Player> getViewers() {
        return model.getViewers();
    }

    public GenericModel getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    @Override
    public void tick(long time) {
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        Pos position = this.getPosition();
        // Use the Record constructor available in the current Minestom version
        var spawnPacket = new SpawnEntityPacket(this.getEntityId(), this.getUuid(), this.getEntityType(), model.getPosition().withView(position.yaw(), 0), position.yaw(), 0, Vec.ZERO);

        player.sendPacket(spawnPacket);
        player.sendPacket(new LazyPacket(this::getMetadataPacket));

        if (this.getEntityType() == EntityType.ZOMBIE || this.getEntityType() == EntityType.ARMOR_STAND)
            player.sendPacket(getEquipmentsPacket());
    }
}
