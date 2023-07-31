package br.com.aspenmc.bukkit.event.player;

import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import org.bukkit.entity.Player;

import lombok.Getter;

@Getter
public class PlayerDamagePlayerEvent extends PlayerCancellableEvent {
	
	private Player damager;
	
	private double damage, finalDamage;
	
	public PlayerDamagePlayerEvent(Player entity, Player damager, boolean cancelled, double damage, double finalDamage) {
		super(entity);
		
		this.setCancelled(cancelled);
		this.damager = damager;
		this.damage = damage;
		this.finalDamage = finalDamage;
	}
	
	public void setDamage(double damage) {
		this.damage = damage;
	}

}
