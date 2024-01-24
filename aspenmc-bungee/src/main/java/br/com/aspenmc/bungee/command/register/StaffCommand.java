package br.com.aspenmc.bungee.command.register;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.BungeeMain;
import br.com.aspenmc.bungee.entity.BungeeMember;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import br.com.aspenmc.utils.ProtocolVersion;
import br.com.aspenmc.utils.string.MessageBuilder;
import br.com.aspenmc.utils.string.StringFormat;
import br.com.aspenmc.utils.string.TimeFormat;
import com.google.common.base.Joiner;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

public class StaffCommand implements CommandHandler {

    @CommandFramework.Command(name = "staffchat", aliases = { "sc" }, permission = "command.staffchat", console = false)
    public void staffChatCommand(CommandArgs cmdArgs) {
        Member member = cmdArgs.getSenderAsMember();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            member.getPreferencesConfiguration()
                  .setStaffChatEnabled(!member.getPreferencesConfiguration().isStaffChatEnabled());

            if (member.getPreferencesConfiguration().isStaffChatEnabled()) {
                member.getPreferencesConfiguration().setSeeingStaffChatEnabled(true);
            }

            member.sendMessage(
                    member.getPreferencesConfiguration().isStaffChatEnabled() ? "§aAgora você está no chat da equipe." :
                            "§cVocê agora não está mais no chat da equipe.");
            return;
        }

        String message = Joiner.on(' ').join(Arrays.copyOfRange(args, 0, args.length));

