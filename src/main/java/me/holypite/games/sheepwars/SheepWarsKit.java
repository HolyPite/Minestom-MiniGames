package me.holypite.games.sheepwars;

import me.holypite.model.Kit;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class SheepWarsKit extends Kit {

    public SheepWarsKit() {
        super("SheepWars", ItemStack.of(Material.WHITE_WOOL));
    }

    @Override
    public void apply(Player player) {
        player.getInventory().clear();
        
        // Leather Armor (TODO: Color based on team)
        player.setHelmet(ItemStack.of(Material.LEATHER_HELMET));
        player.setChestplate(ItemStack.of(Material.LEATHER_CHESTPLATE));
        player.setLeggings(ItemStack.of(Material.LEATHER_LEGGINGS));
        player.setBoots(ItemStack.of(Material.LEATHER_BOOTS));
        
        // Weapons
        player.getInventory().addItemStack(ItemStack.of(Material.STONE_SWORD));
        player.getInventory().addItemStack(ItemStack.of(Material.BOW));
        player.getInventory().addItemStack(ItemStack.of(Material.ARROW).withAmount(1)); // One arrow needed for bow logic usually
    }
}
