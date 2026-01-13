package me.holypite.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GamemodeCommand extends Command {

    public GamemodeCommand() {
        super("gamemode", "gm");

        var modeArg = ArgumentType.Enum("mode", GameMode.class);

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /gamemode <survival|creative|adventure|spectator>");
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return;
            }

            GameMode mode = context.get(modeArg);
            player.setGameMode(mode);
            player.sendMessage("Gamemode set to " + mode.name());
        }, modeArg);
    }
}
