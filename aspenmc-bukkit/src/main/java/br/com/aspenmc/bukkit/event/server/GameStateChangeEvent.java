package br.com.aspenmc.bukkit.event.server;

import br.com.aspenmc.bukkit.event.NormalEvent;
import br.com.aspenmc.server.ProxiedServer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameStateChangeEvent extends NormalEvent {

	private ProxiedServer.GameState oldState;
	private ProxiedServer.GameState state;

}
