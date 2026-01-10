package me.holypite.commands;

import me.holypite.games.sheepwars.SheepRegistry;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

import java.util.Optional;

public class GiveWoolCommand extends Command {

    public GiveWoolCommand() {
        super("givewool");

        var sheepNameArg = ArgumentType.String("sheepName");
        
        sheepNameArg.setSuggestionCallback((sender, context, suggestion) -> {
            for (String id : SheepRegistry.getSheepIds()) {
                suggestion.addEntry(new net.minestom.server.command.builder.suggestion.SuggestionEntry(id));
            }
        });

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /givewool <ID>");
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            String id = context.get(sheepNameArg);
            
            ItemStack item = SheepRegistry.getSheepItemById(id);
            if (item != ItemStack.AIR) {
                player.getInventory().addItemStack(item);
                player.sendMessage("Given " + id);
            } else {
                player.sendMessage("Sheep ID '" + id + "' not found.");
            }
            
        }, sheepNameArg);
    }
}
