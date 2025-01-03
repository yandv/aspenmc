package br.com.aspenmc.bungee.command.register;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.entity.BungeeMember;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.packet.type.discord.MessageRequest;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;
import br.com.aspenmc.utils.string.StringFormat;
import com.google.common.base.Joiner;

import java.util.Arrays;

public class PunishCommand implements CommandHandler {

    @CommandFramework.Command(name = "ban", permission = "command.ban", runAsync = true)
    public void banCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length < 1) {
            sender.sendMessage(sender.t("command.ban.usage"));
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage(sender.t("player-not-found", "%player%", args[0]));
            return;
        }

        String reason = args.length == 1 ? "Sem motivo" : Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));

        Punish currentPunish = member.getPunishConfiguration().getCurrentPunish(PunishType.BAN);

        if (currentPunish != null && currentPunish.isPermanent()) {
            sender.sendMessageFormatted("§cO jogador %player% já está banido.", "%player%", member.getName());
            return;
        }

        Punish punish = CommonPlugin.getInstance().getPunishService()
                                    .createPunish(member, sender, PunishType.BAN, reason, -1L).join();

        if (punish == null) {
            sender.sendMessage("§cNão foi possível banir o jogador %player%, tente novamente.", "%player%",
                    member.getName());
            return;
        }

        if (member instanceof BungeeMember) {
            BungeeMember bungeeMember = (BungeeMember) member;

            bungeeMember.getProxiedPlayer().disconnect(punish.getPunishMessage(member.getLanguage()));
        }


        member.getPunishConfiguration().punish(punish);
        sender.sendMessage("§aO jogador %player% foi banido com sucesso pelo motivo %reason%.", "%player%",
                member.getName(), "%reason%", reason);

        MessageRequest.sendPlayerPunish(punish);
    }

    @CommandFramework.Command(name = "tempban", permission = "command.tempban", runAsync = true)
    public void tempbanCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length < 2) {
            sender.sendMessage(sender.t("command.tempban.usage"));
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage(sender.t("player-not-found", "%player%", args[0]));
            return;
        }

        long time = StringFormat.getTimeFromString(args[1], true);

        String reason = args.length == 2 ? "Sem motivo" : Joiner.on(' ').join(Arrays.copyOfRange(args, 2, args.length));

        Punish currentPunish = member.getPunishConfiguration().getCurrentPunish(PunishType.BAN);

        if (currentPunish != null && currentPunish.isPermanent()) {
            sender.sendMessageFormatted("§cO jogador %player% já está banido.", "%player%", member.getName());
            return;
        }

        Punish punish = CommonPlugin.getInstance().getPunishService()
                                    .createPunish(member, sender, PunishType.BAN, reason, time).join();

        if (punish == null) {
            sender.sendMessageFormatted("§cNão foi possível banir o jogador %player%, tente novamente.", "%player%",
                    member.getName());
            return;
        }

        if (member instanceof BungeeMember) {
            BungeeMember bungeeMember = (BungeeMember) member;

            bungeeMember.getProxiedPlayer().disconnect(punish.getPunishMessage(member.getLanguage()));
        }

        member.getPunishConfiguration().punish(punish);
        sender.sendMessageFormatted("§aO jogador %player% foi banido com sucesso pelo motivo %reason%.", "%player%",
                member.getName(), "%reason%", reason);

        MessageRequest.sendPlayerPunish(punish);
    }

    @CommandFramework.Command(name = "unban", permission = "command.unban", runAsync = true)
    public void unbanCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length < 1) {
            sender.sendMessage(sender.t("command.unban.usage"));
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage(sender.t("player-not-found", "%player%", args[0]));
            return;
        }

        String reason = args.length == 1 ? "Sem motivo" : Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));
        Punish punish = member.getPunishConfiguration().getCurrentPunish(PunishType.BAN);

        if (punish == null) {
            sender.sendMessage("§cO jogador %player% não está banido.", "%player%", member.getName());
            return;
        }

        member.getPunishConfiguration().revoke(PunishType.BAN, sender.getUniqueId(), sender.getName(), reason);
        sender.sendMessageFormatted("§aO jogador %player% foi desbanido com sucesso.", "%player%", member.getName());

        MessageRequest.sendPlayerPunish(punish);
    }

    @CommandFramework.Command(name = "mute", permission = "command.mute", runAsync = true)
    public void muteCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length < 1) {
            sender.sendMessage(sender.t("command.mute.usage"));
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage(sender.t("player-not-found", "%player%", args[0]));
            return;
        }

        String reason = args.length == 1 ? "Sem motivo" : Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));

        Punish currentPunish = member.getPunishConfiguration().getCurrentPunish(PunishType.MUTE);

        if (currentPunish != null && currentPunish.isPermanent()) {
            sender.sendMessageFormatted("§cO jogador %player% já está mutado.", "%player%", member.getName());
            return;
        }

        Punish punish = CommonPlugin.getInstance().getPunishService()
                                    .createPunish(member, sender, PunishType.MUTE, reason, -1L).join();

        if (punish == null) {
            sender.sendMessageFormatted("§cNão foi possível mutar o jogador %player%, tente novamente.", "%player%",
                    member.getName());
            return;
        }

        member.getPunishConfiguration().punish(punish);
        member.sendMessageFormatted(punish.getPunishMessage(member.getLanguage()));
        sender.sendMessageFormatted("§aO jogador %player% foi mutado com sucesso pelo motivo %reason%.", "%player%",
                member.getName(), "%reason%", reason);

        MessageRequest.sendPlayerPunish(punish);
    }

    @CommandFramework.Command(name = "tempmute", permission = "command.tempmute", runAsync = true)
    public void tempmuteCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length < 2) {
            sender.sendMessage(sender.t("command.tempmute.usage"));
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage(sender.t("player-not-found", "%player%", args[0]));
            return;
        }

        long time = StringFormat.getTimeFromString(args[1], true);

        String reason = args.length == 2 ? "Sem motivo" : Joiner.on(' ').join(Arrays.copyOfRange(args, 2, args.length));

        Punish currentPunish = member.getPunishConfiguration().getCurrentPunish(PunishType.MUTE);

        if (currentPunish != null && currentPunish.isPermanent()) {
            sender.sendMessageFormatted("§cO jogador %player% já está mutado.", "%player%", member.getName());
            return;
        }

        Punish punish = CommonPlugin.getInstance().getPunishService()
                                    .createPunish(member, sender, PunishType.MUTE, reason, time).join();

        if (punish == null) {
            sender.sendMessageFormatted("§cNão foi possível mutar o jogador %player%, tente novamente.", "%player%",
                    member.getName());
            return;
        }

        member.getPunishConfiguration().punish(punish);
        member.sendMessageFormatted(punish.getPunishMessage(member.getLanguage()));
        sender.sendMessageFormatted("§aO jogador %player% foi mutado com sucesso pelo motivo %reason%.", "%player%",
                member.getName(), "%reason%", reason);
    }

    @CommandFramework.Command(name = "unmute", permission = "command.unmute", runAsync = true)
    public void unmuteCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length < 1) {
            sender.sendMessage(sender.t("command.unmute.usage"));
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage(sender.t("player-not-found", "%player%", args[0]));
            return;
        }

        String reason = args.length == 1 ? "Sem motivo" : Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));
        Punish punish = member.getPunishConfiguration().getCurrentPunish(PunishType.MUTE);

        if (punish == null) {
            sender.sendMessageFormatted("§cO jogador %player% não está mutado.", "%player%", member.getName());
            return;
        }

        member.getPunishConfiguration().revoke(PunishType.MUTE, sender.getUniqueId(), sender.getName(), reason);
        sender.sendMessageFormatted("§aO jogador %player% foi desmutado com sucesso.", "%player%", member.getName());
    }
}
