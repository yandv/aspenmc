package br.com.aspenmc.packet.type.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.packet.Packet;

@AllArgsConstructor
public abstract class Server extends Packet {

    @Getter
    private String serverId;

}
