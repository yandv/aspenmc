package br.com.aspenmc.packet.type.server;

import lombok.Getter;
import br.com.aspenmc.CommonPlugin;

@Getter
public class ServerStop extends Server {

    public ServerStop(String serverId) {
        super(serverId);
    }

    @Override
    public void receive() {
        if (!CommonPlugin.getInstance().isServerLog()) return;

        CommonPlugin.getInstance().getServerManager().removeActiveServer(getServerId());
        CommonPlugin.getInstance().debug("The server " + getServerId() + " stop.");
    }
}
