package br.com.aspenmc.bukkit.event.player.cooldown;

import br.com.aspenmc.bukkit.event.PlayerEvent;
import br.com.aspenmc.bukkit.utils.cooldown.Cooldown;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

public abstract class CooldownEvent extends PlayerEvent {

	@Getter
	@NonNull
	private Cooldown cooldown;

	public CooldownEvent(Player player, Cooldown cooldown) {
		super(player);
		this.cooldown = cooldown;
	}

}
