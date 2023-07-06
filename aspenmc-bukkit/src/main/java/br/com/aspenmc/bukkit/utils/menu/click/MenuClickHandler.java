package br.com.aspenmc.bukkit.utils.menu.click;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface MenuClickHandler {

	public static final MenuClickHandler EMPTY_HANDLER = new MenuClickHandler() {
		@Override
		public boolean onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
			return false;
		}
	};

	boolean onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot);
}