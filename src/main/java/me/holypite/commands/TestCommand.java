package me.holypite.commands;

import me.holypite.manager.GameManager;
import me.holypite.model.GameType;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class TestCommand extends Command {

    public TestCommand(GameManager gameManager) {
        super("test");

        var gameTypeArg = ArgumentType.Enum("gameType", GameType.class);

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /test <gameType>");
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            
            GameType type = context.get(gameTypeArg);
            player.sendMessage("Force starting " + type + "...");
            gameManager.forceStartGame(player, type);
            
        }, gameTypeArg);
    }
}
