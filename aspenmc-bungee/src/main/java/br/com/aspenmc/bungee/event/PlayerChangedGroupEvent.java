package br.com.aspenmc.bungee.event;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.bungee.entity.BungeeMember;
import br.com.aspenmc.permission.Group;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

import java.util.Optional;

@Getter
public class PlayerChangedGroupEvent extends Event implements Cancellable {

    private final ProxiedPlayer player;
    private BungeeMember member;

    private String groupName;
    private long expiresAt;
    private long duration;

    private GroupAction action;

    @Getter
    @Setter
    private boolean cancelled;

    public PlayerChangedGroupEvent(BungeeMember member, String groupName, long expiresAt, long duration, GroupAction action) {
        this.player = member.getProxiedPlayer();
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
