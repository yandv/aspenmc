package br.com.aspenmc.bukkit.event.player.vanish;

import me.minehurt.bukkit.event.PlayerCancellableEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerAdminEvent extends PlayerCancellableEvent {

	private AdminMode adminMode;
	@Setter
	private GameMode gameMode;

	public PlayerAdminEvent(Player player, AdminMode adminMode, GameMode mode) {
		super(player);
		this.adminMode = adminMode;
		this.gameMode = mode;
	}

	public static enum AdminMode {
		ADMIN, //
		PLAYER
	}

}
