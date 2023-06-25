package br.com.aspenmc.bungee.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.BungeeMain;
import br.com.aspenmc.bungee.entity.BungeeMember;
import br.com.aspenmc.bungee.utils.PlayerAPI;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.Skin;
import br.com.aspenmc.entity.member.configuration.LoginConfiguration;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.punish.Punish;
import br.com.aspenmc.punish.PunishType;
import br.com.aspenmc.utils.string.StringFormat;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.CommandSender;
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

        calculatePermissions(member);
    }

    @EventHandler
    public void onPermissionCheck(PermissionCheckEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer) || event.hasPermission()) {
            return;
        }

        CommandSender sender = event.getSender();
        Member member = CommonPlugin.getInstance().getMemberManager()
                                    .getMemberById(((ProxiedPlayer) sender).getUniqueId()).orElse(null);

        if (member == null) {
            return;
        }

        String permission = sender.getPermissions().stream().filter(string -> string.equals("*")).findFirst()
                                  .orElse(null);

        if (permission == null) {
            event.setHasPermission(member.hasSilentPermission(event.getPermission()));
        } else {
            event.setHasPermission(true);
        }
    }

    private void calculatePermissions(BungeeMember member) {
        ProxiedPlayer player = member.getProxiedPlayer();

        if (player == null) {
            CommonPlugin.getInstance()
                        .debug("The player " + member.getName() + " is null when trying to handle permissions.");
            return;
        }

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

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        CommonPlugin.getInstance().getMemberManager().getMemberById(event.getPlayer().getUniqueId())
                    .ifPresent(member -> {
                        member.stopSession();

                        CommonPlugin.getInstance().getMemberManager().unloadMember(member.getUniqueId());
                        CommonPlugin.getInstance()
                                    .debug("The player " + member.getConstraintName() + " has been unloaded.");
                    });
    }

    public void createConnection(LoginEvent loginEvent) {
        UUID uniqueId = loginEvent.getConnection().getUniqueId();
        String playerName = loginEvent.getConnection().getName();

        long start = System.currentTimeMillis();
        CompletableFuture<BungeeMember> byIdFuture = CommonPlugin.getInstance().getMemberData()
                                                                 .loadMemberAsFutureById(uniqueId, BungeeMember.class);

        CompletableFuture<BungeeMember> byNameFuture = CommonPlugin.getInstance().getMemberData()
                                                                   .loadMemberAsFutureByName(playerName,
                                                                                             BungeeMember.class, true);

        CompletableFuture.allOf(byIdFuture, byNameFuture);

        BungeeMember member = byIdFuture.join();
        boolean created = false;

        if (member == null) {
            Member byName = byNameFuture.join();

            if (byName == null) {
                created = true;
                member = new BungeeMember(uniqueId, playerName, loginEvent.getConnection().isOnlineMode() ?
                                                                LoginConfiguration.AccountType.PREMIUM :
                                                                LoginConfiguration.AccountType.CRACKED);
                CommonPlugin.getInstance().getMemberData().createMember(member);
                CommonPlugin.getInstance().debug("The player " + member.getConstraintName() + " has been created.");
            } else {
                if (!byName.getName().equals(playerName)) {
                    loginEvent.setCancelReason("§akkkk");
                    loginEvent.setCancelled(true);
                    return;
                }

                member = byNameFuture.join();
            }
        } else {
            Punish currentPunish = member.getPunishConfiguration().getCurrentPunish(PunishType.BAN);

            if (currentPunish != null) {
                if (currentPunish.isPermanent()) {
                    loginEvent.setCancelReason(currentPunish.getPunishMessage());
                } else {
                    loginEvent.setCancelReason(currentPunish.getPunishMessage());
                }

                loginEvent.setCancelled(true);
                return;
            }
        }

        if (member.isUsingCustomSkin()) {
            Skin skin = member.getPlayerSkin().equals(CommonPlugin.getInstance().getDefaultSkin().getPlayerName()) ?
                        CommonPlugin.getInstance().getDefaultSkin() :
                        CommonPlugin.getInstance().getSkinData().loadData(member.getPlayerSkin())
                                    .orElse(CommonPlugin.getInstance().getDefaultSkin());

            try {
                PlayerAPI.changePlayerSkin(loginEvent.getConnection(), skin);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                CommonPlugin.getInstance().getLogger()
                            .log(java.util.logging.Level.SEVERE, "Error while changing the player skin.", ex);
            }

            member.setSkin(skin);
        }

        SocketAddress socket = loginEvent.getConnection().getSocketAddress();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socket;
        String ipAddress = inetSocketAddress.getHostString();

        member.createSession(playerName, ipAddress);

        member.loadConfiguration();
        CommonPlugin.getInstance().getMemberManager().loadMember(member);
        CommonPlugin.getInstance().debug("The player " + member.getConstraintName() + " has been loaded (" +
                                         (System.currentTimeMillis() - start) + "ms).");
    }
}
