package br.com.aspenmc.bukkit.event.player;

import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
public class PlayerMoveUpdateEvent extends PlayerCancellableEvent {

	private Location from;
	private Location to;


	public PlayerMoveUpdateEvent(Player player, Location from, Location to) {
		super(player);
		this.from = from;
		this.to = to;
	}

}