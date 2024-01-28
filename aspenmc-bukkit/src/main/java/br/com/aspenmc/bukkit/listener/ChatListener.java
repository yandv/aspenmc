package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.entity.sender.member.status.League;
import br.com.aspenmc.entity.sender.member.status.Status;
import br.com.aspenmc.entity.sender.member.status.StatusType;
import br.com.aspenmc.permission.Tag;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;
import br.com.aspenmc.utils.string.MessageBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChatMute(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(event.getPlayer().getUniqueId())
                                    .orElse(null);

        if (member == null) return;

        Punish currentPunish = member.getPunishConfiguration().getCurrentPunish(PunishType.MUTE);

        if (currentPunish != null) {
            player.sendMessage(currentPunish.getPunishMessage(member.getLanguage()));
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(event.getPlayer().getUniqueId())
                                    .orElse(null);

        if (member == null) return;

        String message =
                member.hasPermission("chat.color") ? ChatColor.translateAlternateColorCodes('&', event.getMessage()) :
                        event.getMessage();

        MessageBuilder messageBuilder = new MessageBuilder("");

        if (BukkitCommon.getInstance().isDisplayRank()) {
            StatusType statusType = StatusType.getByServer(CommonPlugin.getInstance().getServerType());

            if (statusType != null) {
                if (CommonPlugin.getInstance().getStatusManager().hasLoadedStatus(player.getUniqueId(), statusType)) {
                    Status status = CommonPlugin.getInstance().getStatusManager()
                                                .getOrLoadById(player.getUniqueId(), statusType);

                    messageBuilder.append("§7[§f" + status.getLeague() + "§7] ", "", "");
                } else {
                    messageBuilder.append("§7[§f" + League.values()[0] + "§7] ", "", "");
                }
            }
        }

        messageBuilder.append(
                member.getTag().map(Tag::getRealPrefix).orElse("§f") + player.getName() + " §8» §f" + message);

        event.getRecipients().forEach(recipient -> recipient.spigot().sendMessage(messageBuilder.create()));
        CommonPlugin.getInstance().getConsoleSender().sendMessage(messageBuilder.create());
    }
}
