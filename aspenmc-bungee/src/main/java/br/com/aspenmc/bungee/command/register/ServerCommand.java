package br.com.aspenmc.bungee.command.register;

import br.com.aspenmc.bungee.entity.BungeeMember;
import br.com.aspenmc.server.ProxiedServer;
import com.google.common.base.Joiner;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.server.ServerType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerCommand implements CommandHandler {

    @CommandFramework.Command(name = "report", aliases = {"reportar", "r"}, console = false)
    public void reportCommand(CommandArgs cmdArgs) {
        BungeeMember member = cmdArgs.getSenderAsMember(BungeeMember.class);
        String[] args = cmdArgs.getArgs();

        if (args.length <= 1) {
            member.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " <player> <reason>§f para denunciar um jogador.");
            return;
        }

        Sender target = CommonPlugin.getInstance().getMemberManager().getMemberByName(args[0]).orElse(null);

        if (target == null) {
            member.sendMessage("§cO jogador " + args[0] + " não foi encontrado.");
            return;
        }

        String reason = Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));

        ProxyServer.getInstance().broadcast(member.getName() + " reportou " + target.getRealName() + " por " + reason);
    }

    @CommandFramework.Command(name = "ping")
    public void pingCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            if (sender.isPlayer()) {
                sender.sendMessage(
                        "§eSeu ping é de §b" + ((BungeeMember) sender).getProxiedPlayer().getPing() + "ms§e.");
            } else {
                int ping = ProxyServer.getInstance().getPlayers().stream().mapToInt(ProxiedPlayer::getPing).sum() /
                           Math.max(1, ProxyServer.getInstance().getOnlineCount());

                sender.sendMessage("§aO ping médio do servidor é de " + ping + "ms.");
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

    @CommandFramework.Command(name = "connect", aliases = {"server"}, console = false)
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

    @CommandFramework.Command(name = "lobby", aliases = {"hub", "l"}, console = false)
    public void lobbyCommand(CommandArgs cmdArgs) {
        BungeeMember member = cmdArgs.getSenderAsMember(BungeeMember.class);
        String[] args = cmdArgs.getArgs();

        ServerType serverType = member.getCurrentServerType();

        if (args.length > 0) {
            serverType = ServerType.getByName(args[0]);
        }

        if (serverType == null) {
            serverType = ServerType.LOBBY;
        } else {
            if (serverType != ServerType.LOBBY && serverType.hasParent())
                serverType = serverType.getParent();
        }

        ProxiedServer server = CommonPlugin.getInstance().getServerManager().getBalancer(serverType).next();

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

    @CommandFramework.Completer(name = "report", aliases = {"reportar", "r"})
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

    @CommandFramework.Completer(name = "connect", aliases = {"server"})
    public List<String> connectCompleter(CommandArgs cmdArgs) {
        return CommonPlugin.getInstance().getServerManager().getServers().stream().map(ProxiedServer::getServerId)
                           .map(String::toLowerCase)
                           .filter(serverId -> serverId.startsWith(cmdArgs.getArgs()[0].toLowerCase()))
                           .collect(Collectors.toList());
    }

    @CommandFramework.Completer(name = "lobby")
    public List<String> lobbyCompleter(CommandArgs cmdArgs) {
        return Stream.of(ServerType.values()).filter(ServerType::isLobby).map(ServerType::getName)
                     .map(String::toUpperCase)
                     .filter(serverType -> serverType.startsWith(cmdArgs.getArgs()[0].toUpperCase()))
                     .collect(Collectors.toList());
    }
}
