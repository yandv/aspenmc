package br.com.aspenmc.bukkit.event.server.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.minehurt.bukkit.event.NormalEvent;
import me.minehurt.packet.Packet;

@Getter
@RequiredArgsConstructor
public class PacketReceivedEvent extends NormalEvent {

    private final Packet packet;

}
