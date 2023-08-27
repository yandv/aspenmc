package br.com.aspenmc.discord.command.register;

import br.com.aspenmc.discord.command.CommandHandler;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class PingCommand implements CommandHandler {

    @Override
    public void onCommand(SlashCommandEvent event) {
        event.reply("Pong!").queue();
    }
}
