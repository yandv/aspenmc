package br.com.aspenmc.bukkit.command.register;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.player.PlayerChangedSkinEvent;
import br.com.aspenmc.bukkit.utils.PlayerAPI;
import br.com.aspenmc.command.CommandArgs;
import br.com.aspenmc.command.CommandFramework;
import br.com.aspenmc.command.CommandHandler;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.Skin;
import br.com.aspenmc.packet.type.member.skin.SkinChangeRequest;
import br.com.aspenmc.packet.type.member.skin.SkinChangeResponse;
import org.bukkit.Bukkit;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ProfileCommand implements CommandHandler {

    @CommandFramework.Command(name = "profile", console = false)
    public void profileCommand(CommandArgs cmdArgs) {
//        new YourProfileInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer());
    }

    @CommandFramework.Command(name = "skin", runAsync = true, console = false)
    public void skinCommand(CommandArgs cmdArgs) {
        BukkitMember member = cmdArgs.getSenderAsMember(BukkitMember.class);
        String[] args = cmdArgs.getArgs();

        if (args.length == 0) {
//            new SkinInventory(cmdArgs.getSenderAsMember(BukkitMember.class).getPlayer());
            return;
        }

        String skinName = args[0];

        if (!CommonConst.NAME_PATTERN.matcher(skinName).matches()) {
            member.sendMessage("§cO nick " + skinName + " não é válido.");
            return;
        }

        Skin skin = CommonPlugin.getInstance().getSkinData().loadData(skinName).orElse(null);

        if (skin == null) {
            member.sendMessage("§cNenhuma skin encontrada para o usuário " + skinName + ".");
            return;
        }

        CommonPlugin.getInstance().getPacketManager().waitPacket(SkinChangeResponse.class,
                                                                 CommonPlugin.getInstance().getPacketManager()
                                                                             .sendPacket(new SkinChangeRequest(
                                                                                     member.getUniqueId(),
                                                                                     CommonPlugin.getInstance()
                                                                                                 .getDefaultSkin())),
                                                                 500, packet -> {
                    if (packet == null) {
                        member.sendMessage("§cNão foi possível alterar sua skin.");
                        return;
                    }

                    if (packet.getSkinResult() == SkinChangeResponse.SkinResult.SUCCESS) {
                        PlayerAPI.changePlayerSkin(member.getPlayer(), skin, true);
                        Bukkit.getPluginManager().callEvent(new PlayerChangedSkinEvent(member, skin));
                        member.setPlayerSkin(skinName);
                        member.setSkin(skin);
                        member.sendMessage("§aSua skin foi alterada para " + skinName + ".");
                    } else {
                        member.sendMessage("§cOcorreu um erro desconhecido ao alterar sua skin.");
                    }
                });
    }

    @CommandFramework.Command(name = "fakereset", aliases = {"nick.#", "fake.#"}, permission = "command.fake",
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

    @CommandFramework.Command(name = "fake", aliases = {"nick"}, permission = "command.fake", runAsync = true,
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
                                                                       .loadMemberAsFutureByName(fakeName, true);

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

        PlayerAPI.changePlayerName(member.getPlayer(), fakeName, true);
        member.setFakeName(fakeName);
        member.setTag(member.getDefaultTag());
        member.sendMessage("§aO seu nick foi alterado para o " + fakeName + ".");

        CommonPlugin.getInstance().getPacketManager().waitPacket(SkinChangeResponse.class,
                                                                 CommonPlugin.getInstance().getPacketManager()
                                                                             .sendPacket(new SkinChangeRequest(
                                                                                     member.getUniqueId(),
                                                                                     CommonPlugin.getInstance()
                                                                                                 .getDefaultSkin())),
                                                                 500, packet -> {
                    PlayerAPI.changePlayerSkin(member.getPlayer(), CommonPlugin.getInstance().getDefaultSkin(), true);

                    member.setSkin(CommonPlugin.getInstance().getDefaultSkin());
                    member.setPlayerSkin(CommonPlugin.getInstance().getDefaultSkin().getPlayerName());

                    Bukkit.getPluginManager()
                          .callEvent(new PlayerChangedSkinEvent(member, CommonPlugin.getInstance().getDefaultSkin()));
                });
    }
}
