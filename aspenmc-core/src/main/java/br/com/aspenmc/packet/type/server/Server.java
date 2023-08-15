package br.com.aspenmc.packet.type.server;

import br.com.aspenmc.server.ServerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import br.com.aspenmc.packet.Packet;

@AllArgsConstructor
public abstract class Server extends Packet {

    @Getter
    private String serverId;
    @Getter
    private ServerType type;

}
