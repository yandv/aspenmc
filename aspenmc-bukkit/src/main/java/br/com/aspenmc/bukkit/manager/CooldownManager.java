package br.com.aspenmc.bukkit.manager;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.event.player.cooldown.CooldownFinishEvent;
import br.com.aspenmc.bukkit.event.player.cooldown.CooldownStartEvent;
import br.com.aspenmc.bukkit.event.player.cooldown.CooldownStopEvent;
import br.com.aspenmc.bukkit.event.server.ServerTickEvent;
import br.com.aspenmc.bukkit.utils.PlayerAPI;
import br.com.aspenmc.bukkit.utils.ProgressBar;
import br.com.aspenmc.bukkit.utils.cooldown.Cooldown;
import br.com.aspenmc.bukkit.utils.cooldown.ItemCooldown;
import br.com.aspenmc.utils.string.StringFormat;
import br.com.aspenmc.utils.string.TimeFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store and display like actionbar for players all Cooldown Based on
 * https://gitlab.com/Battlebits/ CooldownAPI
 *
 * @author Battlebits developers
 */

public class CooldownManager implements Listener {

    private Map<UUID, List<Cooldown>> map;
    private Listener listener;

    public CooldownManager() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * Add cooldown to player
     *
     * @param player
     * @param cooldown
     */

    public void addCooldown(Player player, Cooldown cooldown) {
        CooldownStartEvent event = new CooldownStartEvent(player, cooldown);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            List<Cooldown> list = map.computeIfAbsent(player.getUniqueId(), v -> new ArrayList<>());

            boolean add = true;

            for (Cooldown cool : list) {
                if (cool.getName().equals(cooldown.getName())) {
                    cool.update(cooldown.getDuration(), cooldown.getStartTime());
                    add = false;
                }
            }

            if (add) list.add(cooldown);

            if (!map.isEmpty()) registerListener();
        }
    }

    /**
     * Add cooldown to player
     *
     * @param uuid     Player UUID
     * @param name     Cooldown name
     * @param duration Cooldown duration
     */

    public void addCooldown(UUID uuid, String name, long duration) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) return;

        Cooldown cooldown = new Cooldown(name, duration);

        CooldownStartEvent event = new CooldownStartEvent(player, cooldown);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            List<Cooldown> list = map.computeIfAbsent(player.getUniqueId(), v -> new ArrayList<>());

            boolean add = true;

            for (Cooldown cool : list) {
                if (cool.getName().equals(cooldown.getName())) {
                    cool.update(cooldown.getDuration(), cooldown.getStartTime());
                    add = false;
                }
            }

            if (add) list.add(cooldown);

            if (!map.isEmpty()) registerListener();
        }
    }

    private void registerListener() {
        if (listener == null) {
            listener = new CooldownListener();
            Bukkit.getPluginManager().registerEvents(listener, BukkitCommon.getInstance());
        }
    }

    /**
     * Remove player cooldown
     *
     * @param player Player
     * @param name   Cooldown name
     */

    public boolean removeCooldown(Player player, String name) {
        if (map.containsKey(player.getUniqueId())) {
            List<Cooldown> list = map.get(player.getUniqueId());
            Iterator<Cooldown> it = list.iterator();
            while (it.hasNext()) {
                Cooldown cooldown = it.next();

                if (cooldown.getName().equals(name)) {
                    it.remove();
                    Bukkit.getPluginManager().callEvent(new CooldownStopEvent(player, cooldown));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if player has cooldown
     *
     * @param player
     * @param name
     * @return boolean
     */

    public boolean hasCooldown(Player player, String name) {
        if (map.containsKey(player.getUniqueId())) {
            List<Cooldown> list = map.get(player.getUniqueId());
            for (Cooldown cooldown : list)
                if (cooldown.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * Check if uniqueId has cooldown
     *
     * @param uniqueId
     * @param name
     * @return
     */

    public boolean hasCooldown(UUID uniqueId, String name) {
        if (map.containsKey(uniqueId)) {
            List<Cooldown> list = map.get(uniqueId);
            for (Cooldown cooldown : list)
                if (cooldown.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * Return the cooldown of player, if the player not have cooldown will return
     * null
     *
     * @param uniqueId
     * @param name
     * @return
     */

    public Cooldown getCooldown(UUID uniqueId, String name) {
        if (map.containsKey(uniqueId)) {
            List<Cooldown> list = map.get(uniqueId);

            for (Cooldown cooldown : list)
                if (cooldown.getName().equals(name)) return cooldown;
        }
        return null;
    }

    public void clearCooldown(Player player) {
        if (map.containsKey(player.getUniqueId())) map.remove(player.getUniqueId());
    }

    public class CooldownListener implements Listener {

        @EventHandler
        public void onUpdate(ServerTickEvent event) {
            if (event.getType() != ServerTickEvent.UpdateType.TICK) return;

            if (event.getCurrentTick() % 5 == 0) return;

            for (UUID uuid : map.keySet()) {
                Player player = Bukkit.getPlayer(uuid);

                if (player != null) {
                    List<Cooldown> list = map.get(uuid);
                    Iterator<Cooldown> it = list.iterator();

                    /* Found Cooldown */
                    Cooldown found = null;
                    while (it.hasNext()) {
                        Cooldown cooldown = it.next();

                        if (!cooldown.expired()) {
                            if (cooldown instanceof ItemCooldown) {
                                ItemStack hand = player.getItemInHand();
                                if (hand != null && hand.getType() != Material.AIR) {
                                    ItemCooldown item = (ItemCooldown) cooldown;
                                    if (hand.equals(item.getItem())) {
                                        item.setSelected(true);
                                        found = item;
                                        break;
                                    }
                                }

                                continue;
                            }
                            found = cooldown;
                            continue;
                        }

                        it.remove();

                        CooldownFinishEvent e = new CooldownFinishEvent(player, cooldown);
                        Bukkit.getServer().getPluginManager().callEvent(e);
                    }

                    /* Display Cooldown */
                    if (found != null) {
                        display(player, found);
                    } else if (list.isEmpty()) {
                        PlayerAPI.actionbar(player, " ");
                        map.remove(uuid);
                    } else {
                        Cooldown cooldown = list.get(0);

                        if (cooldown instanceof ItemCooldown) {
                            ItemCooldown item = (ItemCooldown) cooldown;

                            if (item.isSelected()) {
                                item.setSelected(false);
                                PlayerAPI.actionbar(player, " ");
                            }
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onCooldown(CooldownStopEvent event) {
            if (map.isEmpty()) {
                HandlerList.unregisterAll(listener);
                listener = null;
            }
        }

        private void display(Player player, Cooldown cooldown) {
            PlayerAPI.actionbar(player, "§f" + cooldown.getName() + " " +
                    ProgressBar.getProgressBar(cooldown.getRemaining(), cooldown.getDuration(), 20, '|',
                            ChatColor.GREEN, ChatColor.RED) + " §f" +
                    StringFormat.formatTime((int) cooldown.getRemaining(), TimeFormat.NORMAL));
        }
    }
}
