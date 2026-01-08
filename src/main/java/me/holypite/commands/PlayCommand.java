package me.holypite.commands;

import me.holypite.manager.GameManager;
import me.holypite.model.GameType;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class PlayCommand extends Command {

    public PlayCommand(GameManager gameManager) {
        super("play");

        var gameTypeArg = ArgumentType.Enum("gameType", GameType.class);

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /play <gameType>");
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can play games.");
                return;
            }
            GameType type = context.get(gameTypeArg);
            player.sendMessage("Sending you to " + type + "...");
            gameManager.joinGame(player, type);
        }, gameTypeArg);
    }
}
