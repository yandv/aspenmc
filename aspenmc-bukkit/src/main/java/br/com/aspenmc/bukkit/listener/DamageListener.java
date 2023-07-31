package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.event.player.PlayerDamagePlayerEvent;
import br.com.aspenmc.bukkit.manager.CombatlogManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.metadata.MetadataValue;

public class DamageListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (event.getCause() == DamageCause.FALL) {
            if (player.hasMetadata("nofall")) {
                MetadataValue metadata = player.getMetadata("nofall").stream().findFirst().orElse(null);

                if (metadata.asLong() > System.currentTimeMillis()) event.setCancelled(true);

                metadata.invalidate();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Player damager = null;

        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            if (projectile.getShooter() instanceof Player) {
                damager = (Player) projectile.getShooter();
            }
        }

        if (damager == null) return;

        PlayerDamagePlayerEvent playerDamagePlayerEvent = new PlayerDamagePlayerEvent(player, damager,
                event.isCancelled(), event.getDamage(), event.getFinalDamage());

        Bukkit.getPluginManager().callEvent(playerDamagePlayerEvent);

        event.setCancelled(playerDamagePlayerEvent.isCancelled());
        event.setDamage(playerDamagePlayerEvent.getDamage());

        BukkitCommon.getInstance().getCombatlogManager()
                    .addCombatLog(player.getUniqueId(), new CombatlogManager.CombatLog(damager.getUniqueId()));
        BukkitCommon.getInstance().getCombatlogManager()
                    .addCombatLog(damager.getUniqueId(), new CombatlogManager.CombatLog(player.getUniqueId()));
    }
}
