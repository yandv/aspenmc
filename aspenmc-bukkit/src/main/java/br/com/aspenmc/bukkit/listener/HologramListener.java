package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.event.player.PlayerMoveUpdateEvent;
import br.com.aspenmc.bukkit.event.player.PlayerRealRespawnEvent;
import br.com.aspenmc.bukkit.utils.hologram.Hologram;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class HologramListener implements Listener {

    private static final double MAX_DISTANCE = 128;

    private final Map<Player, Long> playerCooldown;

    public HologramListener() {
        playerCooldown = new HashMap<>();

        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(BukkitCommon.getInstance(), ListenerPriority.LOWEST,
                        PacketType.Play.Client.USE_ENTITY) {

                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.isCancelled()) {
                            return;
                        }


                        int entityId = event.getPacket().getIntegers().read(0);

                        Hologram hologram = BukkitCommon.getInstance().getHologramManager().getHologramById(entityId);

                        if (hologram == null) return;

                        if (hologram.getEntityId() == entityId) {
                            hologram.getTouchHandler().onTouch(hologram, event.getPlayer(),
                                    event.getPacket().getEntityUseActions().read(0) ==
                                            EnumWrappers.EntityUseAction.INTERACT);
                            playerCooldown.put(event.getPlayer(), System.currentTimeMillis() + 200L);
                        }
                    }
                });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        BukkitCommon.getInstance().getHologramManager().getHolograms().forEach(hologram -> {
            if (hologram.isBlocked(event.getPlayer())) return;

            if (hologram.getLocation().getWorld() == event.getPlayer().getLocation().getWorld()) {
                if (hologram.getLocation().distance(event.getPlayer().getLocation()) < MAX_DISTANCE) {
                    hologram.show(event.getPlayer());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRealRespawnEvent event) {
        CommonPlugin.getInstance().getPluginPlatform().runLater(() -> {
            BukkitCommon.getInstance().getHologramManager().getHolograms().forEach(hologram -> {
                if (hologram.getLocation().getWorld() == event.getPlayer().getLocation().getWorld()) {
                    if (hologram.isBlocked(event.getPlayer())) return;

                    if (hologram.isShowingForPlayer(event.getPlayer()) &&
                            hologram.getLocation().distance(event.getPlayer().getLocation()) < MAX_DISTANCE) {
                        hologram.hide(event.getPlayer());
                        hologram.show(event.getPlayer());
                    }
                }
            });
        }, 5L);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleHologramForPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMoveUpdate(PlayerMoveUpdateEvent event) {
        handleHologramForPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent event) {
        BukkitCommon.getInstance().getHologramManager().getHolograms().forEach(hologram -> {
            hologram.hide(event.getPlayer());
            hologram.unblock(event.getPlayer());
        });
    }

    private void handleHologramForPlayer(Player player) {
        BukkitCommon.getInstance().getHologramManager().getHolograms().forEach(hologram -> {
            if (hologram.isBlocked(player)) return;

            if (hologram.isShowingForPlayer(player)) {
                if (hologram.getLocation().getWorld() != player.getLocation().getWorld()) {
                    hologram.hide(player);
                } else if (hologram.getLocation().distance(player.getLocation()) > MAX_DISTANCE) {
                    hologram.hide(player);
                }
            } else {
                if (hologram.getLocation().getWorld() == player.getLocation().getWorld() &&
                        hologram.getLocation().distance(player.getLocation()) < MAX_DISTANCE) {
                    hologram.show(player);
                }
            }
        });
    }
}
