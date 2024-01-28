package br.com.aspenmc.bukkit.command.register;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.player.PlayerChangedSkinEvent;
import br.com.aspenmc.bukkit.event.player.PlayerClanTagUpdateEvent;
import br.com.aspenmc.bukkit.menu.profile.YourLanguageInventory;
import br.com.aspenmc.bukkit.menu.profile.YourProfileInventory;
import br.com.aspenmc.bukkit.menu.profile.YourStatisticsInventory;
import br.com.aspenmc.bukkit.menu.skin.SkinInventory;
import br.com.aspenmc.bukkit.menu.staff.account.UserInventory;
import br.com.aspenmc.bukkit.utils.PlayerAPI;
import br.com.aspenmc.bukkit.utils.ProgressBar;
import br.com.aspenmc.clan.ClanTag;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.entity.sender.member.Skin;
import br.com.aspenmc.entity.sender.member.configuration.LoginConfiguration;
import br.com.aspenmc.entity.sender.member.status.League;
import br.com.aspenmc.entity.sender.member.status.Status;
import br.com.aspenmc.entity.sender.member.status.StatusType;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.manager.PermissionManager;
import br.com.aspenmc.packet.type.member.skin.SkinChangeRequest;
import br.com.aspenmc.packet.type.member.skin.SkinChangeResponse;
import br.com.aspenmc.permission.GroupInfo;
import br.com.aspenmc.permission.Tag;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ProfileCommand implements CommandHandler {

    @CommandFramework.Command(name = "profile", aliases = { "perfil" }, runAsync = true)
    public void profileCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            if (sender.isPlayer()) {
                CommonPlugin.getInstance().getPluginPlatform().runSync(
                        () -> new YourProfileInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer()));
            } else {
                sender.sendMessage(sender.t("command.account.usage", "%label%", cmdArgs.getLabel()));
            }
            return;
        }

        if (sender.hasPermission(CommonConst.SERVER_FULL_PERMISSION) && !sender.getName().equals(args[0])) {
            sender.sendMessage("§cVocê não tem permissão para executar esse comando.");
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage(sender.t("account-not-found", "%player%", args[0]));
            return;
        }

        if (sender.isPlayer()) {
            CommonPlugin.getInstance().getPluginPlatform().runSync(
                    () -> new UserInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer(), member));
        } else {
            sender.sendMessage("em breve... nos cinemas");
        }
    }

    @CommandFramework.Command(name = "account", aliases = { "acc" }, runAsync = true)
    public void accountCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            if (sender.isPlayer()) {
                handleProfileInfo(sender, cmdArgs.getSenderAsMember(BukkitMember.class));
            } else {
                sender.sendMessage(sender.t("command.account.usage", "%label%", cmdArgs.getLabel()));
            }

            return;
        }

        if (!sender.hasPermission(CommonConst.SERVER_FULL_PERMISSION) && !sender.getName().equals(args[0])) {
            sender.sendMessage("§cVocê não tem permissão para executar esse comando.");
            return;
        }

        Member target = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (target == null) {
            sender.sendMessage(sender.t("account-not-found", "%player%", args[0]));
            return;
        }

        handleProfileInfo(sender, target);
    }

    public void handleProfileInfo(Sender sender, Member target) {
        boolean hasSuperPermission = sender.hasPermission(CommonConst.SERVER_FULL_PERMISSION) ||
                target.getUniqueId().equals(sender.getUniqueId());
        sender.sendMessage(
                "§aUsuário " + target.getName() + " (" + target.getLoginConfiguration().getAccountType().name() + ")");
        sender.sendMessage("  §fPrimeiro login: §a" + CommonConst.FULL_DATE_FORMAT.format(target.getFirstLoginAt()));
        sender.sendMessage("  §fÚltimo login: §a" + CommonConst.FULL_DATE_FORMAT.format(target.getLastLoginAt()));
        sender.sendMessage("  §fRank: " + target.getServerGroup().getGroupTag().map(Tag::getColoredName)
                                                .orElse(PermissionManager.NULL_TAG.getColoredName()));
        sender.sendMessage("    §7Adicionado em " + CommonConst.FULL_DATE_FORMAT.format(
                target.getGroupInfo(target.getServerGroup()).map(GroupInfo::getCreatedAt).orElse(-1L)));

        if (hasSuperPermission) {
            if (target.getIpInfo() == null) {
                sender.sendMessage("  §fLocalização: ");
                sender.sendMessage("    §7País: Brazil");
                sender.sendMessage("    §7Estado: Unknown");
                sender.sendMessage("    §7Cidade: Unknown");
            } else {
                sender.sendMessage("  §fLocalização: ");
                sender.sendMessage("    §7País: " + target.getIpInfo().getCountry());
                sender.sendMessage("    §7Estado: " + target.getIpInfo().getRegion());
                sender.sendMessage("    §7Cidade: " + target.getIpInfo().getCity());
            }
        }
    }

    @CommandFramework.Command(name = "rank", aliases = { "ranks", "liga", "ligas" }, runAsync = true)
    public void rankCommand(CommandArgs cmdArgs) {
        if (!cmdArgs.isPlayer()) return;

        Member player = cmdArgs.getSenderAsMember();
        String[] args = cmdArgs.getArgs();

        StatusType statusType = StatusType.getByServer(CommonPlugin.getInstance().getServerType());

        if (statusType == null || (args.length > 0 && args[0].equalsIgnoreCase("list"))) {
            player.sendMessage("");
            player.sendMessage("§aLigas:");
            player.sendMessage("");

            player.sendMessage("§4Masterpiece§f: §4☬§f.");
            player.sendMessage("§cSupremo§f: §c♆I§f, §c♆II§f.");
            player.sendMessage("§5Lenda§f: §5♅I§f, §5♅II§f.");
            player.sendMessage("§dMestre§f: §d✫I§f, §d✫II§f.");
            player.sendMessage("§2Esmeralda§f: §2✥I§f, §2✥II§f, §2✥III§f.");
            player.sendMessage("§bDiamante§f: §b✦I§f, §b✦II§f, §b✦III§f.");
            player.sendMessage("§6Ouro§f: §6✳I§f, §6✳II§f, §6✳III§f, §6✳IV§f, §6✳V§f.");
            player.sendMessage("§7Prata§f: §7✶I§f, §7✶II§f, §7✶III§f, §7✶IV§f, §7✶V§f.");
            player.sendMessage("§8Intermediário§f: §8☰");
            player.sendMessage("§8Novato§f: §8-");
            player.sendMessage("");
            player.sendMessage("§aPara ver sua liga em um modo de jogo utilize: /rank (hg)");
            return;
        }

        try {
            statusType = StatusType.valueOf(args[0].toUpperCase());
        } catch (Exception ex) {
            player.sendMessage("§cUtilize: /rank (hg)");
            return;
        }

        Status status = CommonPlugin.getInstance().getStatusManager().getOrLoadById(player.getUniqueId(), statusType);

        League currentLeague = status.getLeague();
        int currentXp = status.getXp();

        player.sendMessage("§aSua liga: " + currentLeague.getColoredConstraint());
        player.sendMessage("§aSeu xp: §f" + currentXp);

        if (currentLeague == League.values()[League.values().length - 1]) {
            player.sendMessage("");
            player.sendMessage("§aVocê está no maior rank do servidor");
            player.sendMessage("§aContinue ganhando XP para ficar no topo do ranking");
        } else {
            int totalLeagueXp = status.getLeague().getTotalXp(currentXp);
            double percent = (totalLeagueXp * 100.0D) / currentLeague.getMaxXp();

            player.sendMessage("§aXP para a próxima liga §7[" +
                    ProgressBar.getProgressBar(totalLeagueXp, currentLeague.getMaxXp(), 20, '┃', ChatColor.GREEN,
                            ChatColor.GRAY) + "§7] §7" + CommonConst.DECIMAL_FORMAT.format(percent) + "§7%");
        }
    }

    @CommandFramework.Command(name = "addxp", permission = CommonConst.SERVER_FULL_PERMISSION, runAsync = true)
    public void addXpCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length < 2) {
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                    " <player> <statusType> <xp>§f para adicionar xp a um jogador.");
            return;
        }


        StatusType statusType = null;

        try {
            statusType = StatusType.valueOf(args[1].toUpperCase());
        } catch (Exception ex) {
            sender.sendMessage("§cStatusType não encontrado.");
            return;
        }

        int xp = 0;

        try {
            xp = Integer.parseInt(args[2]);
        } catch (Exception ex) {
            sender.sendMessage("§cXP inválido.");
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return;
        }

        Status status = CommonPlugin.getInstance().getStatusManager().getOrLoadById(member.getUniqueId(), statusType);

        status.addXp(xp);
        sender.sendMessage(
                "§aVocê adicionou " + xp + " de xp para o jogador " + member.getName() + " no modo de jogo " +
                        statusType.name() + ".");
    }

    @CommandFramework.Command(name = "removexp", permission = CommonConst.SERVER_FULL_PERMISSION, runAsync = true)
    public void removeXpCommand(CommandArgs cmdArgs) {
        Sender sender = cmdArgs.getSender();
        String[] args = cmdArgs.getArgs();

        if (args.length < 2) {
            sender.sendMessage(" §a» §fUse §a/" + cmdArgs.getLabel() +
                    " <player> <statusType> <xp>§f para adicionar xp a um jogador.");
            return;
        }


        StatusType statusType = null;

        try {
            statusType = StatusType.valueOf(args[1].toUpperCase());
        } catch (Exception ex) {
            sender.sendMessage("§cStatusType não encontrado.");
            return;
        }

        int xp = 0;

        try {
            xp = Integer.parseInt(args[2]);
        } catch (Exception ex) {
            sender.sendMessage("§cXP inválido.");
            return;
        }

        Member member = CommonPlugin.getInstance().getMemberManager().getOrLoadByName(args[0]).orElse(null);

        if (member == null) {
            sender.sendMessage("§cJogador não encontrado.");
            return;
        }

        Status status = CommonPlugin.getInstance().getStatusManager().getOrLoadById(member.getUniqueId(), statusType);

        status.removeXp(xp);
        sender.sendMessage(
                "§aVocê adicionou " + xp + " de xp para o jogador " + member.getName() + " no modo de jogo " +
                        statusType.name() + ".");
    }

    @CommandFramework.Command(name = "statistics", aliases = { "stats", "status" }, console = false)
    public void statisticsCommand(CommandArgs cmdArgs) {
        new YourStatisticsInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer(), null);
    }

    @CommandFramework.Command(name = "clantag", console = false)
    public void clantagCommand(CommandArgs cmdArgs) {
        BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            member.sendMessage(member.getLanguage().t("command.clantag.usage", "%label%", cmdArgs.getLabel()));
            return;
        }

        if (args[0].equalsIgnoreCase("ativar") || args[0].equalsIgnoreCase("enable")) {
            if (member.getPreferencesConfiguration().isClanDisplayTagEnabled()) {
                member.sendMessage(member.getLanguage().t("command.clantag.already-enabled"));
                return;
            }

            member.getPreferencesConfiguration().setClanDisplayTagEnabled(true);
            member.sendMessage(member.getLanguage().t("command.clantag.enabled"));

            if (member.hasClan()) {
                Bukkit.getPluginManager().callEvent(
                        new PlayerClanTagUpdateEvent(member, member.getClan().orElse(null), false, true,
                                member.getPreferencesConfiguration().getClanTag(),
                                member.getPreferencesConfiguration().getClanTag()));
            }
        } else if (args[0].equalsIgnoreCase("desativar") || args[0].equalsIgnoreCase("disable")) {
            if (!member.getPreferencesConfiguration().isClanDisplayTagEnabled()) {
                member.sendMessage(member.getLanguage().t("command.clantag.already-disabled"));
                return;
            }

            member.getPreferencesConfiguration().setClanDisplayTagEnabled(false);
            member.sendMessage(member.getLanguage().t("command.clantag.disabled"));
            Bukkit.getPluginManager().callEvent(
                    new PlayerClanTagUpdateEvent(member, member.getClan().orElse(null), true, false,
                            member.getPreferencesConfiguration().getClanTag(),
                            member.getPreferencesConfiguration().getClanTag()));
        } else {
            ClanTag clanTag = ClanTag.getByName(args[0]);

            if (clanTag == null) {
                member.sendMessage(member.getLanguage().t("command.clantag.clantag-not-found", "%tag%", args[0]));
                return;
            }

            if (member.getPreferencesConfiguration().getClanTag() == clanTag) {
                member.sendMessage(
                        member.getLanguage().t("command.clantag.clantag-already-set", "%tag%", clanTag.name()));
                return;
            }

            if (!member.hasPermission("clantag." + clanTag.name().toLowerCase()) && clanTag != ClanTag.NONE) {
                member.sendMessage(
                        member.getLanguage().t("command.clantag.clantag-not-permission", "%tag%", clanTag.name()));
                return;
            }

            ClanTag oldClanTag = member.getPreferencesConfiguration().getClanTag();

            member.getPreferencesConfiguration().setClanTag(clanTag);
            member.sendMessage(member.getLanguage().t("command.clantag.clantag-set", "%tag%", clanTag.name()));

            if (member.hasClan()) {
                Bukkit.getPluginManager().callEvent(new PlayerClanTagUpdateEvent(member, member.getClan().orElse(null),
                        member.getPreferencesConfiguration().isClanDisplayTagEnabled(),
                        member.getPreferencesConfiguration().isClanDisplayTagEnabled(), oldClanTag, clanTag));
            }
        }
    }

    @CommandFramework.Command(name = "language", aliases = { "lang" }, console = false)
    public void languageCommand(CommandArgs cmdArgs) {
        Member member = cmdArgs.getSenderAsMember();
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
            new YourLanguageInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer(), null);
            return;
        }

        Language language = Language.getByName(Joiner.on(' ').join(Arrays.copyOfRange(args, 0, args.length)));

        if (language == null) {
            member.sendMessage(member.getLanguage().t("command.language.language-not-found", "%language%", args[0]));
            return;
        }

        if (language == member.getLanguage()) {
            member.sendMessage(member.getLanguage().t("command.language.language-already-set", "%language%",
                    language.getLanguageName()));
            return;
        }

        language = member.setLanguage(language);
        member.sendMessage(
                member.getLanguage().t("command.language.language-changed", "%language%", language.getLanguageName()));
    }

    @CommandFramework.Command(name = "skin", aliases = { "pele" }, runAsync = true, console = false)
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

        Skin skin = CommonPlugin.getInstance().getSkinService().loadData(skinName).orElse(null);

        if (skin == null) {
            member.sendMessage(member.t("command.skin.skin-not-found", "%player%", skinName));
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
                        member.sendMessage(member.t("skin-changed-sucess", "%player%", skinName));
                    } else {
                        member.sendMessage("§c" + packet.getErrorMessage());
                    }
                });
    }

    @CommandFramework.Command(name = "skin.#", runAsync = true, console = false)
    public void skinresetCommand(CommandArgs cmdArgs) {
        BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);

        Skin newSkin = member.getLoginConfiguration().getAccountType() == LoginConfiguration.AccountType.PREMIUM ?
                CommonPlugin.getInstance().getSkinService().loadData(member.getName())
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
                        member.sendMessage(member.t("skin-changed-to-default-sucess", "%player%", member.getName()));
                    } else {
                        member.sendMessage("§c" + packet.getErrorMessage());
                    }
                });
    }

    @CommandFramework.Command(name = "fakereset", aliases = { "nick.#", "fake.#" }, permission = "command.fake",
            runAsync = true, console = false)
    public void fakeResetCommand(CommandArgs cmdArgs) {
        BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);
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
        CompletableFuture<? extends Member> memberFuture = CommonPlugin.getInstance().getMemberService()
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

    @CommandFramework.Completer(name = "clantag")
    public List<String> clantagCompleter(CommandArgs cmdArgs) {
        List<String> completes = Arrays.asList("ativar", "desativar");

        for (ClanTag clanTag : ClanTag.values()) {
            if (cmdArgs.getSender().hasPermission("clantag." + clanTag.name().toLowerCase())) {
                completes.add(clanTag.name());
            }
        }


        return completes.stream().filter(complete -> complete.startsWith(
                                cmdArgs.getArgs()[cmdArgs.getArgs().length == 0 ? 0 : cmdArgs.getArgs().length - 1].toUpperCase()))
                        .collect(Collectors.toList());
    }
}
