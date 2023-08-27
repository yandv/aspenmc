package br.com.aspenmc.discord.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public interface CommandHandler {

    void onCommand(SlashCommandEvent event);

}
