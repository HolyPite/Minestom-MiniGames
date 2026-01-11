package me.holypite.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public class InstancesCommand extends Command {

    public InstancesCommand() {
        super("instances");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Active Instances:");
            for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
                int players = instance.getPlayers().size();
                int entities = instance.getEntities().size();
                int chunks = instance.getChunks().size();
                
                String info = String.format("- %s | Players: %d | Entities: %d | Chunks: %d", 
                        instance.getUuid().toString().substring(0, 8), players, entities, chunks);
                
                sender.sendMessage(info);
            }
            sender.sendMessage("Total: " + MinecraftServer.getInstanceManager().getInstances().size());
        });
    }
}
