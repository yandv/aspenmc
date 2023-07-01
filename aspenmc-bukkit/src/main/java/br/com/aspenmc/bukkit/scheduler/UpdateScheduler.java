package br.com.aspenmc.bukkit.scheduler;

import br.com.aspenmc.BukkitConst;
import br.com.aspenmc.bukkit.event.server.ServerTickEvent;
import org.bukkit.Bukkit;

public class UpdateScheduler implements Runnable {

    private long currentTick;

    @Override
    public void run() {
        currentTick++;
        Bukkit.getPluginManager().callEvent(new ServerTickEvent(ServerTickEvent.UpdateType.TICK, currentTick));

        if (currentTick % BukkitConst.TPS == 0) {
            Bukkit.getPluginManager().callEvent(new ServerTickEvent(ServerTickEvent.UpdateType.SECOND, currentTick));
        }

        if (currentTick % (BukkitConst.TPS * 60) == 0) {
            Bukkit.getPluginManager().callEvent(new ServerTickEvent(ServerTickEvent.UpdateType.MINUTE, currentTick));
        }
    }
}