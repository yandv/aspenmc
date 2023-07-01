package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.member.gamer.Gamer;
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

@SuppressWarnings("UnstableApiUsage")
public class MemberListener implements Listener {

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uniqueId = event.getUniqueId();

        long start = System.currentTimeMillis();
        CompletableFuture<BukkitMember> byId = CommonPlugin.getInstance().getMemberData()
                                                           .loadMemberAsFutureById(uniqueId, BukkitMember.class);
        CompletableFuture<List<Gamer>> gamers = CommonPlugin.getInstance().getGamerData().loadGamer(uniqueId,
                                                                                                    BukkitCommon
                                                                                                            .getInstance()
                                                                                                            .getGamerList()
                                                                                                            .toArray(
                                                                                                                    new Map.Entry[0]));

        CompletableFuture.allOf(byId, gamers);

        BukkitMember member = byId.join();

        if (member == null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage("§cSua conta não existe.");
            return;
        }

        member.setSkin(member.getPlayerSkin().equals(CommonPlugin.getInstance().getDefaultSkin().getPlayerName()) ?
                       CommonPlugin.getInstance().getDefaultSkin() :
                       CommonPlugin.getInstance().getSkinData().loadData(member.getPlayerSkin()).orElse(null));
        member.loadConfiguration();

        List<Gamer> gamerList = gamers.join();

        for (Gamer gamer : gamerList) {
            member.loadGamer(gamer.getId(), gamer);
            CommonPlugin.getInstance()
                        .debug("The gamer " + gamer.getId() + " has been loaded for " + member.getConstraintName() +
                               ".");
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
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginM(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            CommonPlugin.getInstance().getMemberManager().unloadMember(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        CommonPlugin.getInstance().getPluginPlatform().runAsync(
                () -> CommonPlugin.getInstance().getServerData().joinPlayer(event.getPlayer().getUniqueId()));
        CommonPlugin.getInstance().getMemberManager().getMemberById(event.getPlayer().getUniqueId()).ifPresent(
                member -> member.joinServer(CommonPlugin.getInstance().getServerId(),
                                            CommonPlugin.getInstance().getServerType()));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        CommonPlugin.getInstance().getPluginPlatform().runAsync(() -> {
            CommonPlugin.getInstance().getServerData().leavePlayer(event.getPlayer().getUniqueId());
            Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(event.getPlayer().getUniqueId())
                                        .orElse(null);

            if (member == null) return;

            if (!BukkitCommon.getInstance().isSaveGamers()) {
                CommonPlugin.getInstance().getMemberManager().unloadGamer(event.getPlayer().getUniqueId());
            }

            CommonPlugin.getInstance().getMemberManager().unloadMember(event.getPlayer().getUniqueId());
        });
    }
}
