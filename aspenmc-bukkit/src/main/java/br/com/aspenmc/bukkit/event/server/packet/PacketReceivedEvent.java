package br.com.aspenmc.bukkit.event.server.packet;

import br.com.aspenmc.bukkit.event.NormalEvent;
import br.com.aspenmc.packet.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PacketReceivedEvent extends NormalEvent {

    private final Packet packet;

}
