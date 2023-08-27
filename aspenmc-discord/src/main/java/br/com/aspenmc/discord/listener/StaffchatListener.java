package br.com.aspenmc.discord.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.discord.DiscordConst;
import br.com.aspenmc.packet.type.discord.DiscordStaffMessage;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StaffchatListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        User user = event.getAuthor();
        MessageChannel messageChannel = event.getChannel();

        if (messageChannel.getId().equals(DiscordConst.STAFF_CHAT_ID)) {
            CommonPlugin.getInstance().getPacketManager().sendPacket(
                    new DiscordStaffMessage(user.getName() + "#" + user.getDiscriminator(),
                            event.getMessage().getContentDisplay()));
            return;
        }

        super.onGuildMessageReceived(event);
    }
}
