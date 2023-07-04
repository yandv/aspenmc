package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.bukkit.event.player.PlayerMoveUpdateEvent;
import br.com.aspenmc.bukkit.event.server.ServerTickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoveListener implements Listener {

    private final Map<UUID, Location> locationMap;

    public MoveListener() {
        locationMap = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerTick(ServerTickEvent event) {
        if (event.getCurrentTick() % 5 == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (locationMap.containsKey(player.getUniqueId())) {
                    Location location = locationMap.get(player.getUniqueId());

                    if (location.getX() != player.getLocation().getX() || location.getZ() != player.getLocation().getZ()
                        || location.getY() != player.getLocation().getY()) {
                        PlayerMoveUpdateEvent playerMoveUpdateEvent = new PlayerMoveUpdateEvent(player, location,
                                                                                                player.getLocation());
                        Bukkit.getPluginManager().callEvent(playerMoveUpdateEvent);

                        if (playerMoveUpdateEvent.isCancelled()) {
                            player.teleport(
                                    new Location(location.getWorld(), location.getX(), player.getLocation().getY(),
                                                 location.getZ(), location.getYaw(), location.getPitch()));
                        }
                    }
                }

                locationMap.put(player.getUniqueId(), player.getLocation());
            }
        }
    }
}
