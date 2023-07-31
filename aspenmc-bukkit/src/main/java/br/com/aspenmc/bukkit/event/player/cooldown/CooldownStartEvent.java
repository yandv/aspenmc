package br.com.aspenmc.bukkit.event.player.cooldown;

import br.com.aspenmc.bukkit.utils.cooldown.Cooldown;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
@Setter
public class CooldownStartEvent extends CooldownEvent implements Cancellable {

	private boolean cancelled;

	public CooldownStartEvent(Player player, Cooldown cooldown) {
		super(player, cooldown);
	}

}
