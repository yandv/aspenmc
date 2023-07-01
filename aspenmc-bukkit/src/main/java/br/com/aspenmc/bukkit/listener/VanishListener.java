package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.packet.type.member.teleport.MemberTeleportResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VanishListener implements Listener {

    private final Map<UUID, UUID> playersToTeleport = new HashMap<>();

    public VanishListener() {
        CommonPlugin.getInstance().getPacketManager().registerHandler(MemberTeleportResponse.class, response -> {
            UUID playerId = response.getUniqueId();

            Player player = Bukkit.getPlayer(playerId);

            if (player == null) {
                playersToTeleport.put(playerId, response.getTargetId());
            } else {
                if (!BukkitCommon.getInstance().getVanishManager().isPlayerInAdmin(player)) {
                    BukkitCommon.getInstance().getVanishManager().setPlayerInAdmin(player);
                }

                player.chat("/tp " + player.getName());
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinL(PlayerJoinEvent event) {
        BukkitCommon.getInstance().getVanishManager().updateVanishToPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).ifPresent(member -> {
            if (player.hasPermission(BukkitConst.PERMISION_ADMIN_MODE) &&
                (member.getPreferencesConfiguration().isAdminOnLogin() ||
                 playersToTeleport.containsKey(player.getUniqueId()))) {
                BukkitCommon.getInstance().getVanishManager().setPlayerInAdmin(player);

                if (playersToTeleport.containsKey(player.getUniqueId())) {
                    Player target = Bukkit.getPlayer(playersToTeleport.get(player.getUniqueId()));

                    if (target != null) {
                        player.chat("/tp " + target.getName());
                    }

                    playersToTeleport.remove(player.getUniqueId());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (isPlayerInAdmin(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            if (isPlayerInAdmin(event.getPlayer())) {
                event.getPlayer().performCommand("invsee " + event.getRightClicked().getName());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BukkitCommon.getInstance().getVanishManager().resetPlayer(event.getPlayer());
        playersToTeleport.remove(event.getPlayer().getUniqueId());
    }

    private boolean isPlayerInAdmin(Player player) {
        return BukkitCommon.getInstance().getVanishManager().isPlayerInAdmin(player);
    }
}