        switch (message.toLowerCase()) {
        case "on":
            if (member.getPreferencesConfiguration().isSeeingStaffChatEnabled()) {
                member.sendMessage("§cVocê já está vendo o chat da equipe.");
                return;
            }

            member.getPreferencesConfiguration().setSeeingStaffChatEnabled(true);
            member.sendMessage("§aAgora você está vendo o chat da equipe.");
            break;
        case "off":
            if (!member.getPreferencesConfiguration().isSeeingStaffChatEnabled()) {
                member.sendMessage("§cVocê já não está vendo o chat da equipe.");
                return;
            }

            member.getPreferencesConfiguration().setSeeingStaffChatEnabled(true);
            member.sendMessage("§aAgora você não está vendo mais o chat da equipe.");
            break;
        default:
            BungeeMain.getInstance().sendStaffChatMessage(member, message, true);
            break;
        }
    }

    @CommandFramework.Command(name = "maintenance", aliases = { "manutencao" }, permission = "command.maintenance",
            runAsync = true)
    public void maintenanceCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(sender.t("command.maintenance.usage", "%label%", cmdArgs.getLabel()));
            return;
        }

        switch (args[0].toLowerCase()) {
        case "on":
        case "true":
            if (BungeeMain.getInstance().isMaintenance()) {
                sender.sendMessage(sender.t("command.maintenance.already-enabled"));
                break;
            }

            BungeeMain.getInstance().setMaintenance(true);
            sender.sendMessage(sender.t("command.maintenance.enabled"));
            break;
        case "off":
        case "false":
            if (!BungeeMain.getInstance().isMaintenance()) {
                sender.sendMessage(sender.t("command.maintenance.already-disabled",
                        "§cO servidor já está fora do modo manuntenção!"));
                break;
            }

            BungeeMain.getInstance().setMaintenance(false);
            sender.sendMessage(
                    sender.t("command.maintenance.disabled", "§cO servidor agora não está mais no modo manunteção!"));
            break;
        case "remove":
        case "add": {
            if (args.length == 1) {
                sender.sendMessage(" §a» §");
                return;
            }

            Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[1]).orElse(null);

            if (member == null) {
                sender.sendMessage(sender.t("account-not-found"));
                break;
            }

            boolean add = args[0].equalsIgnoreCase("add");

            if (add) {
                BungeeMain.getInstance().getMaintenanceWhitelist().add(member.getUniqueId());
                sender.sendMessage(
                        sender.t("command.maintenance.added", "§aO jogador %player% foi adicionado a whitelist!",
                                "%player%", member.getName()));
            } else {
                BungeeMain.getInstance().getMaintenanceWhitelist().remove(member.getUniqueId());
                sender.sendMessage(
                        sender.t("command.maintenance.removed", "§cO jogador %player% foi removido da whitelist!",
                                "%player%", member.getName()));
            }
            break;
        }
        default: {
            try {
                long time = StringFormat.getTimeFromString(args[0], true);

                BungeeMain.getInstance().setMaintenance(true, time);
                sender.sendMessage(sender.t("command.maintenance.enabled-time", "%time%",
                        StringFormat.formatTime((time - System.currentTimeMillis()) / 1000)));
            } catch (IllegalArgumentException exception) {
                sender.sendMessage(sender.t("time-format"));
            }

            break;
        }
        }
    }

    @CommandFramework.Command(name = "stafflist", runAsync = true, permission = "command.staff",
            usage = "/<command> <player> <server>")
    public void stafflistCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        int groupId = sender.isPlayer() ? cmdArgs.getSenderAsMember().getServerGroup().getId() + 2 : Integer.MAX_VALUE;

        int ping = 0, count = 0;
        long time = 0;
        Map<ProtocolVersion, Integer> map = new HashMap<>();


        List<Member> array = CommonPlugin.getInstance().getMemberManager().getMembers().stream().filter(Member::isStaff)
                                         .collect(Collectors.toList());

        for (Member member : array) {
            if (member instanceof BungeeMember) {
                ping += ((BungeeMember) member).getProxiedPlayer().getPing();
                count++;

                ProtocolVersion version = ProtocolVersion.getById(
                        ((BungeeMember) member).getProxiedPlayer().getPendingConnection().getVersion());

                map.putIfAbsent(version, 0);
                map.put(version, map.get(version) + 1);
            }

            time += member.getSessionTime();
        }

        ping = ping / Math.max(count, 1);
        time = time / Math.max(array.size(), 1);

        sender.sendMessage("  §aEquipe online:");
        sender.sendMessage("    §fTempo médio: §7" + StringFormat.formatTime((int) (time / 1000), TimeFormat.NORMAL));
        sender.sendMessage("    §fPing médio: §7" + ping + "ms");
        sender.sendMessage("    §fEquipe: §7" + array.size() + " online");

        MessageBuilder messageBuilder = new MessageBuilder("    §fPlayers: §7");

        for (int i = 0; i < array.size(); i++) {
            Member member = array.get(i);
            messageBuilder.extra(new MessageBuilder("§7" + member.getDefaultTag().getRealPrefix() + member.getName() +
                    (i == array.size() - 1 ? "§7." : "§7, ")).setHoverEvent("§fTempo online: §7" +
                                                                     StringFormat.formatTime((int) (member.getSessionTime() / 1000), TimeFormat.NORMAL) +
                                                                     "\n§fPing: §7" +
                                                                     (member instanceof BungeeMember ?
                                                                             ((BungeeMember) member).getProxiedPlayer().getPing() : -1) +
                                                                     "ms" + "\n§fServidor: §7" + member.getCurrentServer() + "")
                                                             .setClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                                                     "/teleport " + member.getName()).create());
        }

        sender.sendMessage(messageBuilder.create());
    }

    @CommandFramework.Command(name = "find", permission = "command.find", runAsync = true)
    public void findCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " <player>§f para saber aonde um jogador está.");
            return;
        }

        String playerName = args[0];
        Member target = CommonPlugin.getInstance().getMemberManager().getMemberByName(playerName).orElse(null);

        if (target == null) {
            sender.sendMessage("§cO jogador " + playerName + " não existe.");
            return;
        }

        if (target.isOnline()) {
            sender.sendMessage(new MessageBuilder(
                    "§aO jogador " + target.getName() + " está na sala " + target.getCurrentServer() + ".")
                    .setHoverEvent("§aClique para se conectar.").setClickEvent("/tp " + target.getName()).create());
        } else {
            sender.sendMessage("§cO jogador " + playerName + " não está online.");
        }
    }

    @CommandFramework.Command(name = "go", permission = "command.go", console = false)
    public void goCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " <player>§f para ir até um jogador.");
            return;
        }

        String playerName = args[0];
        Member target = CommonPlugin.getInstance().getMemberManager().getMemberByName(playerName).orElse(null);

        if (target == null) {
            sender.sendMessage("§cO jogador " + playerName + " não existe.");
            return;
        }

        if (target.isOnline()) {
            cmdArgs.getSenderAsMember(BungeeMember.class).getProxiedPlayer()
                   .connect(ProxyServer.getInstance().getServerInfo(target.getCurrentServer()));
        } else {
            sender.sendMessage("§cO jogador " + playerName + " não está online.");
        }
    }

    @CommandFramework.Command(name = "send", permission = "command.send")
    public void sendCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length <= 1) {
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                    " <player:current:all> <serverId:serverType>§f para enviar um jogador para uma sala.");
            return;
        }

        List<ProxiedPlayer> playerList = new ArrayList<>();

        if (args[0].equalsIgnoreCase("all")) {
            playerList.addAll(ProxyServer.getInstance().getPlayers());
        } else if (args[0].equalsIgnoreCase("current")) {
            if (cmdArgs.isPlayer()) {
                playerList.addAll(cmdArgs.getSenderAsMember(BungeeMember.class).getProxiedPlayer().getServer().getInfo()
                                         .getPlayers());
            } else {
                sender.sendMessage("§cSomente jogadores podem usar esse comando.");
                return;
            }
        } else {
            if (args[0].contains(",")) {
                String[] split = args[0].split(",");

                for (String playerName : split) {
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);

                    if (player == null) {
                        sender.sendMessage("§cO jogador " + playerName + " não está online.");
                        return;
                    }

                    playerList.add(player);
                }
            } else {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);

                if (player == null) {
                    sender.sendMessage("§cO jogador " + args[0] + " não está online.");
                    return;
                }

                playerList.add(player);
            }
        }

        ServerType serverType = null;

        try {
            serverType = ServerType.valueOf(args[1].toUpperCase());
        } catch (Exception ignored) {
        }

        if (serverType == null) {
            ProxiedServer proxiedServer = CommonPlugin.getInstance().getServerManager().getServerByName(args[1]);

            if (proxiedServer == null || proxiedServer.getServerInfo() == null) {
                sender.sendMessage("§cO servidor " + args[1] + " não foi encontrado.");
                return;
            }

            playerList.forEach(player -> player.connect(proxiedServer.getServerInfo()));

            if (args[0].equalsIgnoreCase("current")) {
                sender.sendMessage(
                        "§aTodos os jogadores da sala foram enviados para o servidor " + proxiedServer.getServerId() +
                                ".");
            } else if (args[0].equalsIgnoreCase("all")) {
                sender.sendMessage(
                        "§aTodos os jogadores foram enviados para o servidor " + proxiedServer.getServerId() + ".");
            } else {
                sender.sendMessage("§aOs jogadores " +
                        playerList.stream().map(ProxiedPlayer::getName).collect(Collectors.joining(", ")) +
                        " foram enviados para o servidor " + proxiedServer.getServerId() + ".");
            }
        } else {
            List<ProxiedServer> servers = CommonPlugin.getInstance().getServerManager().getBalancer(serverType)
                                                      .getList();

            if (servers.isEmpty()) {
                sender.sendMessage("§cNão há nenhum servidor do tipo " + serverType.name() + " disponível.");
                return;
            }

            int index = 0;

            for (ProxiedPlayer player : playerList) {
                player.connect(servers.get(index).getServerInfo(), ServerConnectEvent.Reason.COMMAND);

                index++;

                if (index >= servers.size()) {
                    index = 0;
                }
            }

            if (args[0].equalsIgnoreCase("current")) {
                sender.sendMessage(
                        "§aTodos os jogadores da sala foram enviados para o servidor " + serverType.name() + ".");
            } else if (args[0].equalsIgnoreCase("all")) {
                sender.sendMessage("§aTodos os jogadores foram enviados para o servidor " + serverType.name() + ".");
            } else {
                sender.sendMessage("§aOs jogadores " +
                        playerList.stream().map(ProxiedPlayer::getName).collect(Collectors.joining(", ")) +
                        " foram enviados para o servidor " + serverType.name() + ".");
            }
        }
    }

    @CommandFramework.Command(name = "broadcast", aliases = { "bc" }, permission = "command.broadcast")
    public void broadcastCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " <message>§f para enviar uma mensagem no servidor.");
            return;
        }

        String message = Joiner.on(' ').join(args).replace('&', '§');

        ProxyServer.getInstance().broadcast("");
        ProxyServer.getInstance().broadcast("§bAspen> §f" + message);
        ProxyServer.getInstance().broadcast("");
    }


    @CommandFramework.Command(name = "top", permission = "command.top")
    public void topCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();

        if (cmdArgs.getArgs().length > 0 && cmdArgs.getArgs()[0].equalsIgnoreCase("gc")) {
            Runtime.getRuntime().gc();
            sender.sendMessage("§aVocê passou o Garbage Collector do java no BungeeCord.");
            return;
        }

        long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 2L / 1048576L;
        long allocatedMemory = Runtime.getRuntime().totalMemory() / 1048576L;

        sender.sendMessage(" ");
        sender.sendMessage("  §aBungeeCord Usage Info:");
        sender.sendMessage(
                "    §fMemória usada: §7" + usedMemory + "MB (" + ((usedMemory * 100) / allocatedMemory) + "%)");
        sender.sendMessage("    §fMemória livre: §7" + (allocatedMemory - usedMemory) + "MB (" +
                (((allocatedMemory - usedMemory) * 100) / allocatedMemory) + "%)");
        sender.sendMessage("    §fMemória máxima: §7" + allocatedMemory + "MB");
        sender.sendMessage("    §fCPU: §7" + CommonConst.DECIMAL_FORMAT.format(CommonConst.getCpuUse()) + "%");
        sender.sendMessage("    §fPing médio: §7" + BungeeMain.getInstance().getAveragePing() + "ms.");
    }

    @CommandFramework.Command(name = "glist", permission = "command.glist")
    public void glistCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            handleInfo("global", sender, ProxyServer.getInstance().getPlayers(), -1, -1,
                    ManagementFactory.getRuntimeMXBean().getStartTime());
            return;
        }

        String serverId = args[0];
        ProxiedServer server = CommonPlugin.getInstance().getServerManager().getServer(serverId);

        if (server == null) {
            sender.sendMessage(sender.t("server-not-found", "%serverId%", args[0]));
            return;
        }

        handleInfo(server.getServerId(), sender, server.getServerInfo().getPlayers(), server.getMaxPlayers(),
                server.getPlayersRecord(), server.getStartTime());
    }

    private void handleInfo(String serverId, Sender sender, Collection<ProxiedPlayer> serverPlayers, int maxPlayers,
            int playerRecord, long startTime) {
        sender.sendMessage("  §aServidor " + serverId + ": ");

        sender.sendMessage("    §fPlayers: §7" + serverPlayers.size() + (maxPlayers == -1 ? "" : "/" + maxPlayers));
        sender.sendMessage("    §fRecord de players: §7" + playerRecord);

        long ping = -1;
        Map<ProtocolVersion, Integer> map = new HashMap<>();

        for (ProxiedPlayer player : serverPlayers) {
            ping += player.getPing();

            ProtocolVersion version = ProtocolVersion.getById(player.getPendingConnection().getVersion());

            map.putIfAbsent(version, 0);
            map.put(version, map.get(version) + 1);
        }

        ping = ping / Math.max(serverPlayers.size(), 1);

        sender.sendMessage("    §fPing médio: §7" + ping + "ms");

        if (!serverPlayers.isEmpty()) {
            sender.sendMessage("    §fVersão: §7");

            for (Map.Entry<ProtocolVersion, Integer> entry : map.entrySet()) {
                sender.sendMessage(
                        "      §f- " + entry.getKey().name().replace("MINECRAFT_", "").replace("_", ".") + ": §7" +
                                entry.getValue() + " jogadores");
            }
        }

        sender.sendMessage(
                "    §fLigado há: §7" + StringFormat.formatTime((System.currentTimeMillis() - startTime) / 1000));
    }
}
