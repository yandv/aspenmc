package br.com.aspenmc.bukkit.antihax.test;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.antihax.CheatCheck;
import br.com.aspenmc.bukkit.entity.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AutosoupCheck extends CheatCheck {

    private Map<UUID, Long> time;

    public AutosoupCheck() {
        time = new HashMap<>();
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        if (!event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || event.getCurrentItem() == null ||
                !event.getCurrentItem().getType().equals(Material.MUSHROOM_SOUP)) {
            return;
        }

        time.put(event.getWhoClicked().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || !event.getItem().getType().equals(Material.MUSHROOM_SOUP)) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = BukkitCommon.getInstance().getCheatManager().getOrLoad(player.getUniqueId());

        if (playerData.isInventoryOpened() && System.currentTimeMillis() - playerData.getLastWindowClick() <= 15) {
            alert(player, "soup with inventory opened");
            return;
        }

        if (time.containsKey(player.getUniqueId())) {
            long spentTime = System.currentTimeMillis() - time.get(player.getUniqueId());

            if (spentTime <= 10) {
                alert(player, "soup in " + spentTime + "ms");
            }

            time.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        time.remove(event.getPlayer().getUniqueId());
    }
}