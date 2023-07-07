package br.com.aspenmc.bukkit.listener;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.event.player.group.PlayerChangedGroupEvent;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.permission.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionListener implements Listener {

    private final Map<UUID, PermissionAttachment> attachments;

    public PermissionListener() {
        this.attachments = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(PlayerLoginEvent event) {
        updateAttachment(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginMonitor(PlayerLoginEvent event) {
        if (event.getResult() != Result.ALLOWED) {
            removeAttachment(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChangedGroup(PlayerChangedGroupEvent event) {
        removeAttachment(event.getPlayer());
        updateAttachment(event.getPlayer());
    }

    public void updateAttachment(Player player) {
        Member member = CommonPlugin.getInstance().getMemberManager().getMemberById(player.getUniqueId()).orElse(null);

        if (member == null) {
            return;
        }

        PermissionAttachment attach = attachments.get(player.getUniqueId());
        Permission playerPerm = getCreateWrapper(player.getUniqueId().toString());

        if (attach == null) {
            attach = player.addAttachment(BukkitCommon.getInstance());
            attachments.put(player.getUniqueId(), attach);
            attach.setPermission(playerPerm, true);
        } else {
            attach.getPermissions().clear();
            attach.setPermission(playerPerm, true);
        }

        playerPerm.getChildren().clear();

        for (Group group : member.getGroups())
            for (String perm : group.getPermissions())
                if (!playerPerm.getChildren().containsKey(perm)) {
                    playerPerm.getChildren().put(perm, true);
                }

        for (String perm : member.getPermissions().keySet())
            if (!playerPerm.getChildren().containsKey(perm)) {
                playerPerm.getChildren().put(perm, true);
            }

        player.recalculatePermissions();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        removeAttachment(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent event) {
        removeAttachment(event.getPlayer());
    }

    protected void removeAttachment(Player player) {
        PermissionAttachment attach = (PermissionAttachment) this.attachments.remove(player.getUniqueId());

        if (attach != null) {
            attach.remove();
        }

        Bukkit.getPluginManager().removePermission(player.getUniqueId().toString());
    }

    private Permission getCreateWrapper(String name) {
        Permission perm = Bukkit.getPluginManager().getPermission(name);

        if (perm == null) {
            perm = new Permission(name, "Server Internal Permission", PermissionDefault.FALSE);
            Bukkit.getPluginManager().addPermission(perm);
        }

        return perm;
    }
}
