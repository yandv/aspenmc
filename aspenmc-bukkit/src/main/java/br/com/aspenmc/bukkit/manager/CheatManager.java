package br.com.aspenmc.bukkit.manager;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.antihax.CheatCheck;
import br.com.aspenmc.bukkit.antihax.CheatType;
import br.com.aspenmc.bukkit.antihax.PlayerListener;
import br.com.aspenmc.bukkit.antihax.test.AutosoupCheck;
import br.com.aspenmc.bukkit.command.BukkitCommandFramework;
import br.com.aspenmc.bukkit.entity.PlayerData;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.packet.type.member.MemberPunishRequest;
import br.com.aspenmc.punish.PunishType;
import me.liwk.karhu.api.KarhuAPI;
import me.liwk.karhu.api.event.impl.KarhuAlertEvent;
import me.liwk.karhu.api.event.impl.KarhuInitEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CheatManager {

    private Map<UUID, PlayerData> playerDataMap;

    public CheatManager() {
        playerDataMap = new HashMap<>();
        KarhuAPI.getEventRegistry().addListener(event -> {
            if (event instanceof KarhuAlertEvent) {
                KarhuAlertEvent alertEvent = (KarhuAlertEvent) event;
                Player player = alertEvent.getPlayer();

                CheatType cheatType = CheatType.valueOf(alertEvent.getCheck().getSubCategory().name());

                int maxViolations = alertEvent.getDebug().getMaxVl();
                long ping = alertEvent.getDebug().getPing();

                getOrLoad(player.getUniqueId()).alert(cheatType, ping, maxViolations);

                event.cancel();
            } else if (event instanceof KarhuInitEvent) {
                CommonPlugin.getInstance().getPluginPlatform().runLater(() -> {
                    BukkitCommandFramework.INSTANCE.unregisterCommands("karhu");
                }, 80L);
            }
        });

        CommonPlugin.getInstance().getPluginPlatform().runAsyncTimer(() -> {
            playerDataMap.values().stream().filter(PlayerData::isToBeBanned).forEach(playerData -> {
                Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(playerData.getUniqueId())
                                            .orElse(null);

                if (member == null) return;

                long time = playerData.getToBeBanned() - System.currentTimeMillis();

                if (time <= 0) {
                    playerData.setToBeBanned(-1L);
                    CommonPlugin.getInstance().getPacketManager().sendPacket(
                            new MemberPunishRequest(playerData.getUniqueId(),
                                    CommonPlugin.getInstance().getConsoleSender(), PunishType.BAN, "Uso de Trapaças",
                                    -1L));
                    return;
                }

                if ((time / 1000L) % 15 == 0) {
                    playerData.sendBanMessage();
                }
            });
        }, 20L, 20L);

        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler(priority = EventPriority.HIGHEST)
            public void onPlayerQuit(PlayerQuitEvent event) {
                Player player = event.getPlayer();

                PlayerData playerData = playerDataMap.remove(player.getUniqueId());

                if (playerData == null || !playerData.isToBeBanned()) return;

                CommonPlugin.getInstance().getPacketManager().sendPacket(
                        new MemberPunishRequest(player.getUniqueId(), CommonPlugin.getInstance().getConsoleSender(),
                                PunishType.BAN, "Uso de Trapaças", -1L));
            }
        }, BukkitCommon.getInstance());

        registerCheck(new AutosoupCheck());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), BukkitCommon.getInstance());
    }

    public void registerCheck(CheatCheck cheatCheck) {
        Bukkit.getPluginManager().registerEvents(cheatCheck, BukkitCommon.getInstance());
    }

    public PlayerData getOrLoad(UUID uniqueId) {
        return playerDataMap.computeIfAbsent(uniqueId, PlayerData::new);
    }

    public void unloadPlayerData(UUID uniqueId) {
        playerDataMap.remove(uniqueId);
    }
}
