package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.permission.Tag;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;
import br.com.aspenmc.utils.string.MessageBuilder;
import br.com.aspenmc.utils.string.StringFormat;
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

        MessageBuilder messageBuilder = new MessageBuilder(
                member.getTag().map(Tag::getRealPrefix).orElse("§f") + player.getName() + " §8» §f" +
                        event.getMessage());

        event.getRecipients().forEach(recipient -> recipient.spigot().sendMessage(messageBuilder.create()));
    }
}
