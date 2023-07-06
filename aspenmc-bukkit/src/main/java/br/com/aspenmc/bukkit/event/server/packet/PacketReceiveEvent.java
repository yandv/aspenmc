package br.com.aspenmc.bukkit.event.server.packet;

import br.com.aspenmc.bukkit.event.NormalEvent;
import br.com.aspenmc.packet.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;

@RequiredArgsConstructor
@Getter
public class PacketReceiveEvent extends NormalEvent implements Cancellable {

    private final Packet packet;

    @Setter
    private boolean cancelled;
}
