package br.com.aspenmc.bungee.entity;

import br.com.aspenmc.server.ProxiedServer;
import lombok.Getter;
import lombok.Setter;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.configuration.LoginConfiguration;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@Getter
public class BungeeMember extends Member {

    @Setter
    private transient ProxiedPlayer proxiedPlayer;

    public BungeeMember(UUID uniqueId, String name, LoginConfiguration.AccountType accountType) {
        super(uniqueId, name, accountType);
    }

    @Override
    public boolean hasPermission(String permission) {
        return proxiedPlayer.hasPermission(permission) || super.hasPermission(permission);
    }

    @Override
    public void sendServer(String serverId) {
        ProxiedServer server = CommonPlugin.getInstance().getServerManager().getServer(serverId);

        if (server == null) return;

        proxiedPlayer.connect(server.getServerInfo());
    }

    @Override
    public void performCommand(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(proxiedPlayer, command);
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            proxiedPlayer.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(TextComponent... messages) {
        proxiedPlayer.sendMessage(messages);
    }
}
