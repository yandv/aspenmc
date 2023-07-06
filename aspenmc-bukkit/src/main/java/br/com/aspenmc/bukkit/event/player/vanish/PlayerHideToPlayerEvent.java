package br.com.aspenmc.bukkit.event.player.vanish;

import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PlayerHideToPlayerEvent extends PlayerCancellableEvent {

	private Player toPlayer;

	public PlayerHideToPlayerEvent(Player player, Player toPlayer) {
		super(player);
		this.toPlayer = toPlayer;
	}

}
