package br.com.aspenmc.bukkit.event.player.cooldown;

import br.com.aspenmc.bukkit.utils.cooldown.Cooldown;
import org.bukkit.entity.Player;

public class CooldownStopEvent extends CooldownEvent {

	public CooldownStopEvent(Player player, Cooldown cooldown) {
		super(player, cooldown);
	}

}