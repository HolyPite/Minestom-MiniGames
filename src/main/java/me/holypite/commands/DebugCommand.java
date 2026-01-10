package me.holypite.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class DebugCommand extends Command {

    public DebugCommand() {
        super("debug");

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.getInventory().addItemStack(ItemStack.of(Material.RED_WOOL).withAmount(64));
                player.getInventory().addItemStack(ItemStack.of(Material.BOW));
                player.getInventory().addItemStack(ItemStack.of(Material.ARROW).withAmount(64));
                player.sendMessage("Debug items given!");
            }
        });
    }
}
