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
import br.com.aspenmc.packet.type.discord.MessageRequest;
import br.com.aspenmc.server.ProxiedServer;
import br.com.aspenmc.server.ServerType;
import com.google.common.base.Joiner;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerCommand implements CommandHandler {

    @CommandFramework.Command(name = "report", aliases = { "reportar", "r" }, console = false)
    public void reportCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSenderAsMember(BungeeMember.class);
        String[] args = cmdArgs.getArgs();

        if (args.length <= 1) {
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " <player> <reason>§f para denunciar um jogador.");
            return;
        }

        Member target = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[0]).orElse(null);

        if (target == null) {
            sender.sendMessage(sender.t("player-not-found", "%player%", args[0]));
            return;
        }

        String reason = Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));

        CommonPlugin.getInstance().getMemberManager().getMembers().stream()
                    .filter(m -> m.hasPermission(CommonConst.ADMIN_MODE_PERMISSION))
                    .filter(m -> m.getPreferencesConfiguration().isSeeingReportsEnabled()).forEach(m -> m.sendMessage(
                            m.t("command.report.report-staff-broadcast", "%player%", target.getName(), "%reason%",
                                    reason,
                                    "%reporter%", sender.getName())));
        MessageRequest.sendReportMessage(sender, target, reason);
    }

    @CommandFramework.Command(name = "ping")
    public void pingCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            if (sender.isPlayer()) {
                int yourPing = ((BungeeMember) sender).getProxiedPlayer().getPing();

                if (sender.hasPermission(CommonConst.ADMIN_MODE_PERMISSION)) {
                    double serverAveragePing = Math.max(BungeeMain.getInstance().getAveragePing(), 10E-16);
                    int playerCount = ProxyServer.getInstance().getOnlineCount();

                    long sum = 0;

                    for (ProxiedPlayer i : ProxyServer.getInstance().getPlayers()) {
                        sum += Math.pow(i.getPing() - serverAveragePing, 2);
                    }

                    double variance = sum / (double) playerCount;
                    double standardDeviation = Math.sqrt(variance);
                    double relativePing = yourPing / serverAveragePing;

                    sender.sendMessage("§eSeu ping é de §b" + yourPing + "ms §7(" + relativePing + "x ms)§e.");
                    sender.sendMessage("§eO ping médio do servidor é de §b" +
                            CommonConst.DECIMAL_FORMAT.format(serverAveragePing) + "ms§e com desvio padrão de §b" +
                            standardDeviation + "ms §7(" + variance + ")§e.");
                } else {
                    sender.sendMessage("§eSeu ping é de §b" + yourPing + "ms§e.");
                }
            } else {
                sender.sendMessage(
                        "§aO ping médio do servidor é de " + BungeeMain.getInstance().getAveragePing() + "ms.");
                sender.sendMessage("§aO servidor atualmente tem " + ProxyServer.getInstance().getOnlineCount() +
                        " jogadores online.");
            }

            return;
        }

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage("§cO jogador " + args[0] + " não foi encontrado.");
            return;
        }

        sender.sendMessage("§eO ping de §b" + player.getName() + "§e é de §b" + player.getPing() + "§ems.");
    }

    @CommandFramework.Command(name = "connect", aliases = { "server" }, console = false)
    public void connectCommand(CommandArgs cmdArgs) {
        BungeeMember member = cmdArgs.getSenderAsMember(BungeeMember.class);
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            member.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " <serverId>§f para se conectar a um servidor.");
            return;
        }

        String serverId = args[0];
        ProxiedServer server = CommonPlugin.getInstance().getServerManager().getServerByName(serverId);

        if (server == null) {
            member.sendMessage("§cO servidor não foi encontrado.");
            return;
        }

        if (server.getServerInfo() == null) {
            member.sendMessage("§cO servidor está indisponível no momento.");
            return;
        }

        if (server.getServerId().equals(member.getCurrentServer())) {
            member.sendMessage("§cVocê já está conectado a este servidor.");
            return;
        }

        member.getProxiedPlayer().connect(server.getServerInfo());
    }

    @CommandFramework.Command(name = "lobby", aliases = { "hub", "l" }, console = false)
    public void lobbyCommand(CommandArgs cmdArgs) {
        BungeeMember member = cmdArgs.getSenderAsMember(BungeeMember.class);

        ServerType serverType = member.getCurrentServerType();

        if (serverType.hasParent()) {
            serverType = serverType.getParent();
        }

        ProxiedServer server = CommonPlugin.getInstance().getServerManager().getBalancer(serverType, ServerType.LOBBY)
                                           .next();

        if (server == null) {
            member.sendMessage("§cNenhum lobby encontrado no momento.");
            return;
        }

        if (server.getServerInfo() == null) {
            member.sendMessage("§cO servidor está indisponível no momento.");
            return;
        }

        if (server.getServerId().equals(member.getCurrentServer())) {
            member.sendMessage("§cVocê já está conectado a este servidor.");
            return;
        }

        member.getProxiedPlayer().connect(server.getServerInfo());
    }

    @CommandFramework.Command(name = "ip", console = false)
    public void ipCommand(CommandArgs cmdArgs) {
        cmdArgs.getSender().sendMessage(cmdArgs.getSender().t("command.ip.connected-server", "%ip%",
                cmdArgs.getSenderAsMember().getCurrentServer()));
    }

    @CommandFramework.Completer(name = "report", aliases = { "reportar", "r", "find", "go" })
    public List<String> reportCompleter(CommandArgs cmdArgs) {
        return CommonPlugin.getInstance().getMemberManager().getMembers().stream()
                           .map(member -> member.isUsingFake() ? member.getFakeName() : member.getName())
                           .filter(name -> name.toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
                           .collect(Collectors.toList());
    }

    @CommandFramework.Completer(name = "ping")
    public List<String> pingCompleter(CommandArgs cmdArgs) {
        return CommonPlugin.getInstance().getMemberManager().getMembers().stream()
                           .map(member -> member.isUsingFake() ? member.getFakeName() : member.getName())
                           .filter(name -> name.toLowerCase().startsWith(cmdArgs.getArgs()[0].toLowerCase()))
                           .collect(Collectors.toList());
    }

    @CommandFramework.Completer(name = "connect", aliases = { "server", "glist" })
    public List<String> connectCompleter(CommandArgs cmdArgs) {
        return CommonPlugin.getInstance().getServerManager().getServers().stream().map(ProxiedServer::getServerId)
                           .map(String::toLowerCase)
                           .filter(serverId -> serverId.startsWith(cmdArgs.getArgs()[0].toLowerCase()))
                           .collect(Collectors.toList());
    }
}
