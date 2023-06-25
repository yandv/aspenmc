package br.com.aspenmc.bungee.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import br.com.aspenmc.packet.Packet;
import net.md_5.bungee.api.plugin.Event;

@RequiredArgsConstructor
@Getter
public class PacketReceiveEvent extends Event {

	@NonNull
	private final Packet packet;

	@Setter
	private boolean cancelled;

}
