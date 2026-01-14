package me.holypite.commands;

import me.holypite.manager.StructureManager;
import me.holypite.manager.StructureManager.StructureMirror;
import me.holypite.manager.StructureManager.StructureRotation;
import me.holypite.manager.StructurePreviewManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;

public class StructureCommand extends Command {

    private final StructureManager structureManager = new StructureManager();
    private final StructurePreviewManager previewManager;

    public StructureCommand(StructurePreviewManager previewManager) {
        super("structure");
        this.previewManager = previewManager;

        // Arguments
        var pos1Arg = ArgumentType.RelativeBlockPosition("pos1");
        var pos2Arg = ArgumentType.RelativeBlockPosition("pos2"); 
        var nameArg = ArgumentType.String("name"); 
        
        var rotationArg = ArgumentType.Word("rotation").from("0", "90", "180", "270");
        var mirrorArg = ArgumentType.Word("mirror").from("none", "x", "z", "xz");
        
        rotationArg.setDefaultValue("0");
        mirrorArg.setDefaultValue("none");

        // Syntax: /structure save <pos1> <pos2> <name>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Point p1 = context.get(pos1Arg).from(player);
            Point p2 = context.get(pos2Arg).from(player);
            String name = context.get(nameArg);
            structureManager.saveStructure(player.getInstance(), p1, p2, name);
            player.sendMessage("Structure '" + name + "' saved.");
        }, ArgumentType.Literal("save"), pos1Arg, pos2Arg, nameArg);

        // Syntax: /structure load <pos1> <name> [rotation] [mirror]
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Point p1 = context.get(pos1Arg).from(player);
            String name = context.get(nameArg);
            String rotStr = context.get(rotationArg);
            String mirStr = context.get(mirrorArg);
            
            StructureRotation rotation = getRotation(rotStr);
            StructureMirror mirror = getMirror(mirStr);

            boolean success = structureManager.placeStructureWithResult(player.getInstance(), p1, name, rotation, mirror);
            if (success) {
                player.sendMessage(Component.text("Structure '" + name + "' loaded.", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Failed to load structure '" + name + "'. Check console.", NamedTextColor.RED));
            }
        }, ArgumentType.Literal("load"), pos1Arg, nameArg, rotationArg, mirrorArg);

        // Syntax: /structure preview <name>
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            previewManager.startPreview(player, context.get(nameArg));
        }, ArgumentType.Literal("preview"), nameArg);

        // Syntax: /structure confirm
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            previewManager.confirmPreview(player);
        }, ArgumentType.Literal("confirm"));

        // Syntax: /structure cancel
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            previewManager.cancelPreview(player);
        }, ArgumentType.Literal("cancel"));
        
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: ");
            sender.sendMessage("/structure save <x1> <y1> <z1> <x2> <y2> <z2> <name>");
            sender.sendMessage("/structure load <x> <y> <z> <name>");
            sender.sendMessage("/structure preview <name>");
        });
    }

    private StructureRotation getRotation(String str) {
        return switch (str) {
            case "90" -> StructureRotation.R90;
            case "180" -> StructureRotation.R180;
            case "270" -> StructureRotation.R270;
            default -> StructureRotation.R0;
        };
    }

    private StructureMirror getMirror(String str) {
        return switch (str.toLowerCase()) {
            case "x" -> StructureMirror.X;
            case "z" -> StructureMirror.Z;
            case "xz" -> StructureMirror.XZ;
            default -> StructureMirror.NONE;
        };
    }
}
