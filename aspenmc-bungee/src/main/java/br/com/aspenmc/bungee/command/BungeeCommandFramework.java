package br.com.aspenmc.bungee.command;

import lombok.Getter;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.BungeeMain;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.Sender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.jline.reader.Completer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class BungeeCommandFramework implements CommandFramework {

    public static final BungeeCommandFramework INSTANCE = new BungeeCommandFramework(BungeeMain.getInstance());

    private final Map<String, Entry<Method, Object>> commandMap = new HashMap<String, Entry<Method, Object>>();
    private final Map<String, Entry<Method, Object>> completers = new HashMap<String, Entry<Method, Object>>();
    private final Plugin plugin;

    public BungeeCommandFramework(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getProxy().getPluginManager().registerListener(plugin, new BungeeCompleter());
    }

    public boolean handleCommand(CommandSender sender, String label, String[] args) {
        Sender currentSender = sender instanceof ProxiedPlayer ? CommonPlugin.getInstance().getMemberManager()
                                                                             .getMemberById(
                                                                                     ((ProxiedPlayer) sender).getUniqueId())
                                                                             .orElse(null) :
                               CommonPlugin.getInstance().getConsoleSender();
        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());

            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }

            String cmdLabel = buffer.toString();

            if (commandMap.containsKey(cmdLabel)) {
                Entry<Method, Object> entry = commandMap.get(cmdLabel);
                CommandFramework.Command command = entry.getKey().getAnnotation(Command.class);

                if (sender instanceof ProxiedPlayer) {
                    ProxiedPlayer p = (ProxiedPlayer) sender;
                    Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(p.getUniqueId())
                                                .orElse(null);

                    if (member == null) {
                        p.disconnect(TextComponent.fromLegacyText("ERRO"));
                        return true;
                    }

                    if (!command.permission().isEmpty() && !member.hasPermission(command.permission())) {
                        member.sendMessage("§cVocê não tem permissão para executar esse comando.");
                        return true;
                    }
                } else {
                    if (!command.console()) {
                        sender.sendMessage("§cO comando " + cmdLabel + " não pode ser executado pelo console.");
                        return true;
                    }
                }

                if (command.runAsync()) {
                    plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {

                        @Override
                        public void run() {
                            try {
                                entry.getKey().invoke(entry.getValue(),
                                                      new CommandArgs(currentSender, label.replace(".", " "), args,
                                                                      cmdLabel.split("\\.").length - 1));
                            } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    try {
                        entry.getKey().invoke(entry.getValue(), new CommandArgs(currentSender, label, args,
                                                                                cmdLabel.split("\\.").length - 1));
                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        defaultCommand(new CommandArgs(currentSender, label, args, 0));
        return true;
    }

    @Override
    public void registerCommands(CommandHandler cls) {
        for (Method m : cls.getClass().getMethods()) {
            if (m.getAnnotation(Command.class) != null) {
                Command command = m.getAnnotation(Command.class);
                if (m.getParameterTypes().length != 1 ||
                    !CommandArgs.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
                    continue;
                }
                registerCommand(command, command.name(), m, cls);
                for (String alias : command.aliases()) {
                    registerCommand(command, alias, m, cls);
                }
            } else if (m.getAnnotation(Completer.class) != null) {
                Completer comp = m.getAnnotation(Completer.class);
                if (m.getParameterTypes().length != 1 ||
                    !CommandArgs.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    System.out.println(
                            "Unable to register tab completer " + m.getName() + ". Unexpected method arguments");
                    continue;
                }
                if (m.getReturnType() != List.class) {
                    System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
                    continue;
                }
                registerCompleter(comp.name(), m, cls);
                for (String alias : comp.aliases()) {
                    registerCompleter(alias, m, cls);
                }
            }
        }
    }

    /**
     * Registers all the commands under the plugin's help
     */

    private void registerCommand(Command command, String label, Method m, Object obj) {
        Entry<Method, Object> entry = new AbstractMap.SimpleEntry<Method, Object>(m, obj);
        commandMap.put(label.toLowerCase(), entry);
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        plugin.getProxy().getPluginManager().registerCommand(plugin, new BungeeCommand(cmdLabel));
    }

    private void registerCompleter(String label, Method m, Object obj) {
        completers.put(label, new AbstractMap.SimpleEntry<Method, Object>(m, obj));
    }

    private void defaultCommand(CommandArgs args) {
        args.getSender().sendMessage("§cComando do bungeecord inacessível!");
    }

    class BungeeCommand extends net.md_5.bungee.api.plugin.Command {

        protected BungeeCommand(String label) {
            super(label);
        }

        protected BungeeCommand(String label, String permission) {
            super(label, permission);
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            handleCommand(sender, getName(), args);
        }
    }

    public static void main(String[] args) {
        test("/party ");
        test("/party c");
        test("/party c ");
        test("/party c c");
    }

    public static void test(String string) {
        Pattern pattern = Pattern.compile("\\s+");
        Matcher matcher = pattern.matcher(string);

        int count = 0;

        while (matcher.find()) {
            count++;
        }

        String[] split = string.replaceAll("\\s+", " ").split(" ");

        if (split.length == 0) {
            return;
        }

        String[] a = new String[count];

        for (int i = 1; i < split.length; i++) {
            a[i - 1] = split[i];
        }

        for (int i = a.length - 1; i >= 0; i--) {
            if (a[i] == null) {
                a[i] = "";
            }
        }
    }

    public class BungeeCompleter implements Listener {

        @SuppressWarnings("unchecked")
        @EventHandler
        public void onTabComplete(TabCompleteEvent event) {
            if (!(event.getSender() instanceof ProxiedPlayer)) {
                return;
            }

            if (!event.getCursor().startsWith("/")) {
                return;
            }

            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            String[] split = event.getCursor().replaceAll("\\s+", " ").split(" ");

            if (split.length == 0) {
                return;
            }

            Pattern pattern = Pattern.compile("\\s+");
            Matcher matcher = pattern.matcher(event.getCursor());

            int count = 0;

            while (matcher.find()) {
                count++;
            }

            if (count == 0) {
                return;
            }

            String[] args = new String[count];

            for (int i = 1; i < split.length; i++) {
                args[i - 1] = split[i];
            }

            for (int i = args.length - 1; i >= 0; i--) {
                if (args[i] == null) {
                    args[i] = "";
                }
            }

            String label = split[0].substring(1);

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
                        event.getSuggestions().clear();

                        List<String> list = (List<String>) entry.getKey().invoke(entry.getValue(), new CommandArgs(
                                CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId())
                                            .orElse(null), label, args, cmdLabel.split("\\.").length - 1));

                        event.getSuggestions().addAll(list);
                    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public Class<?> getJarClass() {
        return plugin.getClass();
    }
}
