package me.holypite.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;

public class CustomDeathEvent implements PlayerEvent {

    private final Player player;
    private final Entity killer;
    private final String damageType;

    public CustomDeathEvent(Player player, Entity killer, String damageType) {
        this.player = player;
        this.killer = killer;
        this.damageType = damageType;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public Entity getKiller() {
        return killer;
    }

    public String getDamageType() {
        return damageType;
    }
}
