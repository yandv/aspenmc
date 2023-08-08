package br.com.aspenmc.bungee.command.register;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.entity.BungeeMember;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.utils.string.StringFormat;
import com.google.common.base.Joiner;
import br.com.aspenmc.bungee.BungeeMain;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Member;

import java.util.Arrays;

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
            BungeeMain.getInstance().sendStaffChatMessage(member, message);
            break;
        }
    }

    @CommandFramework.Command(name = "maintenance", aliases = { "manutencao" }, permission = "command.maintenance",
            runAsync = true)
    public void maintenanceCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(sender.t("command.maintenance.usage",
                    " §a» §fUse §a/%label% <on:off>§f para ativar ou desativar a manutenção." + "\n" +
                            " §a» §fUse §a/%label% <add:remove> <player>§f para adicionar ou removar alguém da " +
                            "whitelist.", "%label%", cmdArgs.getLabel()));
            return;
        }

        switch (args[0].toLowerCase()) {
        case "on":
        case "true":
            if (BungeeMain.getInstance().isMaintenance()) {
                sender.sendMessage(
                        sender.t("command.maintenance.already-enabled", "§cO servidor já está no modo manuntenção!"));
                break;
            }

            BungeeMain.getInstance().setMaintenance(true);
            sender.sendMessage(sender.t("command.maintenance.enabled", "§aO servidor agora está no modo manunteção!"));
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
                sender.sendMessage(sender.t("command.maintenance.enabled-time",
                        "§aO servidor agora está no modo manunteção por %time%.", "%time%",
                        StringFormat.formatTime((time - System.currentTimeMillis()) / 1000)));
            } catch (IllegalArgumentException exception) {
                sender.sendMessage(sender.t("time-format"));
            }

            break;
        }
        }
    }

    @CommandFramework.Command(name = "glist", permission = "command.glist")
    public void glistCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        String serverId;

        if (sender.isPlayer()) {

        }
    }
}
