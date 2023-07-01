package br.com.aspenmc.bukkit.event.player.group;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.PlayerCancellableEvent;
import br.com.aspenmc.permission.Group;
import lombok.Getter;

import java.util.Optional;

@Getter
public class PlayerChangedGroupEvent extends PlayerCancellableEvent {

    private BukkitMember member;

    private String groupName;
    private long expiresAt;
    private long duration;

    private GroupAction action;

    public PlayerChangedGroupEvent(BukkitMember member, String groupName, long expiresAt, long duration, GroupAction action) {
        super(member.getPlayer());
        this.member = member;
        this.groupName = groupName;
        this.expiresAt = expiresAt;
        this.duration = duration;
        this.action = action;
    }

    public Optional<Group> getGroup() {
        return CommonPlugin.getInstance().getPermissionManager().getGroupByName(groupName);
    }

    public enum GroupAction {
        ADD,
        SET,
        REMOVE
    }
}
