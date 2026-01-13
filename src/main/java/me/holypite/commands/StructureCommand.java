package me.holypite.commands;

import me.holypite.manager.StructureManager;
import me.holypite.manager.StructureManager.StructureMirror;
import me.holypite.manager.StructureManager.StructureRotation;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;

public class StructureCommand extends Command {

    private final StructureManager structureManager = new StructureManager();

    public StructureCommand() {
        super("structure");

        // Arguments
        var pos1Arg = ArgumentType.RelativeBlockPosition("pos1");
        var pos2Arg = ArgumentType.RelativeBlockPosition("pos2"); // Used only for save
        var nameArg = ArgumentType.String("name"); // Structure name
        
        var rotationArg = ArgumentType.Word("rotation").from("0", "90", "180", "270");
        var mirrorArg = ArgumentType.Word("mirror").from("none", "x", "z", "xz");
        
        rotationArg.setDefaultValue("0");
        mirrorArg.setDefaultValue("none");

        // Syntax: /structure save <pos1> <pos2> <name>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command.");
                return;
            }

            Point p1 = context.get(pos1Arg).from(player);
            Point p2 = context.get(pos2Arg).from(player);
            String name = context.get(nameArg);

            structureManager.saveStructure(player.getInstance(), p1, p2, name);
            player.sendMessage("Structure '" + name + "' saved.");

        }, ArgumentType.Literal("save"), pos1Arg, pos2Arg, nameArg);

        // Syntax: /structure load <pos1> <name> [rotation] [mirror]
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command.");
                return;
            }

            Point p1 = context.get(pos1Arg).from(player);
            String name = context.get(nameArg);
            String rotStr = context.get(rotationArg);
            String mirStr = context.get(mirrorArg);
            
            StructureRotation rotation = switch (rotStr) {
                case "90" -> StructureRotation.R90;
                case "180" -> StructureRotation.R180;
                case "270" -> StructureRotation.R270;
                default -> StructureRotation.R0;
            };
            
            StructureMirror mirror = switch (mirStr.toLowerCase()) {
                case "x" -> StructureMirror.X;
                case "z" -> StructureMirror.Z;
                case "xz" -> StructureMirror.XZ;
                default -> StructureMirror.NONE;
            };

            structureManager.placeStructure(player.getInstance(), p1, name, rotation, mirror);
            player.sendMessage("Structure '" + name + "' loaded (Rot: " + rotStr + ", Mirror: " + mirStr + ").");

        }, ArgumentType.Literal("load"), pos1Arg, nameArg, rotationArg, mirrorArg);
        
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: ");
            sender.sendMessage("/structure save <x1> <y1> <z1> <x2> <y2> <z2> <name>");
            sender.sendMessage("/structure load <x> <y> <z> <name> [0/90/180/270] [none/x/z/xz]");
        });
    }
}