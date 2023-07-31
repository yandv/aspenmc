package br.com.aspenmc.bukkit.utils.menu.click;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface MenuClickHandler {

	public static final MenuClickHandler EMPTY_HANDLER = clickArgs -> {};

	void onClick(ClickArgs clickArgs);

}
