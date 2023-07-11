package br.com.aspenmc.packet.type.server.keepalive;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.packet.Packet;
import br.com.aspenmc.server.ProxiedServer;

public class KeepAliveRequest extends Packet {

    private final long createdAt = System.currentTimeMillis();

    public KeepAliveRequest(String serverId) {
        server(serverId);
    }

    public KeepAliveRequest(ProxiedServer server) {
        server(server.getServerId());
    }

    @Override
    public void receive() {
        CommonPlugin.getInstance().getPacketManager()
                    .sendPacketAsync(new KeepAliveResponse(createdAt).server(getSource()).id(getId()));
    }
}
