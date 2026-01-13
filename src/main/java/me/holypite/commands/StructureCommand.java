package me.holypite.commands;

import me.holypite.manager.StructureManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.location.RelativeBlockPosition;

public class StructureCommand extends Command {

    private final StructureManager structureManager = new StructureManager();

    public StructureCommand() {
        super("structure");

        // Arguments
        var modeArg = ArgumentType.Word("mode").from("save", "load");
        var pos1Arg = ArgumentType.RelativeBlockPosition("pos1");
        var pos2Arg = ArgumentType.RelativeBlockPosition("pos2"); // Used only for save
        var nameArg = ArgumentType.String("name"); // Structure name

        // Syntax: /structure save <pos1> <pos2> <name>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command.");
                return;
            }

            RelativeBlockPosition relativePos1 = context.get(pos1Arg);
            RelativeBlockPosition relativePos2 = context.get(pos2Arg);
            String name = context.get(nameArg);

            Point p1 = relativePos1.from(player);
            Point p2 = relativePos2.from(player);

            structureManager.saveStructure(player.getInstance(), p1, p2, name);
            player.sendMessage("Structure '" + name + "' saved.");

        }, ArgumentType.Literal("save"), pos1Arg, pos2Arg, nameArg);

        // Syntax: /structure load <pos1> <name>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command.");
                return;
            }

            RelativeBlockPosition relativePos1 = context.get(pos1Arg);
            String name = context.get(nameArg);

            Point p1 = relativePos1.from(player);

            structureManager.placeStructure(player.getInstance(), p1, name);
            player.sendMessage("Structure '" + name + "' loaded.");

        }, ArgumentType.Literal("load"), pos1Arg, nameArg);
        
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: ");
            sender.sendMessage("/structure save <x1> <y1> <z1> <x2> <y2> <z2> <name>");
            sender.sendMessage("/structure load <x> <y> <z> <name>");
        });
    }
}
