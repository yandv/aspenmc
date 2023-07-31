package br.com.aspenmc.packet.type.server;

import br.com.aspenmc.server.ProxiedServer;
import lombok.Getter;
import br.com.aspenmc.CommonPlugin;

@Getter
public class ServerStart extends Server {

    private final ProxiedServer server;

    public ServerStart(ProxiedServer server) {
        super(server.getServerId());
        this.server = server;
    }

    @Override
    public void receive() {
        if (!CommonPlugin.getInstance().isServerLog()) return;

        CommonPlugin.getInstance().getServerManager().addActiveServer(server);
        CommonPlugin.getInstance()
                    .debug("The server " + getServer().getServerId() + " (" + server.getServerAddress() + ":" +
                           server.getServerPort() + ") has been started as a " + getServer().getServerType().name() +
                           ".");
    }
}
