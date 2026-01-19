package me.holypite.commands;

import me.holypite.cosmetics.TrailType;
import me.holypite.manager.CosmeticManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class CosmeticCommand extends Command {

    public CosmeticCommand(CosmeticManager cosmeticManager) {
        super("cosmetic", "trail");

        var typeArg = ArgumentType.Enum("type", TrailType.class);

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /cosmetic <type>");
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.");
                return;
            }

            TrailType type = context.get(typeArg);
            cosmeticManager.setTrail(player, type);
        }, typeArg);
    }
}
