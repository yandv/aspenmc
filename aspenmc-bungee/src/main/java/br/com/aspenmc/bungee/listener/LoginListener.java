package br.com.aspenmc.bungee.listener;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.BungeeMain;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class LoginListener implements Listener {

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        String playerName = event.getConnection().getName();

        if (CommonConst.NAME_PATTERN.matcher(playerName).matches()) {
            event.setCancelReason(
                    "§cO seu nick não é permitido.\n" +
                    "§cPara entrar com um nick válido é necessário que ele:\n" +
                    "§cTenha entre 3 e 16 caracteres.\n" +
                    "§cContenha apenas números e letras.");
            event.setCancelled(true);
            return;
        }

        if (CommonPlugin.getInstance().isPiratePlayersEnabled()) {
            event.registerIntent(BungeeMain.getInstance());

            CommonPlugin.getInstance().getPluginPlatform().runAsync(() -> {
                boolean onlineMode = true;

//                if (CommonPlugin.getInstance().getConnectionData().isRedisCached(playerName)) {
//                    onlineMode = CommonPlugin.getInstance().getConnectionData().isConnectionPremium(playerName);
//                    CommonPlugin.getInstance().debug("The player " + event.getConnection().getName() + " is " +
//                                                     (onlineMode ? "premium" : "cracked") + " (cached)");
//                } else {
                    UUID uniqueId = CommonPlugin.getInstance().getUuidFetcher().getUniqueId(playerName);

                    if (uniqueId == null) {
                        onlineMode = false;
                    } else {
                        CommonPlugin.getInstance().debug("The player " + playerName + " have the UUID " + uniqueId);
                    }

//                    CommonPlugin.getInstance().getConnectionData().setConnectionStatus(playerName, uniqueId == null ?
//                                                                                                   UUID.nameUUIDFromBytes(
//                                                                                                           ("OfflinePlayer:" +
//                                                                                                            playerName).getBytes(
//                                                                                                                   Charsets.UTF_8)) :
//                                                                                                   uniqueId,
//                                                                                       onlineMode);

                    CommonPlugin.getInstance().debug("The player " + event.getConnection().getName() + " is " +
                                                     (onlineMode ? "premium" : "cracked") + " (not cached)");
//                }

                event.getConnection().setOnlineMode(onlineMode);
                event.completeIntent(BungeeMain.getInstance());
            });
        } else {
            event.getConnection().setOnlineMode(true);
        }
    }
}
