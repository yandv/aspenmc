package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.gamer.Gamer;
import br.com.aspenmc.entity.member.status.Status;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MemberListener implements Listener {

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uniqueId = event.getUniqueId();

        long start = System.currentTimeMillis();
        CompletableFuture<BukkitMember> byId = CommonPlugin.getInstance().getMemberService()
                                                           .getMemberById(uniqueId, BukkitMember.class);
        CompletableFuture<List<? extends Gamer<Player>>> gamers = CommonPlugin.getInstance().getGamerService()
                                                                              .loadGamer(uniqueId,
                                                                                      BukkitCommon.getInstance()
                                                                                                  .getGamerList()
                                                                                                  .toArray(
                                                                                                          new Map.Entry[0]));
        CompletableFuture<List<Status>> loadStatus = CommonPlugin.getInstance().getStatusService().getStatusById(uniqueId,
                BukkitCommon.getInstance().getPreloadedStatus());

        CompletableFuture.allOf(byId, gamers, loadStatus);

        BukkitMember member = byId.join();

        if (member == null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("§cSua conta não existe.");
            return;
        }

        member.loadSkin();
        member.loadConfiguration();

        List<? extends Gamer<Player>> gamerList = gamers.join();

        for (Gamer<Player> gamer : gamerList) {
            member.loadGamer(gamer.getId(), gamer);
            CommonPlugin.getInstance()
                        .debug("The gamer " + gamer.getId() + " has been loaded for " + member.getConstraintName() +
                                ".");
        }

        for (Status status : loadStatus.join()) {
            CommonPlugin.getInstance().getStatusManager().loadStatus(status);
            CommonPlugin.getInstance().debug("The status " + status.getStatusType().name() + " has been loaded for " +
                    member.getConstraintName() + ".");
        }

        if (member.hasClan()) {
            if (!CommonPlugin.getInstance().getClanManager().getClanById(member.getClanId()).isPresent()) {
                CommonPlugin.getInstance().getClanManager().loadClan(
                        CommonPlugin.getInstance().getClanService().getClanById(member.getClanId(), Clan.class).join());
            }
        }

        CommonPlugin.getInstance().getMemberManager().loadMember(member);
        CommonPlugin.getInstance().debug("The player " + member.getConstraintName() + " has been loaded (" +
                (System.currentTimeMillis() - start) + "ms).");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        BukkitMember member = CommonPlugin.getInstance().getMemberManager()
                                          .getMemberById(player.getUniqueId(), BukkitMember.class).orElse(null);

        if (member == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                    "§cSeu perfil não foi carregado.\n§cTente iniciar uma nova conexão.");
            return;
        }

        member.setPlayer(player);
        CommonPlugin.getInstance().getMemberManager().getGamers(player.getUniqueId(), Player.class)
                    .forEach(gamer -> gamer.loadEntity(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginM(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            CommonPlugin.getInstance().getMemberManager().unloadMember(event.getPlayer().getUniqueId());
            return;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        CommonPlugin.getInstance().getPluginPlatform().runAsync(
                () -> CommonPlugin.getInstance().getServerService().joinPlayer(event.getPlayer().getUniqueId()));
        CommonPlugin.getInstance().getMemberManager().getMemberById(event.getPlayer().getUniqueId()).ifPresent(
                member -> member.joinServer(CommonPlugin.getInstance().getServerId(),
                        CommonPlugin.getInstance().getServerType()));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        CommonPlugin.getInstance().getPluginPlatform().runAsync(() -> {
            CommonPlugin.getInstance().getServerService().leavePlayer(event.getPlayer().getUniqueId());
            Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(event.getPlayer().getUniqueId())
                                        .orElse(null);

            if (member == null) return;

            if (!BukkitCommon.getInstance().isSaveGamers()) {
                CommonPlugin.getInstance().getMemberManager().unloadGamer(event.getPlayer().getUniqueId());
            }

            CommonPlugin.getInstance().getMemberManager().unloadMember(event.getPlayer().getUniqueId());
            CommonPlugin.getInstance().getStatusManager().unloadStatus(event.getPlayer().getUniqueId());

            member.getClan().ifPresent(clan -> {
                if (clan.getOnlineMembers().isEmpty()) {
                    CommonPlugin.getInstance().getClanManager().unloadClan(clan.getClanId());
                }
            });
        });
    }
}
