package br.com.aspenmc.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NormalEvent extends Event {
	
	private static final HandlerList HANDLER_LIST = new HandlerList();

	public NormalEvent(boolean async) {
		super(async);
	}

	public NormalEvent() {
		this(false);
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}

}
