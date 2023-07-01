package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.event.player.PlayerRealRespawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Entity killer = null;

        if (player.getKiller() != null) {
            killer = player.getKiller();
        } else if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) player.getLastDamageCause();
            killer = entityDamageByEntityEvent.getDamager();
        }

        final Entity finalKiller = killer;

        CommonPlugin.getInstance().getPluginPlatform().runLater(() -> {
            PlayerRealRespawnEvent playerRealRespawnEvent = new PlayerRealRespawnEvent(player, finalKiller);
            Bukkit.getPluginManager().callEvent(playerRealRespawnEvent);

            player.teleport(playerRealRespawnEvent.getRespawnLocation());
            player.spigot().respawn();

            if (playerRealRespawnEvent.isDropXp()) {
                ExperienceOrb experienceOrb = playerRealRespawnEvent.getDropLocation().getWorld()
                                                                    .spawn(playerRealRespawnEvent.getDropLocation(),
                                                                           ExperienceOrb.class);
                experienceOrb.setExperience((int) (player.getTotalExperience() * BukkitConst.DROP_XP_PERCENTAGE));
            }

            if (playerRealRespawnEvent.isDropItems()) {
                playerRealRespawnEvent.getDrops().forEach(
                        itemStack -> playerRealRespawnEvent.getDropLocation().getWorld()
                                                           .dropItemNaturally(playerRealRespawnEvent.getDropLocation(),
                                                                              itemStack));
            }
        }, 3L);
    }
}
