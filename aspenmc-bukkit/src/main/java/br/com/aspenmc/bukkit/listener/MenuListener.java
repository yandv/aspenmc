package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.event.player.PlayerOpenInventoryEvent;
import br.com.aspenmc.bukkit.event.server.ServerTickEvent;
import br.com.aspenmc.bukkit.utils.menu.MenuHolder;
import br.com.aspenmc.bukkit.utils.menu.MenuInventory;
import br.com.aspenmc.bukkit.utils.menu.MenuItem;
import br.com.aspenmc.bukkit.utils.menu.click.ClickType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class MenuListener implements Listener {

    private final Map<Player, MenuHolder> playerMap;

    public MenuListener() {
        playerMap = new HashMap<>();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();

        if (inv.getHolder() == null || !(inv.getHolder() instanceof MenuHolder)) {
            return;
        }

        if (event.getRawSlots().stream().anyMatch(integer -> integer >= inv.getSize())) {
            event.setCancelled(true);
            return;
        }

        MenuInventory menuInventory = ((MenuHolder) inv.getHolder()).getMenu();

        event.setCancelled(
                !menuInventory.onDrag((Player) event.getWhoClicked(), event.getInventorySlots(), event.getNewItems(),
                                      event.getOldCursor(), event.getOldCursor(), event.getType()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() == null) {
            return;
        }

        Inventory inv = event.getInventory();

        if (inv.getHolder() == null || !(inv.getHolder() instanceof MenuHolder)) {
            return;
        }

        if (event.getClickedInventory() != inv || !(event.getWhoClicked() instanceof Player) || event.getSlot() <= 0) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
            event.getAction() == InventoryAction.HOTBAR_SWAP ||
            event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            event.setCancelled(true);
            return;
        }

        MenuHolder holder = (MenuHolder) inv.getHolder();
        MenuInventory menu = holder.getMenu();

        if (menu.hasItem(event.getSlot())) {
            Player p = (Player) event.getWhoClicked();
            MenuItem item = menu.getItem(event.getSlot());

            try {
                event.setCancelled(!item.getHandler()
                                        .onClick(p, inv, ClickType.from(event.getAction()), event.getCurrentItem(),
                                                 event.getSlot()));
            } catch (Exception ex) {
                event.setCancelled(true);
                CommonPlugin.getInstance().getLogger().log(java.util.logging.Level.SEVERE,
                                                            "Error while clicking on menu item " +
                                                            (item.getItemStack().hasItemMeta() ?
                                                             item.getItemStack().getItemMeta().getDisplayName() :
                                                             item.getItemStack().getType().name()) + " in menu " +
                                                            menu.getTitle(), ex);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMenuOpen(PlayerOpenInventoryEvent event) {
        if (event.getInventory() == null) {
            return;
        }

        Inventory inventory = event.getInventory();

        if (inventory.getHolder() instanceof MenuHolder) {
            MenuInventory menu = ((MenuHolder) inventory.getHolder()).getMenu();

            if (menu.getUpdateHandler() == null) {
                playerMap.remove(event.getPlayer());
            } else {
                playerMap.put(event.getPlayer(), (MenuHolder) inventory.getHolder());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerMap.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUpdate(ServerTickEvent event) {
        if (event.getType() == ServerTickEvent.UpdateType.SECOND) {
            for (Map.Entry<Player, MenuHolder> entry : playerMap.entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().getMenu().getUpdateHandler().onUpdate(entry.getKey(), entry.getValue().getMenu());
                }
            }
        }
    }
}