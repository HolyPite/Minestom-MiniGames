package me.holypite.games.kits;

import me.holypite.model.Kit;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class ClassicKit extends Kit {

    public ClassicKit() {
        super("Classic", ItemStack.of(Material.IRON_SWORD));
    }

    @Override
    public void apply(Player player) {
        player.getInventory().clear();
        
        // Armor
        player.setHelmet(ItemStack.of(Material.IRON_HELMET));
        player.setChestplate(ItemStack.of(Material.IRON_CHESTPLATE));
        player.setLeggings(ItemStack.of(Material.IRON_LEGGINGS));
        player.setBoots(ItemStack.of(Material.IRON_BOOTS));
        
        // Items
        player.getInventory().addItemStack(ItemStack.of(Material.IRON_SWORD));
        player.getInventory().addItemStack(ItemStack.of(Material.BOW));
        player.getInventory().addItemStack(ItemStack.of(Material.COOKED_BEEF).withAmount(64));
        player.getInventory().addItemStack(ItemStack.of(Material.ARROW).withAmount(32));
    }
}
