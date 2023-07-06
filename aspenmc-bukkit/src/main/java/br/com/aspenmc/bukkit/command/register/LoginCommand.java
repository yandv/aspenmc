package br.com.aspenmc.bukkit.command.register;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.player.MemberLoginEvent;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.Sender;
import org.bukkit.Bukkit;

public class LoginCommand implements CommandHandler {

    @CommandFramework.Command(name = "register", aliases = {"registrar"}, console = false)
    public void registerCommand(CommandArgs cmdArgs) {
        Member member = cmdArgs.getSenderAsMember();
        String[] args = cmdArgs.getArgs();

        if (member.getLoginConfiguration().isRegistered()) {
            member.sendMessage("§cSua conta já foi registrada.");
            return;
        }

        if (args.length <= 1) {
            member.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " <senha> <senha> §fpara registrar uma conta.");
            return;
        }

        String passWord = args[0];

        if (!passWord.equals(args[1])) {
            member.sendMessage("§cAs senhas inseridas não são iguais.");
            return;
        }

        member.getLoginConfiguration().register(passWord);
        member.sendMessage("§aSua conta foi registrada com sucesso.");
        Bukkit.getPluginManager().callEvent(new MemberLoginEvent((BukkitMember) member));
    }

    @CommandFramework.Command(name = "login", aliases = {"logar"}, console = false)
    public void loginCommand(CommandArgs cmdArgs) {
        Member member = cmdArgs.getSenderAsMember();
        String[] args = cmdArgs.getArgs();

        if (member.getLoginConfiguration().isLogged()) {
            member.sendMessage("§cSomente usuários sem uma sessão ativa podem executar este comando.");
            return;
        }

        if (!member.getLoginConfiguration().isCaptch()) {
            member.sendMessage("§cComplete o captcha para iniciar uma sessão.");
            return;
        }

        if (!member.getLoginConfiguration().isRegistered()) {
            member.sendMessage("§cSomente jogadores registrados podem executar este comando.");
            return;
        }

        if (args.length == 0) {
            member.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() + " <senha> §fpara iniciar uma sessão.");
            return;
        }

        String passWord = args[0];

        if (!member.getLoginConfiguration().logIn(passWord)) {
            member.sendMessage("§cA senha inserida é inválida, você possui mais 5 tentativas.");
            return;
        }

        Bukkit.getPluginManager().callEvent(new MemberLoginEvent((BukkitMember) member));
        member.sendMessage("§aSua conta foi autenticada com sucesso.");
    }

    @CommandFramework.Command(name = "changepassword", aliases = {"mudarsenha"})
    public void changepassword(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (!sender.isPlayer()) {
            if (args.length < 2) {
                sender.sendMessage(
                        "§a» §fUse §a/" + cmdArgs.getLabel() + " <player> <senha> §fpara mudar a senha de um jogador.");
                return;
            }

            Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

            if (member == null) {
                sender.sendMessage("§cO jogador §f" + args[0] + " §cnão foi encontrado.");
                return;
            }

            member.getLoginConfiguration().changePassword(args[1]);
            member.sendMessage("§aA senha do jogador §f" + member.getName() + " §afoi alterada com sucesso.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§a» §fUse §a/" + cmdArgs.getLabel() + " <senha> <senha> §fpara mudar sua senha.");
            return;
        }

        String passWord = args[0];

        if (!passWord.equals(args[1])) {
            sender.sendMessage("§cAs senhas inseridas não são iguais.");
            return;
        }

        cmdArgs.getSenderAsMember().getLoginConfiguration().changePassword(passWord);
        cmdArgs.getSenderAsMember().getLoginConfiguration().logIn(passWord);
        sender.sendMessage("§aSua senha foi alterada com sucesso.");
    }
}
