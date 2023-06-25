package br.com.aspenmc.bungee;

import br.com.aspenmc.CommonPlatform;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BungeePlatform implements CommonPlatform {

    @Override
    public void broadcast(String... messages) {
        for (String message : messages) {
            ProxyServer.getInstance().broadcast(message);
        }
    }

    @Override
    public void broadcast(TextComponent... components) {
        ProxyServer.getInstance().broadcast(components);
    }

    @Override
    public String getNameById(UUID playerId) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
        return player == null ? null : player.getName();
    }

    @Override
    public UUID getUniqueId(String playerName) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
        return player == null ? null : player.getUniqueId();
    }

    @Override
    public void runAsync(Runnable runnable) {
        ProxyServer.getInstance().getScheduler().runAsync(BungeeMain.getInstance(), runnable);
    }

    @Override
    public void runAsyncTimer(Runnable runnable, long delay, long period) {
        ProxyServer.getInstance().getScheduler()
                   .schedule(BungeeMain.getInstance(), runnable, (long) ((delay / 20.0D) * 1000),
                             (long) ((period / 20.0D) * 1000), TimeUnit.MILLISECONDS);
    }

    @Override
    public void runSync(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runLater(Runnable runnable, long delay) {
        ProxyServer.getInstance().getScheduler()
                   .schedule(BungeeMain.getInstance(), runnable, (long) ((delay / 20.0D) * 1000),
                             TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTimer(Runnable runnable, long delay, long period) {
        ProxyServer.getInstance().getScheduler()
                   .schedule(BungeeMain.getInstance(), runnable, (long) ((delay / 20.0D) * 1000),
                             (long) ((period / 20.0D) * 1000), TimeUnit.MILLISECONDS);
    }
}
