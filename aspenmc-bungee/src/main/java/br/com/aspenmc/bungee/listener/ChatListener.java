package br.com.aspenmc.bungee.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.BungeeMain;
import br.com.aspenmc.entity.sender.member.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.List;

public class ChatListener implements Listener {

    private static final List<String> ALLOWED_COMMANDS = Arrays.asList("login", "logar", "register", "registrar");

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (member == null) return;

        if (!member.getLoginConfiguration().isLogged()) {
            if (event.isCommand()) {
                String command = event.getMessage().split(" ")[0].replace("/", "").toLowerCase();
                if (!ALLOWED_COMMANDS.contains(command)) {
                    event.setCancelled(true);
                    player.sendMessage("§cVocê precisa estar logado para executar comandos.");
                }
                return;
            }

            if (member.getPreferencesConfiguration().isStaffChatEnabled()) {
                member.getPreferencesConfiguration().setStaffChatEnabled(false);
            }
        }

        if (member.getPreferencesConfiguration().isStaffChatEnabled() && !event.isCommand()) {
            event.setCancelled(true);
            BungeeMain.getInstance().sendStaffChatMessage(member, event.getMessage().replace('&', '§'), true);
            return;
        }
    }
}
