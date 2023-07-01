package br.com.aspenmc.bukkit.utils.menu;

import org.bukkit.entity.Player;

public interface MenuUpdateHandler {
	
	void onUpdate(Player player, MenuInventory menu);
	
}
