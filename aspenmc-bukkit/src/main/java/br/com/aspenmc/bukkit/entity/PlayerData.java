package br.com.aspenmc.bukkit.entity;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.antihax.CheatType;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.packet.type.discord.MessageRequest;
import br.com.aspenmc.utils.string.StringFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerData {

    private final UUID uniqueId;

    private final Map<CheatType, AlertData> alertMap;

    @Setter
    private double fallDistance;
    @Setter
    private double distanceY;

    @Setter
    private double deltaX = 0, deltaY = 0, deltaZ = 0;

    @Setter
    private double lastDeltaX = 0, lastDeltaY = 0, lastDeltaZ = 0;

    @Setter
    private double jumpHeight = 0;

    @Setter
    private boolean isFalling;
    @Setter
    private boolean isGoingUp;

    @Setter
    private boolean onGround;

    @Setter
    private boolean climbing;

    @Setter
    private boolean isInVehicle;

    @Setter
    private boolean sprinting, sneaking, surrondingBlocks, swimming;

    @Setter
    private boolean inventoryOpened;

    @Setter
    private int aboveBlocksTicks;

    @Setter
    private int waterTicks;

    @Setter
    private int ping, lastPing, deltaPing;

    @Setter
    private long lastWindowClick, lastAttack, lastFlying, lastTeleport;

    @Setter
    private long toBeBanned = -1L;

    public PlayerData(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.alertMap = new HashMap<>();
    }

    public boolean isToBeBanned() {
        return toBeBanned != -1L;
    }

    public void alert(CheatType cheatType, long ping, int maxViolations) {
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(uniqueId).orElse(null);

        if (member == null) return;

        AlertData alertData = getOrLoad(cheatType);
        int violation = alertData.count();

        if (alertData.needAlert()) {
            int alertViolation = alertData.alert();

            CommonPlugin.getInstance().getMemberManager().getMembers().stream()
                        .filter(m -> m.hasPermission("command.alert"))
                        .filter(m -> m.getPreferencesConfiguration().isAlertsEnabled()).forEach(m -> m.sendMessage(
                                m.t("spectrum.hax-alert", "%player%", member.getRealName(), "%cheat%",
                                        cheatType.name(), "%ping%",
                                        "" + ping, "%violation%", "" + alertViolation)));
            MessageRequest.sendAnticheatMessage(member.getName(), cheatType.name(),
                    CommonPlugin.getInstance().getServerId(),
                    "O jogador " + member.getName() + " está usando " + cheatType.name() + " (" + alertViolation + ")");
        }

        if (violation == maxViolations) {
            startPunishProcess();
        }
    }

    public void startPunishProcess() {
        sendBanMessage();
        toBeBanned = System.currentTimeMillis() + (1000L * 60L);
    }

    public void sendBanMessage() {
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(uniqueId).orElse(null);

        if (member == null) return;

        CommonPlugin.getInstance().getMemberManager().getMembers().stream()
                    .filter(m -> m.hasPermission("command.alert"))
                    .filter(m -> m.getPreferencesConfiguration().isSeeingStafflogsEnabled()).forEach(m -> m.sendMessage(
                            m.t("spectrum.hax-ban", "%player%", member.getRealName(), "%cheat%", "%time%",
                                    StringFormat.formatTime((toBeBanned - System.currentTimeMillis()) / 1000L))));
    }

    public AlertData getOrLoad(CheatType cheatType) {
        return alertMap.computeIfAbsent(cheatType, v -> new AlertData());
    }

    public static class AlertData {

        private int count;
        private int alertCount;
        private long lastAlert;

        public int count() {
            if (lastAlert + (1000L * 60L * 60L * 3) < System.currentTimeMillis()) {
                count = 0;
                alertCount = 0;
            }

            return ++count;
        }

        public int alert() {
            lastAlert = System.currentTimeMillis();
            return ++alertCount;
        }

        public boolean needAlert() {
            return lastAlert + 250L < System.currentTimeMillis();
        }
    }
}