package br.com.aspenmc.bungee.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.BungeeMain;
import br.com.aspenmc.bungee.entity.BungeeMember;
import br.com.aspenmc.bungee.event.PlayerChangedGroupEvent;
import br.com.aspenmc.bungee.utils.PlayerAPI;
import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.entity.sender.member.Skin;
import br.com.aspenmc.entity.sender.member.configuration.LoginConfiguration;
import br.com.aspenmc.packet.type.server.group.GroupFieldUpdate;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MemberListener implements Listener {

    public MemberListener() {
        CommonPlugin.getInstance().getPacketManager().registerHandler(GroupFieldUpdate.class, this::onGroupFieldUpdate);
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        event.registerIntent(BungeeMain.getInstance());
        CommonPlugin.getInstance().getPluginPlatform().runAsync(() -> {
            createConnection(event);
            event.completeIntent(BungeeMain.getInstance());
        });
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        BungeeMember member = CommonPlugin.getInstance().getMemberManager()
                                          .getMemberById(player.getUniqueId(), BungeeMember.class).orElse(null);

        if (member == null) {
            player.disconnect("§cSeu perfil não foi carregado.\n§cTente iniciar uma nova conexão.");
            return;
        }

        member.setProxiedPlayer(player);
        member.getLoginConfiguration().reloadSession();

        calculatePermissions(player, member);

        if (BungeeMain.getInstance().isMaintenance() &&
                !BungeeMain.getInstance().getMaintenanceWhitelist().contains(player.getUniqueId()) &&
                (!player.hasPermission("command.admin"))) {
            player.disconnect("§cO servidor está em manutenção.\n§cTente novamente mais tarde.");
            return;
        }
    }

    @EventHandler
    public void onPermissionCheck(PermissionCheckEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer) || event.hasPermission()) {
            return;
        }

        event.setHasPermission(CommonPlugin.getInstance().getMemberManager()
                                           .getMemberById(((ProxiedPlayer) event.getSender()).getUniqueId())
                                           .map(member ->
                                                   member.hasSilentPermission(event.getPermission().toLowerCase()) ||
                                                           member.hasSilentPermission("*")).orElse(false));
    }

    @EventHandler
    public void onPlayerChangedGroup(PlayerChangedGroupEvent event) {
        calculatePermissions(event.getPlayer(), event.getMember());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        CommonPlugin.getInstance().getMemberManager().getMemberById(event.getPlayer().getUniqueId())
                    .ifPresent(member -> {
                        member.stopSession();

                        CommonPlugin.getInstance().getMemberManager().unloadMember(member.getUniqueId());
                        CommonPlugin.getInstance().getClanManager().removeInvites(member.getUniqueId());

                        member.getClan().ifPresent(clan -> {
                            if (clan.getOnlineMembers().isEmpty()) {
                                CommonPlugin.getInstance().getClanManager().unloadClan(clan.getClanId());
                            }
                        });

                        CommonPlugin.getInstance()
                                    .debug("The player " + member.getConstraintName() + " has been unloaded.");
                    });
    }

    public void createConnection(LoginEvent loginEvent) {
        UUID uniqueId = loginEvent.getConnection().getUniqueId();
        String playerName = loginEvent.getConnection().getName();

        long start = System.currentTimeMillis();
        CompletableFuture<BungeeMember> byIdFuture = CommonPlugin.getInstance().getMemberService()
                                                                 .getMemberById(uniqueId, BungeeMember.class);

        CompletableFuture<BungeeMember> byNameFuture = CommonPlugin.getInstance().getMemberService()
                                                                   .getMemberByName(playerName, BungeeMember.class,
                                                                           true);

        CompletableFuture.allOf(byIdFuture, byNameFuture);

        BungeeMember member = byIdFuture.join();
        boolean created = false;

        if (member == null) {
            Member byName = byNameFuture.join();

            if (byName == null) {
                created = true;
                member = new BungeeMember(uniqueId, playerName,
                        loginEvent.getConnection().isOnlineMode() ? LoginConfiguration.AccountType.PREMIUM :
                                LoginConfiguration.AccountType.CRACKED);
                CommonPlugin.getInstance().getMemberService().createMember(member);
                CommonPlugin.getInstance().debug("The player " + member.getConstraintName() + " has been created.");
            } else {
                if (!byName.getName().equals(playerName)) {
                    loginEvent.setCancelReason("§a");
                    loginEvent.setCancelled(true);
                    return;
                }

                member = byNameFuture.join();
            }
        } else {
            Punish currentPunish = member.getPunishConfiguration().getCurrentPunish(PunishType.BAN);

            if (currentPunish != null) {
                loginEvent.setCancelReason(currentPunish.getPunishMessage(member.getLanguage()));
                loginEvent.setCancelled(true);
                return;
            }
        }

        Skin skin = null;

        if (member.isUsingDefaultSkin()) {
            skin = CommonPlugin.getInstance().getDefaultSkin();
        } else if (member.isUsingCustomSkin()) {
            skin = CommonPlugin.getInstance().getSkinService().loadData(member.getPlayerSkin())
                               .orElse(CommonPlugin.getInstance().getDefaultSkin());
        } else {
            skin = PlayerAPI.getPlayerSkin(loginEvent.getConnection(), CommonPlugin.getInstance().getDefaultSkin());
            CommonPlugin.getInstance().getSkinService().save(skin);
        }

        try {
            PlayerAPI.changePlayerSkin(loginEvent.getConnection(), skin);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            CommonPlugin.getInstance().getLogger()
                        .log(java.util.logging.Level.SEVERE, "Error while changing the player skin.", ex);
        }

        member.setSkin(skin);

        if (member.hasClan()) {
            BungeeMember finalMember = member;

            Clan clan = CommonPlugin.getInstance().getClanManager().getClanById(finalMember.getUniqueId()).orElse(null);

            if (clan == null) {
                CommonPlugin.getInstance().getClanService().getClanById(member.getClanId(), Clan.class)
                            .whenComplete((c, throwable) -> {
                                if (throwable != null) {
                                    throwable.printStackTrace();
                                    finalMember.sendMessage(
                                            "§cO servidor não conseguiu carregar o seu clan, tentaremos novamente " +
                                                    "mais tarde.");
                                    return;
                                }

                                if (c == null) {
                                    finalMember.setClan(null);
                                    return;
                                }

                                if (c.update(finalMember)) CommonPlugin.getInstance().getClanManager().loadClan(c);
                            });
            } else {
                clan.update(finalMember);
            }
        }

        SocketAddress socket = loginEvent.getConnection().getSocketAddress();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socket;
        String ipAddress = inetSocketAddress.getHostString();

        Member targetMember = member;

        CommonPlugin.getInstance().getGeoipService().getIp(ipAddress).whenComplete((ipInfo, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            if (ipInfo == null) {
                targetMember.setIpInfo(null);
                return;
            }

            targetMember.setIpInfo(ipInfo);
        });

        member.createSession(playerName, ipAddress);

        member.loadConfiguration();
        CommonPlugin.getInstance().getMemberManager().loadMember(member);
        CommonPlugin.getInstance().debug("The player " + member.getConstraintName() + " has been loaded (" +
                (System.currentTimeMillis() - start) + "ms).");
    }

    private void calculatePermissions(ProxiedPlayer player, BungeeMember member) {
        if (player.getPermissions() != null) {
            for (String permission : ImmutableList.copyOf(player.getPermissions())) {
                player.setPermission(permission, false);
            }
        }

        for (String string : member.getPermissions().keySet()) {
            player.setPermission(string.toLowerCase(), true);
        }

        for (Group group : member.getGroups()) {
            for (String string : group.getPermissions()) {
                player.setPermission(string.toLowerCase(), true);
            }
        }
    }

    private void onGroupFieldUpdate(GroupFieldUpdate packet) {
        CommonPlugin.getInstance().getMemberManager().getMembers(BungeeMember.class).stream()
                    .filter(member -> member.hasGroup(packet.getGroupName()))
                    .filter(member -> member.getProxiedPlayer() != null).forEach(member -> {
                        calculatePermissions(member.getProxiedPlayer(), member);
                    });
    }
}
