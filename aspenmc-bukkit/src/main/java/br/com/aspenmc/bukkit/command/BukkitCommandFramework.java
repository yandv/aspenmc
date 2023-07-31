package br.com.aspenmc.bukkit.command;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Sender;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.CustomTimingsHandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Getter
public class BukkitCommandFramework implements CommandFramework {

    public static final BukkitCommandFramework INSTANCE = new BukkitCommandFramework(BukkitCommon.getInstance());

    private Plugin plugin;
    private final Map<String, Entry<Method, Object>> commandMap = new HashMap<String, Entry<Method, Object>>();
    private CommandMap map;

    private Map<String, org.bukkit.command.Command> knownCommands;

    public BukkitCommandFramework(Plugin plugin) {
        this.plugin = plugin;

        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();

            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                map = (CommandMap) field.get(manager);
            } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException | SecurityException e) {
                e.printStackTrace();
            }

            try {
                Field field = map.getClass().getDeclaredField("knownCommands");

                field.setAccessible(true);
                knownCommands = (HashMap<String, org.bukkit.command.Command>) field.get(map);
            } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean handleCommand(CommandSender sender, String label, org.bukkit.command.Command cmd, String[] args) {
        Sender currentSender = sender instanceof Player ?
                CommonPlugin.getInstance().getMemberManager().getMemberById(((Player) sender).getUniqueId())
                            .orElse(null) : CommonPlugin.getInstance().getConsoleSender();
        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());

            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }

            String cmdLabel = buffer.toString();

            if (commandMap.containsKey(cmdLabel)) {
                Entry<Method, Object> entry = commandMap.get(cmdLabel);
                Command command = entry.getKey().getAnnotation(Command.class);

                if (!command.console() && !(sender instanceof Player)) {
                    sender.sendMessage("§cSomente jogadores podem executar esse comando!");
                    return true;
                }

                if (command.runAsync() && Bukkit.isPrimaryThread()) {
                    CommonPlugin.getInstance().getPluginPlatform().runAsync(() -> {
                        try {
                            entry.getKey().invoke(entry.getValue(),
                                    new CommandArgs(currentSender, label, args, cmdLabel.split("\\.").length - 1));
                        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    try {
                        entry.getKey().invoke(entry.getValue(),
                                new CommandArgs(currentSender, label, args, cmdLabel.split("\\.").length - 1));
                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }

        sender.sendMessage("§cNão há implementação desse commando no momento.");
        return true;
    }

    @Override
    public void registerCommands(CommandHandler commandHandler) {
        for (Method m : commandHandler.getClass().getMethods()) {
            if (m.getAnnotation(Command.class) != null) {
                Command command = m.getAnnotation(Command.class);
                if (m.getParameterTypes().length != 1 ||
                        !CommandArgs.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
                    continue;
                }

                registerCommand(command, command.name(), m, commandHandler);

                for (String alias : command.aliases()) {
                    registerCommand(command, alias, m, commandHandler);
                }
            }
        }

        for (Method m : commandHandler.getClass().getMethods()) {
            if (m.getAnnotation(Completer.class) != null) {
                Completer comp = m.getAnnotation(Completer.class);
                if (m.getParameterTypes().length != 1 || m.getParameterTypes()[0] != CommandArgs.class) {
                    System.out.println(
                            "Unable to register tab completer " + m.getName() + ". Unexpected method arguments");
                    continue;
                }

                if (m.getReturnType() != List.class) {
                    System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
                    continue;
                }

                registerCompleter(comp.name(), m, commandHandler);

                for (String alias : comp.aliases()) {
                    registerCompleter(alias, m, commandHandler);
                }
            }
        }
    }

    public void registerHelp() {
        Set<HelpTopic> help = new TreeSet<HelpTopic>(HelpTopicComparator.helpTopicComparatorInstance());

        for (String s : commandMap.keySet()) {
            if (!s.contains(".")) {
                org.bukkit.command.Command cmd = map.getCommand(s);
                HelpTopic topic = new GenericCommandHelpTopic(cmd);
                help.add(topic);
            }
        }

        IndexHelpTopic topic = new IndexHelpTopic(plugin.getName(), "All commands for " + plugin.getName(), null, help,
                "Below is a list of all " + plugin.getName() + " commands:");
        Bukkit.getServer().getHelpMap().addTopic(topic);
    }

    private void registerCommand(Command command, String label, Method m, Object obj) {
        Entry<Method, Object> entry = new AbstractMap.SimpleEntry<Method, Object>(m, obj);
        commandMap.put(label.toLowerCase(), entry);
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command cmd = new BukkitCommand(command.name(), cmdLabel, plugin, command.permission());
            knownCommands.put(cmdLabel, cmd);
        } else {
            if (map.getCommand(cmdLabel) instanceof BukkitCommand) {
                BukkitCommand bukkitCommand = (BukkitCommand) map.getCommand(cmdLabel);
                bukkitCommand.setPermission(command.permission());
            }
        }

        if (!command.description().equalsIgnoreCase("") && cmdLabel == label) {
            map.getCommand(cmdLabel).setDescription(command.description());
        }

        if (!command.usage().equalsIgnoreCase("") && cmdLabel == label) {
            map.getCommand(cmdLabel).setUsage(command.usage());
        }
    }

    public Collection<org.bukkit.command.Command> getCommands() {
        return knownCommands.values().stream().filter(command -> command instanceof BukkitCommand)
                            .collect(Collectors.toList());
    }

    private void registerCompleter(String label, Method m, Object obj) {
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command command = new BukkitCommand(cmdLabel, cmdLabel, plugin, "");
            knownCommands.put(cmdLabel, command);
        }

        if (map.getCommand(cmdLabel) instanceof BukkitCommand) {
            BukkitCommand command = (BukkitCommand) map.getCommand(cmdLabel);

            if (command.getCompleter() == null) {
                command.setCompleter(new BukkitCompleter());
            }

            command.getCompleter().addCompleter(label, m, obj);
        } else if (map.getCommand(cmdLabel) instanceof PluginCommand) {
            try {
                Object command = map.getCommand(cmdLabel);
                Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);
                if (field.get(command) == null) {
                    BukkitCompleter completer = new BukkitCompleter();
                    completer.addCompleter(label, m, obj);
                    field.set(command, completer);
                } else if (field.get(command) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(command);
                    completer.addCompleter(label, m, obj);
                } else {
                    System.out.println("Unable to register tab completer " + m.getName() +
                            ". A tab completer is already registered for that command!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void unregisterCommands(String... commands) {
        try {
            Field f1 = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f1.setAccessible(true);

            CommandMap commandMap = (CommandMap) f1.get(Bukkit.getServer());
            Field f2 = commandMap.getClass().getDeclaredField("knownCommands");

            f2.setAccessible(true);

            for (String command : commands) {
                if (knownCommands.containsKey(command)) {
                    knownCommands.remove(command);

                    List<String> aliases = new ArrayList<>();

                    for (String key : knownCommands.keySet()) {
                        if (!key.contains(":")) {
                            continue;
                        }

                        String substr = key.substring(key.indexOf(":") + 1);

                        if (substr.equalsIgnoreCase(command)) {
                            aliases.add(key);
                        }
                    }

                    for (String alias : aliases) {
                        knownCommands.remove(alias);
                    }
                }
            }

            Iterator<Entry<String, org.bukkit.command.Command>> iterator = knownCommands.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<String, org.bukkit.command.Command> entry = iterator.next();

                if (entry.getKey().contains(":")) {
                    entry.getValue().unregister(commandMap);
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<?> getJarClass() {
        return plugin.getClass();
    }


    public BukkitCommand createCommand(String fallbackPrefix, String label, String permission) {
        return new BukkitCommand(fallbackPrefix, label, plugin, permission);
    }

    public class BukkitCommand extends org.bukkit.command.Command {

        private Plugin owningPlugin;
        private CommandExecutor executor;

        @Setter
        @Getter
        private BukkitCompleter completer;

        @Getter
        private String permission;

        public BukkitCommand(String fallbackPrefix, String label, Plugin owner, String permission) {
            super(label);
            this.executor = owner;
            this.owningPlugin = owner;
            this.usageMessage = "";
            this.permission = permission;

            try {
                Class<?> timingsClass = Class.forName("co.aikar.timings.Timings");

                Method method = timingsClass.getDeclaredMethod("ofSafe", String.class);
                method.setAccessible(true);
                Field field = org.bukkit.command.Command.class.getDeclaredField("timings");
                field.setAccessible(true);
                field.set(this, method.invoke(null, "** Command: " + getName()));
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | InvocationTargetException |
                     IllegalAccessException e) {
                this.timings = new CustomTimingsHandler("** Command: " + getName());
            }
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            boolean success = false;

            if (!owningPlugin.isEnabled()) {
                return false;
            }

            if (!testPermission(sender)) {
                return true;
            }

            try {
                success = handleCommand(sender, commandLabel, this, args);
            } catch (Throwable ex) {
                throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin " +
                        owningPlugin.getDescription().getFullName(), ex);
            }

            if (!success && usageMessage.length() > 0) {
                for (String line : usageMessage.replace("<command>", commandLabel).split("\n")) {
                    sender.sendMessage(line);
                }
            }

            return success;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args)
                throws CommandException, IllegalArgumentException {
            Validate.notNull(sender, "Sender cannot be null");
            Validate.notNull(args, "Arguments cannot be null");
            Validate.notNull(alias, "Alias cannot be null");

            List<String> completions = null;
            try {
                if (completer != null) {
                    completions = completer.onTabComplete(sender, this, alias, args);
                }
                if (completions == null && executor instanceof TabCompleter) {
                    completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
                }
            } catch (Throwable ex) {
                StringBuilder message = new StringBuilder();
                message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');

                for (String arg : args) {
                    message.append(arg).append(' ');
                }

                message.deleteCharAt(message.length() - 1).append("' in plugin ")
                       .append(owningPlugin.getDescription().getFullName());
                throw new CommandException(message.toString(), ex);
            }

            if (completions == null) {
                return super.tabComplete(sender, alias, args);
            }
            return completions;
        }

        @Override
        public boolean testPermission(CommandSender target) {
            if (testPermissionSilent(target)) {
                return true;
            }

            target.sendMessage("§cVocê não tem permissão para executar este comando.");
            return false;
        }

        @Override
        public boolean testPermissionSilent(CommandSender target) {
            if (getPermission().isEmpty()) {
                return true;
            }

            if (target instanceof Player) {
                return target.hasPermission(getPermission());
            }

            return true;
        }
    }

    public class BukkitCompleter implements TabCompleter {

        private final Map<String, Entry<Method, Object>> completers = new HashMap<String, Entry<Method, Object>>();

        public void addCompleter(String label, Method m, Object obj) {
            completers.put(label, new AbstractMap.SimpleEntry<Method, Object>(m, obj));
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label,
                String[] args) {
            for (int i = args.length; i >= 0; i--) {
                StringBuilder buffer = new StringBuilder();
                buffer.append(label.toLowerCase());
                for (int x = 0; x < i; x++) {
                    if (!args[x].equals("") && !args[x].equals(" ")) {
                        buffer.append(".").append(args[x].toLowerCase());
                    }
                }
                String cmdLabel = buffer.toString();
                if (completers.containsKey(cmdLabel)) {
                    Entry<Method, Object> entry = completers.get(cmdLabel);
                    try {
                        return (List<String>) entry.getKey().invoke(entry.getValue(), new CommandArgs(
                                sender instanceof Player ? CommonPlugin.getInstance().getMemberManager()
                                                                       .getMemberById(((Player) sender).getUniqueId())
                                                                       .orElse(null) :
                                        CommonPlugin.getInstance().getConsoleSender(), label, args,
                                cmdLabel.split("\\.").length - 1));
                    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}