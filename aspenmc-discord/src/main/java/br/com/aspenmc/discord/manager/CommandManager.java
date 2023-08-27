package br.com.aspenmc.discord.manager;

import br.com.aspenmc.discord.command.CommandHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandManager {

    private Map<String, CommandHandler> commandMap;

    public CommandManager() {
        commandMap = new HashMap<>();
    }

    public void registerCommand(String command, CommandHandler commandHandler) {
        commandMap.put(command, commandHandler);
    }

    public Optional<CommandHandler> getCommand(String command) {
        return Optional.ofNullable(commandMap.get(command));
    }

    public void unregisterCommand(String command) {
        commandMap.remove(command);
    }
}
