package br.com.aspenmc.bukkit.manager;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.event.player.vanish.PlayerAdminEvent;
import br.com.aspenmc.bukkit.event.player.vanish.PlayerHideToPlayerEvent;
import br.com.aspenmc.bukkit.event.player.vanish.PlayerShowToPlayerEvent;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.permission.Group;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VanishManager {

    private final Map<UUID, Group> vanishMap;
    private final Set<UUID> adminSet;

    private final Map<UUID, PlayerState> playerStateMap;

    private final Set<UUID> hideAllSet;

    public VanishManager() {
        vanishMap = new HashMap<>();
        adminSet = new HashSet<>();
        playerStateMap = new HashMap<>();
        hideAllSet = new HashSet<>();
    }

    public void hideAllPlayers(Player player) {
        hideAllSet.add(player.getUniqueId());

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            onlinePlayer.hidePlayer(player);
        }
    }

    public void showAllPlayers(Player player) {
        hideAllSet.remove(player.getUniqueId());
        updateVanishToPlayer(player);
    }

    public boolean isHidingAllPlayers(Player player) {
        return hideAllSet.contains(player.getUniqueId());
    }

    /**
     * Try to change the mode of player to admin. It will be fail if the player
     * already is in admin mode
     *
     * @param player
     */

    public boolean setPlayerInAdmin(Player player) {
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (adminSet.contains(player.getUniqueId()) || member == null) {
            return false;
        }

        PlayerAdminEvent playerAdminEvent = new PlayerAdminEvent(player, PlayerAdminEvent.AdminMode.ADMIN,
                GameMode.CREATIVE);
        Bukkit.getPluginManager().callEvent(playerAdminEvent);

        if (playerAdminEvent.isCancelled()) return false;

        if (member.getPreferencesConfiguration().isAdminRemoveItems()) {
            ItemStack[] contents = new ItemStack[player.getInventory().getContents().length + 4];

            for (int i = 0; i < playerAdminEvent.getContents().length; i++) {
                contents[i] = player.getInventory().getContents()[i];
            }

            for (int i = 0; i < playerAdminEvent.getArmorContents().length; i++) {
                contents[player.getInventory().getContents().length + i] = player.getInventory().getArmorContents()[i];
            }

            playerStateMap.put(player.getUniqueId(),
                    new PlayerState(contents, player.getGameMode(), player.getAllowFlight(), player.isFlying()));
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
        }

        adminSet.add(player.getUniqueId());
        member.getPreferencesConfiguration().setAdminModeEnabled(true);

        Group group = hidePlayer(player);
        player.sendMessage(
                "§dVocê entrou no modo admin.\n§dVocê está invísivel para " + group.getGroupName() + " e abaixo.");
        player.setGameMode(playerAdminEvent.getGameMode());

        if (playerAdminEvent.getGameMode() == GameMode.CREATIVE) {
            player.setFlying(true);
        }
        return true;
    }

    /**
     * Try to change the mode of player to player. It will be fail if the player
     * already is in player mode
     *
     * @param player
     */

    public boolean setPlayer(Player player) {
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (adminSet.contains(player.getUniqueId()) && member != null) {
            PlayerState playerState = playerStateMap.remove(player.getUniqueId());
            ItemStack[] storedContents = playerState == null ? new ItemStack[40] : playerState.getContents();

            ItemStack[] contents = Arrays.copyOfRange(storedContents, 0, player.getInventory().getContents().length);
            ItemStack[] armorContents = new ItemStack[4];

            PlayerAdminEvent playerAdminEvent = new PlayerAdminEvent(player, PlayerAdminEvent.AdminMode.PLAYER,
                    playerState == null ? GameMode.SURVIVAL : playerState.getGameMode(), contents, armorContents);
            Bukkit.getPluginManager().callEvent(playerAdminEvent);

            if (playerAdminEvent.isCancelled()) {
                return false;
            }

            adminSet.remove(player.getUniqueId());
            member.getPreferencesConfiguration().setAdminModeEnabled(false);

            showPlayer(player);
            player.sendMessage("§dVocê entrou no modo jogador.\n§dVocê está visível para todos os jogadores.");

            player.setGameMode(playerAdminEvent.getGameMode());
            player.getInventory().setContents(playerAdminEvent.getContents());
            player.getInventory().setArmorContents(playerAdminEvent.getArmorContents());
            player.setAllowFlight(playerState == null ? false : playerState.isAllowFlight());
            player.setFlying(playerState == null ? player.getAllowFlight() : playerState.isFlying());
            return false;
        }

        return true;
    }

    /**
     * Remove the player in vanish
     *
     * @param player
     */

    public void resetPlayer(Player player) {
        adminSet.remove(player.getUniqueId());
        vanishMap.remove(player.getUniqueId());
        playerStateMap.remove(player.getUniqueId());
    }

    public void showPlayer(Player player) {
        setPlayerVanishToGroup(player, null);
    }

    public Group setPlayerVanishToGroup(Player player, Group group) {
        if (group == null) {
            vanishMap.remove(player.getUniqueId());
        } else {
            vanishMap.put(player.getUniqueId(), group);
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            if (hideAllSet.contains(online.getUniqueId())) {
                if (online.canSee(player)) {
                    online.hidePlayer(player);
                }
                continue;
            }

            Member onlineP = CommonPlugin.getInstance().getMemberManager().getMemberById(online.getUniqueId())
                                         .orElse(null);

            if (onlineP == null) {
                continue;
            }

            if (group != null && (onlineP.getServerGroup().getId() <= group.getId() ||
                    !onlineP.getPreferencesConfiguration().isSpectatorsEnabled())) {
                PlayerHideToPlayerEvent event = new PlayerHideToPlayerEvent(player, online);

                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    if (!online.canSee(player)) {
                        online.showPlayer(player);
                    }
                } else if (online.canSee(player)) {
                    online.hidePlayer(player);
                }

                continue;
            }

            PlayerShowToPlayerEvent event = new PlayerShowToPlayerEvent(player, online);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                if (online.canSee(player)) {
                    online.hidePlayer(player);
                }
            } else if (!online.canSee(player)) {
                online.showPlayer(player);
            }
        }
        return group;
    }

    public void updateVanishToPlayer(Player player) {
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (member == null) return;

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            if (hideAllSet.contains(player.getUniqueId())) {
                if (player.canSee(online)) {
                    player.hidePlayer(online);
                }

                continue;
            }

            Group group = vanishMap.get(online.getUniqueId());

            if (group != null) {
                if (member.getServerGroup().getId() <= group.getId() ||
                        !member.getPreferencesConfiguration().isSpectatorsEnabled()) {
                    PlayerHideToPlayerEvent event = new PlayerHideToPlayerEvent(online, player);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        if (!player.canSee(online)) {
                            player.showPlayer(online);
                        }
                    } else if (player.canSee(online)) {
                        player.hidePlayer(online);
                    }

                    continue;
                }
            }

            PlayerShowToPlayerEvent event = new PlayerShowToPlayerEvent(online, player);

            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                if (player.canSee(online)) {
                    player.hidePlayer(online);
                }
            } else if (!player.canSee(online)) {
                player.showPlayer(online);
            }
        }
    }

    public Group hidePlayer(Player player) {
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (member == null) return null;

        return setPlayerVanishToGroup(player,
                CommonPlugin.getInstance().getPermissionManager().getFirstLowerGroup(member.getServerGroup()));
    }

    /**
     * Return the current vanished to group of player or null
     *
     * @param player the player
     * @return the group or null
     */

    public Group getVanishedToGroup(Player player) {
        return vanishMap.get(player.getUniqueId());
    }

    /**
     * Check if the id of a player is in admin mode
     *
     * @param playerId the id of player
     * @return true if is in admin mode
     */

    public boolean isPlayerInAdmin(UUID playerId) {
        return adminSet.contains(playerId);
    }

    /**
     * Check if the player is in admin mode
     *
     * @param player the player
     * @return true if is in admin mode
     */

    public boolean isPlayerInAdmin(Player player) {
        return isPlayerInAdmin(player.getUniqueId());
    }

    public Set<UUID> getPlayersInAdmin() {
        return ImmutableSet.copyOf(adminSet);
    }

    @AllArgsConstructor
    @Getter
    public static class PlayerState {

        private ItemStack[] contents;
        private GameMode gameMode;
        private boolean allowFlight;
        private boolean flying;
    }

    public boolean isPlayerVanished(UUID uniqueId) {
        return vanishMap.containsKey(uniqueId);
    }
}
