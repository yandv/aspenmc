package br.com.aspenmc.bungee.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import br.com.aspenmc.packet.Packet;
import net.md_5.bungee.api.plugin.Event;

@Getter
@AllArgsConstructor
public class PacketReceivedEvent extends Event {

    @NonNull
    private final Packet packet;

}