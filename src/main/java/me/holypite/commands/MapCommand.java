package me.holypite.commands;

import me.holypite.manager.MapManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;

public class MapCommand extends Command {

    public MapCommand(MapManager mapManager) {
        super("map");

        ArgumentWord subCommand = ArgumentType.Word("subCommand").from("save");
        ArgumentWord mapName = ArgumentType.Word("name");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.");
                return;
            }

            String name = context.get(mapName);
            if (player.getInstance() instanceof InstanceContainer container) {
                player.sendMessage(Component.text("Sauvegarde de la map en cours...", NamedTextColor.YELLOW));
                
                // Perform save
                mapManager.saveInstance(container, name);
                
                player.sendMessage(Component.text("Map '" + name + "' sauvegardée avec succès !", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Impossible de sauvegarder ce type d'instance.", NamedTextColor.RED));
            }
        }, subCommand, mapName);
    }
}
