package br.com.aspenmc.bungee.listener;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.BungeeMain;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginListener implements Listener {

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        String playerName = event.getConnection().getName();

        if (!CommonConst.NAME_PATTERN.matcher(playerName).matches()) {
            event.setCancelReason(
                    "§cO seu nick não é permitido.\n" + "§cPara entrar com um nick válido é necessário que ele:\n" +
                            "§cTenha entre 3 e 16 caracteres.\n" + "§cContenha apenas números e letras.");
            event.setCancelled(true);
            return;
        }

        if (CommonPlugin.getInstance().isPiratePlayersEnabled()) {
            event.registerIntent(BungeeMain.getInstance());

            CommonPlugin.getInstance().getConnectionData().retrieveConnection(playerName)
                        .whenComplete((connection, throwable) -> {
                            if (throwable != null) {
                                event.getConnection().setOnlineMode(true);
                                event.completeIntent(BungeeMain.getInstance());
                                CommonPlugin.getInstance().getLogger().log(java.util.logging.Level.SEVERE,
                                                                           "An error occurred while trying to " +
                                                                                   "retrieve the connection of " +
                                                                                   playerName, throwable);
                                return;
                            }

                            CommonPlugin.getInstance().debug("The player " + playerName + " have the UUID " +
                                                                     connection.getPlayerId() + " and is " +
                                                                     (connection.isPremium() ? "premium" : "cracked") +
                                                                     (connection.isCached() ? " (cached)" :
                                                                             " (not cached)"));
                            CommonPlugin.getInstance().getConnectionData().persistConnection(connection);

                            event.getConnection().setOnlineMode(connection.isPremium());
                            event.completeIntent(BungeeMain.getInstance());
                        });
        } else {
            event.getConnection().setOnlineMode(true);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        CommonPlugin.getInstance().getConnectionData().cacheConnection(event.getPlayer().getName());
    }
}
