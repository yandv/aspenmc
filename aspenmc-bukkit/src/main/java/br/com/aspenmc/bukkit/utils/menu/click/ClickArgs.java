package br.com.aspenmc.bukkit.utils.menu.click;

import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.bukkit.utils.menu.MenuItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class ClickArgs {

	private Player player;

	private Inventory inventory;
	private MenuInventory menuInventory;
	private ClickType clickType;

	private MenuItem menuItem;
	private int slot;

	@Setter
	private boolean cancelled = true;

	public ClickArgs(Player player, Inventory inventory, MenuInventory menuInventory, ClickType clickType, MenuItem menuItem, int slot) {
		this.player = player;
		this.inventory = inventory;
		this.menuInventory = menuInventory;
		this.clickType = clickType;
		this.menuItem = menuItem;
		this.slot = slot;
	}

	public ItemStack getItemStack() {
		return menuItem.getItemStack();
	}
}