package br.com.aspenmc.bukkit.command.register;

import com.google.common.base.Joiner;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.event.player.language.PlayerLanguageChangedEvent;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.language.Language;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TranslationCommand implements CommandHandler {

    @CommandFramework.Command(name = "translate", aliases = { "translation", "traducoes" },
            permission = "command.translate")
    public void translateCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            sender.sendMessage(
                    " §a» §fUse §a/" + cmdArgs.getLabel() + " <language>§f para ver informações sobre a linguagem.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                    " <language> translate <translation>§f para traduzir uma chave pelo chat.");
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                    " <language> <translateId> <translation>§f para traduzir uma chave.");
            return;
        }

        Language language = Language.getByName(args[0]);

        if (language == null) {
            sender.sendMessage(
                    sender.getLanguage().translate("command.language.language-not-found", "%language%", args[0]));
            return;
        }

        if (args.length == 1) {
            sender.sendMessage("§aLíngua " + language.getLanguageName() + "§f:");
            sender.sendMessage("  §fCódigo: §7" + language.getLanguageCode());
            sender.sendMessage("  §fTraduções: §7" +
                    CommonPlugin.getInstance().getLanguageManager().getTranslations(language).size());
            return;
        }

        String translationId = args[1];

        if (args.length == 2) {
            sender.sendMessage("§aLingua " + language.getLanguageName() + "§f:");
            sender.sendMessage("  §f" + translationId + ": §7" + language.translate(translationId));
            return;
        }

        String translate = Joiner.on(' ').join(Arrays.copyOfRange(args, 2, args.length));

        CommonPlugin.getInstance().getLanguageManager().setTranslation(language, translationId, translate);
        sender.sendMessage(sender.getLanguage().translate("command.language.translation-set", "%language%",
                language.getLanguageName(), "%translationId%", translationId, "%translation%", translate));

        CommonPlugin.getInstance().getMemberManager().getMembers().stream()
                    .filter(member -> member.getLanguage() == language).filter(member -> member instanceof BukkitMember)
                    .map(member -> (BukkitMember) member).forEach(member -> Bukkit.getPluginManager().callEvent(
                            new PlayerLanguageChangedEvent(member, member.getLanguage())));
    }

    @CommandFramework.Completer(name = "translate", aliases = { "translation", "traducoes" })
    public List<String> translateCompleter(CommandArgs cmdArgs) {
        List<String> completions = new ArrayList<>();

        if (cmdArgs.getArgs().length == 1) {
            for (Language value : Language.values()) {
                completions.add(value.name());
            }
        } else if (cmdArgs.getArgs().length == 2) {
            Language language = Language.getByName(cmdArgs.getArgs()[0]);

            if (language != null) {
                completions.addAll(CommonPlugin.getInstance().getLanguageManager().getTranslations(language).keySet());
            }
        }

        return completions.stream().filter(completion -> completion.toLowerCase().startsWith(
                                  cmdArgs.getArgs()[cmdArgs.getArgs().length == 0 ? 0 : cmdArgs.getArgs().length - 1]))
                          .collect(Collectors.toList());
    }
}
