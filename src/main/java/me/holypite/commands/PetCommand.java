package me.holypite.commands;

import me.holypite.entity.PetEntity;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;

public class PetCommand extends Command {

    public PetCommand() {
        super("pet");

        var modelArg = ArgumentType.String("modelName");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /pet <modelName>");
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return;
            }

            String modelName = context.get(modelArg);
            PetEntity pet = new PetEntity(EntityType.ZOMBIE, modelName, player);
            pet.setInstance(player.getInstance(), player.getPosition());
            
            player.sendMessage("Pet spawned with model: " + modelName);
        }, modelArg);
    }
}
