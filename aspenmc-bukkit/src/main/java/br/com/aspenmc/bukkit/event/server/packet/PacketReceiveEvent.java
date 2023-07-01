package br.com.aspenmc.bukkit.event.server.packet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.minehurt.bukkit.event.NormalEvent;
import me.minehurt.packet.Packet;
import org.bukkit.event.Cancellable;

@RequiredArgsConstructor
@Getter
public class PacketReceiveEvent extends NormalEvent implements Cancellable {

    private final Packet packet;

    @Setter
    private boolean cancelled;
}
