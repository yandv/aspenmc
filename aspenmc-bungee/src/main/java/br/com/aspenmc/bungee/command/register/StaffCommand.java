package br.com.aspenmc.bungee.command.register;

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

            if (member.getPreferencesConfiguration().isStaffChatEnabled())
                member.getPreferencesConfiguration().setSeeingStaffChatEnabled(true);

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

    @CommandFramework.Command(name = "glist", permission = "command.glist")
    public void glistCommand(CommandArgs cmdArgs) {

    }
}
