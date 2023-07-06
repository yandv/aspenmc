package br.com.aspenmc.bukkit.entity;

import br.com.aspenmc.CommonPlatform;
import br.com.aspenmc.bukkit.BukkitCommon;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitPlatform implements CommonPlatform {

    @Override
    public void broadcast(String... messages) {
        for (String message : messages) {
            Bukkit.broadcastMessage(message);
        }
    }

    @Override
    public void broadcast(TextComponent... components) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(components);
        }
    }

    @Override
    public String getNameById(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        return player == null ? null : player.getName();
    }

    @Override
    public UUID getUniqueId(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        return player == null ? null : player.getUniqueId();
    }

    @Override
    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(BukkitCommon.getInstance(), runnable);
    }

    @Override
    public void runAsyncTimer(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(BukkitCommon.getInstance(), runnable, delay, period);
    }

    @Override
    public void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(BukkitCommon.getInstance(), runnable);
    }

    @Override
    public void runLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(BukkitCommon.getInstance(), runnable, delay);
    }

    @Override
    public void runTimer(Runnable runnable, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(BukkitCommon.getInstance(), runnable, delay, period);
    }
}
