package br.com.aspenmc.bukkit.entity;

import br.com.aspenmc.bukkit.BukkitCommon;
import br.com.aspenmc.bukkit.event.player.group.PlayerChangedGroupEvent;
import br.com.aspenmc.bukkit.event.player.language.PlayerLanguageChangeEvent;
import br.com.aspenmc.bukkit.event.player.language.PlayerLanguageChangedEvent;
import br.com.aspenmc.bukkit.event.player.tag.PlayerChangeTagEvent;
import br.com.aspenmc.bukkit.event.player.tag.PlayerChangedTagEvent;
import br.com.aspenmc.entity.Member;
import br.com.aspenmc.entity.Sender;
import br.com.aspenmc.entity.member.configuration.LoginConfiguration;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.permission.GroupInfo;
import br.com.aspenmc.permission.Tag;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class BukkitMember extends Member {

    @Setter
    private transient Player player;

    @Setter
    private transient boolean buildEnabled;

    public BukkitMember(UUID uniqueId, String name, LoginConfiguration.AccountType accountType) {
        super(uniqueId, name, accountType);
    }

    @Override
    public Language setLanguage(Language language) {
        PlayerLanguageChangeEvent playerEvent = new PlayerLanguageChangeEvent(this, language, getLanguage());

        Bukkit.getPluginManager().callEvent(playerEvent);

        if (!playerEvent.isCancelled()) {
            PlayerLanguageChangedEvent playerChangedEvent = new PlayerLanguageChangedEvent(this,
                    playerEvent.getNewLanguage());

            Bukkit.getPluginManager().callEvent(playerEvent);
            return super.setLanguage(playerEvent.getNewLanguage());
        }

        return getLanguage();
    }

    @Override
    public boolean setTag(Tag tag) {
        return setTag(tag, false);
    }

    public boolean setTag(Tag tag, boolean forcetag) {
        PlayerChangeTagEvent event = new PlayerChangeTagEvent(player, getTag().orElse(null), tag, forcetag);
        Bukkit.getPluginManager().callEvent(event);

        tag = event.getNewTag();

        if (!event.isCancelled() || forcetag) {
            PlayerChangedTagEvent change = new PlayerChangedTagEvent(player, this, getTag().orElse(null), tag,
                    forcetag);
            Bukkit.getPluginManager().callEvent(change);
            tag = change.getNewTag();
            super.setTag(tag);
        }

        return !event.isCancelled();
    }


    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            player.sendMessage(message);
        }
    }

    @Override
    public GroupInfo addServerGroup(Group group, Sender sender, long expiresAt) {
        GroupInfo groupInfo = super.addServerGroup(group, sender, expiresAt);
        Bukkit.getPluginManager().callEvent(
                new PlayerChangedGroupEvent(this, group.getGroupName(), groupInfo.getExpiresAt(),
                        groupInfo.getDuration(), PlayerChangedGroupEvent.GroupAction.ADD));
        return groupInfo;
    }

    @Override
    public GroupInfo setServerGroup(Group group, Sender sender) {
        GroupInfo groupInfo = super.setServerGroup(group, sender);
        Bukkit.getPluginManager().callEvent(
                new PlayerChangedGroupEvent(this, group.getGroupName(), groupInfo.getExpiresAt(),
                        groupInfo.getDuration(), PlayerChangedGroupEvent.GroupAction.SET));
        return groupInfo;
    }

    @Override
    public void removeServerGroup(Group group) {
        Bukkit.getPluginManager().callEvent(new PlayerChangedGroupEvent(this, group.getGroupName(), -1L, -1L,
                PlayerChangedGroupEvent.GroupAction.REMOVE));
        super.removeServerGroup(group);
    }

    @Override
    public void sendServer(String serverId) {
        if (player != null) {
            BukkitCommon.getInstance().sendPlayerToServer(player, serverId);
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        if (player != null) {
            return player.hasPermission(permission.toLowerCase()) || super.hasPermission(permission);
        }

        return super.hasPermission(permission);
    }

    @Override
    public void performCommand(String command) {
        if (player != null) {
            player.performCommand(command);
        }
    }

    @Override
    public void sendMessage(TextComponent... messages) {
        player.spigot().sendMessage(messages);
    }
}
