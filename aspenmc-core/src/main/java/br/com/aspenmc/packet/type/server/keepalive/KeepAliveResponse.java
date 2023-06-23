package br.com.aspenmc.packet.type.server.keepalive;

import lombok.Getter;
import br.com.aspenmc.packet.Packet;

@Getter
public class KeepAliveResponse extends Packet {

    private final long createdAt;
    private final long responseAt;

    public KeepAliveResponse(long createdAt) {
        this.createdAt = createdAt;
        this.responseAt = System.currentTimeMillis();
    }

}
