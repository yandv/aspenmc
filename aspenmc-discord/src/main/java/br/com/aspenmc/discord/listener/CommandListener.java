package br.com.aspenmc.discord.listener;

import br.com.aspenmc.discord.DiscordMain;
import br.com.aspenmc.discord.command.CommandHandler;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        String label = event.getName().toLowerCase();
        CommandHandler handler = DiscordMain.getInstance().getCommandManager().getCommand(label).orElse(null);

        if (handler != null) {
            handler.onCommand(event);
        }
    }
}
