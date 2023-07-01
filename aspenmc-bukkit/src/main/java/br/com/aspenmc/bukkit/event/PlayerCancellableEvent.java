package br.com.aspenmc.bukkit.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
@Setter
public class PlayerCancellableEvent extends PlayerEvent implements Cancellable {

	private boolean cancelled;

	public PlayerCancellableEvent(Player player) {
		super(player);
	}

	public PlayerCancellableEvent(Player player, boolean async) {
		super(player, async);
	}

}
