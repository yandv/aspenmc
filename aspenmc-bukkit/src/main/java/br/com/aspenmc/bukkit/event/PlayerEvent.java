package br.com.aspenmc.bukkit.event;

import lombok.Getter;
import org.bukkit.entity.Player;

public class PlayerEvent extends NormalEvent {

	@Getter
	private Player player;

	public PlayerEvent(Player player, boolean async) {
		super(async);
		this.player = player;
	}

	public PlayerEvent(Player player) {
		this(player, false);
	}

}
