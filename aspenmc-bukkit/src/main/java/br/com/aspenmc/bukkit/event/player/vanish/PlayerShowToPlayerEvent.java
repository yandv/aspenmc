package br.com.aspenmc.bukkit.event.player.vanish;

import lombok.Getter;
import me.minehurt.bukkit.event.PlayerCancellableEvent;
import org.bukkit.entity.Player;

@Getter
public class PlayerShowToPlayerEvent extends PlayerCancellableEvent {
	private Player toPlayer;

	public PlayerShowToPlayerEvent(Player player, Player toPlayer) {
		super(player);
		this.toPlayer = toPlayer;
	}
}
