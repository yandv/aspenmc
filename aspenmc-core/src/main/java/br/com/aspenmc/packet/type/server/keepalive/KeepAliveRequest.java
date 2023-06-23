package br.com.aspenmc.packet.type.server.keepalive;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;

public class KeepAliveRequest extends Packet {

    private final long createdAt = System.currentTimeMillis();

    public KeepAliveRequest(String serverId) {
        server(serverId);
    }

    @Override
    public void receive() {
        CommonPlugin.getInstance().getPacketManager()
                    .sendPacketAsync(new KeepAliveResponse(createdAt).server(getSource()).id(getId()));
    }
}
