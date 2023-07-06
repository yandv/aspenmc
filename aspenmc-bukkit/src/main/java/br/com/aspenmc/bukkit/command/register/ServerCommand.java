package br.com.aspenmc.bukkit.command.register;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.utils.Location;
import br.com.aspenmc.bukkit.utils.ProtocolVersion;
import br.com.aspenmc.bukkit.utils.character.impl.DefaultCharacter;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.entity.member.Skin;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.utils.string.StringFormat;
import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerCommand implements CommandHandler {

    @CommandFramework.Command(name = "setdefaultskin", permission = "command.setdefaultskin")
    public void setdefaultskinCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " <skin> §fpara alterar o skin.");
            return;
        }

        Skin skin = CommonPlugin.getInstance().getSkinData().loadData(args[0]).orElse(null);

        if (skin == null) {
            sender.sendMessage("§cO jogador " + args[0] + " não possui skin.");
            return;
        }

        CommonPlugin.getInstance().setDefaultSkin(skin);
        sender.sendMessage("§aSkin default alterada para " + args[0] + ".");
    }

    @CommandFramework.Command(name = "servermanager", permission = "command.server")
    public void serverCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            if (sender.hasPermission("command.sender.setserverid")) {
                sender.sendMessage(
                        " §a» §fUse §a/" + cmdArgs.getLabel() + " setserverid <id> §fpara alterar o id do servidor.");
            }

            if (sender.hasPermission("command.sender.setservertype")) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                   " setservertype <type> §fpara alterar o tipo do servidor.");
            }

            return;
        }

        switch (args[0].toLowerCase()) {
        case "setserverid": {
            if (!sender.hasPermission("command.sender.setserverid")) {
                sender.sendMessage("§cVocê não tem permissão para executar este comando.");
                return;
            }

            if (args.length < 2) {
                sender.sendMessage(
                        " §a» §fUse §a/" + cmdArgs.getLabel() + " setserverid <id> §fpara alterar o id do servidor.");
                return;
            }

            String serverId = args[1];

            CommonPlugin.getInstance().getServerData().stopServer();
            CommonPlugin.getInstance().setServerId(serverId);
            BukkitCommon.getInstance().getConfig().set("serverId", serverId);
            BukkitCommon.getInstance().saveConfig();
            sender.sendMessage(" §a» §fId do servidor alterado para §a" + serverId + "§f.");
            CommonPlugin.getInstance().getServerData().startServer(Bukkit.getMaxPlayers());
            Bukkit.getOnlinePlayers()
                  .forEach(player -> CommonPlugin.getInstance().getServerData().joinPlayer(player.getUniqueId()));
            break;
        }
        case "setservertype": {
            if (!sender.hasPermission("command.sender.setservertype")) {
                sender.sendMessage("§cVocê não tem permissão para executar este comando.");
                return;
            }

            if (args.length < 2) {
                sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                                   " setservertype <type> §fpara alterar o tipo do servidor.");
                return;
            }

            ServerType serverType = ServerType.getByName(args[1]);

            if (serverType == null) {
                sender.sendMessage("§cO tipo de servidor §f" + args[1] + "§c não existe.");
                return;
            }

            CommonPlugin.getInstance().getServerData().stopServer();
            CommonPlugin.getInstance().setServerType(serverType);
            BukkitCommon.getInstance().getConfig().set("serverType", serverType.name());
            BukkitCommon.getInstance().saveConfig();
            sender.sendMessage(" §a» §fTipo do servidor alterado para §a" + serverType + "§f.");
            CommonPlugin.getInstance().getServerData().startServer(Bukkit.getMaxPlayers());
            break;
        }
        case "start": {
            CommonPlugin.getInstance().getServerData().startServer(Bukkit.getMaxPlayers());
            Bukkit.getOnlinePlayers()
                  .forEach(player -> CommonPlugin.getInstance().getServerData().joinPlayer(player.getUniqueId()));
            sender.sendMessage("§aO servidor foi iniciado com sucesso.");
            break;
        }
        case "stop": {
            CommonPlugin.getInstance().getServerData().stopServer();
            sender.sendMessage("§aO servidor foi encerrado com sucesso.");
            break;
        }
        }
    }

    @CommandFramework.Command(name = "tps", aliases = {"ticks"}, permission = "command.tps")
    public void tpsCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();

        if (cmdArgs.getArgs().length == 0) {
            long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L;
            long allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L;

            sender.sendMessage(" §aServidor " + CommonPlugin.getInstance().getServerId() + ":");
            sender.sendMessage("    §fPlayers: §7" + Bukkit.getOnlinePlayers().size() + " jogadores");
            sender.sendMessage("    §fMáximo de players: §7" + Bukkit.getMaxPlayers() + " jogadores");
            sender.sendMessage("    §fMemória: §7" + (usedMemory + "/" + allocatedMemory + " MB"));
            sender.sendMessage("    §fLigado há: §7" + StringFormat.formatTime(
                    (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000));
            sender.sendMessage("    §fTPS: ");
            sender.sendMessage("      §f1m: §7" + format(MinecraftServer.getServer().recentTps[0]));
            sender.sendMessage("      §f5m: §7" + format(MinecraftServer.getServer().recentTps[1]));
            sender.sendMessage("      §f15m: §7" + format(MinecraftServer.getServer().recentTps[2]));

            int ping = 0;
            Map<ProtocolVersion, Integer> map = new HashMap<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                ping += ProtocolVersion.getPing(player);

                ProtocolVersion version = ProtocolVersion.getProtocolVersion(player);

                map.putIfAbsent(version, 0);
                map.put(version, map.get(version) + 1);
            }

            ping = ping / Math.max(Bukkit.getOnlinePlayers().size(), 1);

            sender.sendMessage("    §fPing médio: §7" + ping + "ms");
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                sender.sendMessage("    §fVersão: §7");

                for (Map.Entry<ProtocolVersion, Integer> entry : map.entrySet()) {
                    sender.sendMessage(
                            "      §f- " + entry.getKey().name().replace("MINECRAFT_", "").replace("_", ".") + ": §7" +
                            entry.getValue() + " jogadores");
                }
            }

            return;
        }

        if (cmdArgs.getArgs()[0].equalsIgnoreCase("gc")) {
            Runtime.getRuntime().gc();
            sender.sendMessage(" §a» §fVocê passou o GarbargeCollector no servidor.");
        } else {
            World world = Bukkit.getWorld(cmdArgs.getArgs()[0]);

            if (world == null) {
                sender.sendMessage(" §c» §fO mundo " + cmdArgs.getArgs()[0] + " não existe.");
            } else {
                sender.sendMessage(" §aMundo " + world.getName());
                sender.sendMessage("    §fEntidades: §7" + world.getEntities().size());
                sender.sendMessage("    §fLoaded chunks: §7" + world.getLoadedChunks().length);
            }
        }
    }

    @CommandFramework.Command(name = "memoryinfo", permission = "command.tps")
    public void memoryinfoCommand(CommandArgs cmdArgs) {
        long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L;
        long allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L;

        cmdArgs.getSender().sendMessage("  §aMemory Info:");
        cmdArgs.getSender().sendMessage(
                "    §fMemória usada: §7" + usedMemory + "MB (" + ((usedMemory * 100) / allocatedMemory) + "%)");
        cmdArgs.getSender().sendMessage("    §fMemória livre: §7" + (allocatedMemory - usedMemory) + "MB (" +
                                        (((allocatedMemory - usedMemory) * 100) / allocatedMemory) + "%)");
        cmdArgs.getSender().sendMessage("    §fMemória máxima: §7" + allocatedMemory + "MB");
        cmdArgs.getSender()
               .sendMessage("    §fCPU: §7" + CommonConst.DECIMAL_FORMAT.format(CommonConst.getCpuUse()) + "%");
    }

    @CommandFramework.Completer(name = "servermanager")
    public List<String> serverCompleter(CommandArgs cmdArgs) {
        List<String> args = new ArrayList<>();

        if (cmdArgs.getArgs().length == 1) {
            args.addAll(Arrays.asList("setserverid", "setservertype"));
        } else if (cmdArgs.getArgs().length == 2) {
            if (cmdArgs.getArgs()[0].equalsIgnoreCase("setservertype")) {
                args.addAll(Arrays.stream(ServerType.values()).map(ServerType::name).collect(Collectors.toList()));
            }
        }

        return args.stream().filter(arg -> arg.toLowerCase().startsWith(
                           cmdArgs.getArgs()[cmdArgs.getArgs().length == 0 ? 0 : cmdArgs.getArgs().length - 1].toLowerCase()))
                   .collect(Collectors.toList());
    }

    private String format(double tps) {
        return (tps > BukkitConst.TPS * 0.9d ? ChatColor.GREEN :
                (tps > BukkitConst.TPS * 0.8d ? ChatColor.YELLOW : ChatColor.RED)) +
               (tps > BukkitConst.TPS ? "*" : "") + Math.min(Math.round(tps * 100.0D) / 100.0D, BukkitConst.TPS);
    }
}
