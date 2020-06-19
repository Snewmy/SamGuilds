package me.Sam.Guilds;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener{
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getView().getTitle().equalsIgnoreCase("Guild Rankings")) {
			e.setCancelled(true);
			ItemStack item = e.getCurrentItem();
			Player p = (Player) e.getWhoClicked();
			if(item == null) {
				return;
			}
			if(item.getType().equals(Material.AIR)) {
				return;
			}
			if(!item.hasItemMeta()) {
				return;
			}
			if(item.getItemMeta().hasDisplayName() == false) {
				return;
			}
			if(item.getItemMeta().getDisplayName().equalsIgnoreCase(" ")) {
				return;
			}
			if(item.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.Chat("&c&lPrevious Page"))) {
				int inventoryindex = Guilds.instance.inventories.indexOf(e.getInventory());
				if(inventoryindex == 0) {
					return;
				}
				p.closeInventory();
				p.openInventory(Guilds.instance.inventories.get(inventoryindex - 1));
			}
            if(item.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.Chat("&c&lNext Page"))) {
            	int inventoryindex = Guilds.instance.inventories.indexOf(e.getInventory()); 
            	if((Guilds.instance.inventories.size() - 1) == inventoryindex) {
            		return;
            	}
				p.closeInventory();
				p.openInventory(Guilds.instance.inventories.get(inventoryindex + 1));
			}
            if(item.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.Chat("&a&lExit"))) {
				p.closeInventory();
			}
			
		}
	}

}
