package br.com.aspenmc.bukkit.event.player;

import br.com.aspenmc.bukkit.event.PlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import lombok.Getter;

@Getter
public class PlayerOpenInventoryEvent extends PlayerEvent {
	
	private Inventory inventory;

    public PlayerOpenInventoryEvent(Player player, Inventory inventory) {
        super(player);
        this.inventory = inventory;
    }

}
