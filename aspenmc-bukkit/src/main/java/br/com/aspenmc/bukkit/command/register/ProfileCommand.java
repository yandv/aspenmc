package br.com.aspenmc.bukkit.command.register;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.player.PlayerChangedSkinEvent;
import br.com.aspenmc.bukkit.menu.profile.YourLanguageInventory;
import br.com.aspenmc.bukkit.menu.profile.YourProfileInventory;
import br.com.aspenmc.bukkit.menu.skin.SkinInventory;
import br.com.aspenmc.bukkit.utils.PlayerAPI;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.Skin;
import br.com.aspenmc.entity.member.configuration.LoginConfiguration;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.packet.type.member.skin.SkinChangeRequest;
import br.com.aspenmc.packet.type.member.skin.SkinChangeResponse;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ProfileCommand implements CommandHandler {

    @CommandFramework.Command(name = "profile", aliases = { "perfil" }, console = false)
    public void profileCommand(CommandArgs cmdArgs) {
        new YourProfileInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer());
    }

    @CommandFramework.Command(name = "language", aliases = { "lang" }, console = false)
    public void languageCommand(CommandArgs cmdArgs) {
        Member member = cmdArgs.getSenderAsMember();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            new YourLanguageInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer());
            return;
        }

        Language language = Language.getByName(Joiner.on(' ').join(Arrays.copyOfRange(args, 0, args.length)));

        if (language == null) {
            member.sendMessage(member.getLanguage()
                                     .t("command.language.language-not-found", "§cO idioma " + args[0] + " não existe.",
                                             "%language%", args[0]));
            return;
        }

        if (language == member.getLanguage()) {
            member.sendMessage(member.getLanguage().t("command.language.language-already-set",
                    "§cO seu idioma atual já é " + language.getLanguageName() + ".", "%language%",
                    language.getLanguageName()));
            return;
        }

        language = member.setLanguage(language);
        member.sendMessage(member.getLanguage().t("command.language.language-changed",
                "§aSeu idioma foi alterado para " + language.getLanguageName() + " com sucesso.", "%language%",
                language.getLanguageName()));
    }

    @CommandFramework.Command(name = "skin", runAsync = true, console = false)
    public void skinCommand(CommandArgs cmdArgs) {
        BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            new SkinInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer());
            return;
        }

        String skinName = args[0];

        if (!CommonConst.NAME_PATTERN.matcher(skinName).matches()) {
            member.sendMessage("§cO nick " + skinName + " não é válido.");
            return;
        }

        Skin skin = CommonPlugin.getInstance().getSkinData().loadData(skinName).orElse(null);

        if (skin == null) {
            member.sendMessage(
                    member.t("command.skin.skin-not-found", "§cNenhuma skin encontrada para o usuário %player%.",
                            "%player%", skinName));
            return;
        }

        CommonPlugin.getInstance().getPacketManager().waitPacket(SkinChangeResponse.class,
                CommonPlugin.getInstance().getPacketManager().sendPacket(
                        new SkinChangeRequest(member.getUniqueId(), CommonPlugin.getInstance().getDefaultSkin())), 500,
                packet -> {
                    if (packet == null) {
                        member.sendMessage(member.t("skin-change-error"));
                        return;
                    }

                    if (packet.getSkinResult() == SkinChangeResponse.SkinResult.SUCCESS) {
                        PlayerAPI.changePlayerSkin(member.getPlayer(), skin, true);
                        Bukkit.getPluginManager().callEvent(new PlayerChangedSkinEvent(member, skin));
                        member.setPlayerSkin(skinName);
                        member.setSkin(skin);
                        member.sendMessage(member.translate("skin-changed-sucess", "%player%", skinName));
                    } else {
                        member.sendMessage("§c" + packet.getErrorMessage());
                    }
                });
    }

    @CommandFramework.Command(name = "skin.#", runAsync = true, console = false)
    public void skinresetCommand(CommandArgs cmdArgs) {
        BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);

        Skin newSkin = member.getLoginConfiguration().getAccountType() == LoginConfiguration.AccountType.PREMIUM ?
                CommonPlugin.getInstance().getSkinData().loadData(member.getPlayerSkin())
                            .orElse(CommonPlugin.getInstance().getDefaultSkin()) :
                CommonPlugin.getInstance().getDefaultSkin();

        CommonPlugin.getInstance().getPacketManager().waitPacket(SkinChangeResponse.class,
                CommonPlugin.getInstance().getPacketManager()
                            .sendPacket(new SkinChangeRequest(member.getUniqueId(), newSkin)), 500, packet -> {
                    if (packet == null) {
                        member.sendMessage(member.t("skin-change-error"));
                        return;
                    }

                    if (packet.getSkinResult() == SkinChangeResponse.SkinResult.SUCCESS) {
                        PlayerAPI.changePlayerSkin(member.getPlayer(), newSkin, true);
                        Bukkit.getPluginManager().callEvent(new PlayerChangedSkinEvent(member, newSkin));
                        member.setPlayerSkin(member.getName());
                        member.setSkin(newSkin);
                        member.sendMessage(
                                member.translate("skin-changed-to-default-sucess", "%player%", member.getName()));
                    } else {
                        member.sendMessage("§c" + packet.getErrorMessage());
                    }
                });
    }

    @CommandFramework.Command(name = "fakereset", aliases = { "nick.#", "fake.#" }, permission = "command.fake",
            runAsync = true, console = false)
    public void fakeResetCommand(CommandArgs cmdArgs) {
        BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            member.sendMessage(" §e» §fUse §a/" + cmdArgs.getLabel() + " <nick>§f para alterar seu nick.");
            return;
        }

        member.setFakeName(null);
        member.setTag(member.getDefaultTag());
        PlayerAPI.changePlayerName(member.getPlayer(), member.getName(), true);
        member.sendMessage("§aO seu nick foi alterado para o original.");
    }

    @CommandFramework.Command(name = "fake", aliases = { "nick" }, permission = "command.fake", runAsync = true,
            console = false)
    public void fakeCommand(CommandArgs cmdArgs) {
        BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            member.sendMessage(" §e» §fUse §a/" + cmdArgs.getLabel() + " <nick>§f para alterar seu nick.");
            return;
        }

        if (args[0].equalsIgnoreCase(member.getName())) {
            member.setFakeName(null);
            member.setTag(member.getDefaultTag());
            PlayerAPI.changePlayerName(member.getPlayer(), member.getName(), true);
            member.sendMessage("§aO seu nick foi alterado para o original.");
            return;
        }

        String fakeName = args[0];

        if (!CommonConst.NAME_PATTERN.matcher(fakeName).matches()) {
            member.sendMessage("§cO nick " + fakeName + " não é válido.");
            return;
        }

        CompletableFuture<UUID> idFuture = CompletableFuture.supplyAsync(
                () -> CommonPlugin.getInstance().getMojangId(fakeName));
        CompletableFuture<? extends Member> memberFuture = CommonPlugin.getInstance().getMemberData()
                                                                       .getMemberByName(fakeName, true);

        CompletableFuture.allOf(idFuture, memberFuture);

        UUID uniqueId = idFuture.join();

        if (uniqueId != null && !member.hasPermission("command.fake.mojang-bypass")) {
            member.sendMessage("§cO nick " + fakeName + " não pode ser utilizado.");
            return;
        }

        Member byName = memberFuture.join();

        if (byName != null && !member.hasPermission("command.fake.mojang-bypass")) {
            member.sendMessage("§cO nick " + fakeName + " não pode ser utilizado.");
            return;
        }

        PlayerAPI.changePlayerName(member.getPlayer(), fakeName, false);
        member.setFakeName(fakeName);
        member.setTag(member.getDefaultTag());
        member.sendMessage("§aO seu nick foi alterado para o " + fakeName + ".");

        CommonPlugin.getInstance().getPacketManager().waitPacket(SkinChangeResponse.class,
                CommonPlugin.getInstance().getPacketManager().sendPacket(
                        new SkinChangeRequest(member.getUniqueId(), CommonPlugin.getInstance().getDefaultSkin())), 500,
                packet -> {
                    PlayerAPI.changePlayerSkin(member.getPlayer(), CommonPlugin.getInstance().getDefaultSkin(), true);

                    member.setSkin(CommonPlugin.getInstance().getDefaultSkin());
                    member.setPlayerSkin(CommonConst.DEFAULT_SKIN_NAME);

                    Bukkit.getPluginManager()
                          .callEvent(new PlayerChangedSkinEvent(member, CommonPlugin.getInstance().getDefaultSkin()));
                });
    }

    @CommandFramework.Completer(name = "language", aliases = { "lang" })
    public List<String> languageCompleter(CommandArgs cmdArgs) {
        return Language.stream().map(Language::name).filter(language -> language.startsWith(
                               cmdArgs.getArgs()[cmdArgs.getArgs().length == 0 ? 0 : cmdArgs.getArgs().length - 1].toUpperCase()))
                       .collect(Collectors.toList());
    }
}
