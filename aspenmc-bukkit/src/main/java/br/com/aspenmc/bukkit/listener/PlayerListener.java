package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.event.player.PlayerChangeLeagueEvent;
import br.com.aspenmc.bukkit.event.player.PlayerRealRespawnEvent;
import br.com.aspenmc.bukkit.utils.scoreboard.ScoreHelper;
import br.com.aspenmc.entity.sender.member.status.League;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        event.getPlayer().awardAchievement(Achievement.OPEN_INVENTORY);

        event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    @EventHandler
    public void onPlayerChangeLeague(PlayerChangeLeagueEvent event) {
        if (event.getPlayer() != null && event.getNewLeague().ordinal() > event.getOldLeague().ordinal()) {
            if (event.getNewLeague() == League.values()[League.values().length - 1]) {
                Bukkit.broadcastMessage(" ");
                Bukkit.broadcastMessage("§e" + event.getPlayer().getName() + "§f subiu para o rank " +
                        League.values()[League.values().length - 1].getColor() +
                        League.values()[League.values().length - 1].getName() + "§f!");
                Bukkit.broadcastMessage(" ");

                Bukkit.getOnlinePlayers()
                      .forEach(player -> player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 0.1f));
            }

            event.getBukkitMember().sendMessage("§a§l> §fVocê subiu para o rank §a" + event.getNewLeague().getColor() +
                    event.getNewLeague().getSymbol() + " " + event.getNewLeague().getName());
        } else if (event.getNewLeague().ordinal() < event.getOldLeague().ordinal()) {
            event.getBukkitMember().sendMessage("§c§l> §fVocê desceu para o rank §c" + event.getNewLeague().getColor() +
                    event.getNewLeague().getSymbol() + " " + event.getNewLeague().getName());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Entity killer = null;

        if (player.getKiller() != null) {
            killer = player.getKiller();
        } else if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent =
                    (EntityDamageByEntityEvent) player.getLastDamageCause();
            killer = entityDamageByEntityEvent.getDamager();
        }

        final Entity finalKiller = killer;

        CommonPlugin.getInstance().getPluginPlatform().runLater(() -> {
            player.spigot().respawn();

            PlayerRealRespawnEvent playerRealRespawnEvent = new PlayerRealRespawnEvent(player, finalKiller);
            Bukkit.getPluginManager().callEvent(playerRealRespawnEvent);

            player.teleport(playerRealRespawnEvent.getRespawnLocation());

            if (playerRealRespawnEvent.isDropXp()) {
                ExperienceOrb experienceOrb = playerRealRespawnEvent.getDropLocation().getWorld()
                                                                    .spawn(playerRealRespawnEvent.getDropLocation(),
                                                                            ExperienceOrb.class);
                experienceOrb.setExperience((int) (player.getTotalExperience() * BukkitConst.DROP_XP_PERCENTAGE));
            }

            if (playerRealRespawnEvent.isDropItems()) {
                playerRealRespawnEvent.getDrops().forEach(itemStack -> {
                    if (itemStack != null) {
                        playerRealRespawnEvent.getDropLocation().getWorld()
                                              .dropItemNaturally(playerRealRespawnEvent.getDropLocation(), itemStack);
                    }
                });
            }
        }, 3L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitListener(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        ScoreHelper.getInstance().removeScoreboard(event.getPlayer());
        Scoreboard board = event.getPlayer().getScoreboard();

        if (board != null) {
            for (Team t : board.getTeams()) {
                t.unregister();
            }

            for (Objective ob : board.getObjectives()) {
                ob.unregister();
            }
        }

        event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        removePlayerFile(event.getPlayer().getUniqueId());
    }

    private void removePlayerFile(UUID uuid) {
        if (BukkitCommon.getInstance().isRemovePlayerDat()) {
            World world = Bukkit.getWorlds().get(0);
            File folder = new File(world.getWorldFolder(), "playerdata");

            if (folder.exists() && folder.isDirectory()) {
                File file = new File(folder, uuid.toString() + ".dat");
                CommonPlugin.getInstance().getPluginPlatform().runAsyncLater(() -> {
                    if (file.exists() && !file.delete()) {
                        removePlayerFile(uuid);
                    }
                }, 2L);
            }
        }
    }
}
