package me.holypite.inventory;

import me.holypite.manager.GameManager;
import me.holypite.model.GameType;
import me.holypite.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public class GameSelectorInventory {

    private final GameManager gameManager;
    private final Inventory inventory;

    public GameSelectorInventory(GameManager gameManager) {
        this.gameManager = gameManager;
        this.inventory = new Inventory(InventoryType.CHEST_3_ROW, Component.text("Sélection du Mini-Jeu", NamedTextColor.DARK_GRAY));
        
        setupItems();
        
        // Handle clicks
        inventory.eventNode().addListener(InventoryPreClickEvent.class, event -> {
            event.setCancelled(true);
            ItemStack clickedItem = event.getClickedItem();
            if (clickedItem.isAir()) return;

            for (GameType type : GameType.values()) {
                if (clickedItem.material() == type.getIcon()) {
                    Player player = event.getPlayer();
                    player.closeInventory();
                    gameManager.joinGame(player, type);
                    break;
                }
            }
        });
    }

    private void setupItems() {
        // Fill with glass panes
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.empty())
                .build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItemStack(i, filler);
        }

        // Add game items
        int[] slots = {11, 15}; // Distributed for 2 games
        GameType[] types = GameType.values();
        
        for (int i = 0; i < types.length && i < slots.length; i++) {
            GameType type = types[i];
            ItemStack item = new ItemBuilder(type.getIcon())
                    .name(type.getDisplayName())
                    .lore(List.of(type.getDescription(), Component.empty(), Component.text("▶ Cliquez pour jouer !", NamedTextColor.GREEN)))
                    .build();
            inventory.setItemStack(slots[i], item);
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
