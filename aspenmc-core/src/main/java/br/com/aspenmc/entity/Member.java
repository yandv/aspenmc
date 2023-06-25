package br.com.aspenmc.entity;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.member.Skin;
import br.com.aspenmc.entity.member.configuration.LoginConfiguration;
import br.com.aspenmc.entity.member.configuration.PreferencesConfiguration;
import br.com.aspenmc.entity.member.configuration.PunishConfiguration;
import br.com.aspenmc.entity.member.gamer.Gamer;
import lombok.Getter;
import lombok.Setter;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.permission.GroupInfo;
import br.com.aspenmc.permission.Tag;
import br.com.aspenmc.server.ServerType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public abstract class Member implements Sender {

    private final UUID uniqueId;
    private String name;

    private String fakeName;
    private String playerSkin;

    private Map<String, GroupInfo> groupMap;
    private Map<String, Long> permissions;
    private String tag;

    private final LoginConfiguration loginConfiguration;
    private final PreferencesConfiguration preferencesConfiguration;
    private final PunishConfiguration punishConfiguration;

    /*
     * Date
     */

    private long firstLoginAt;
    private long lastLoginAt;
    private long onlineTime;

    /*
     * IP Address
     */

    private String ipAddress;
    private String lastIpAddress;

    /*
     * Server
     */

    private String currentServer;
    private String currentServerType;

    private String lastServer;
    private String lastServerType;

    private boolean online;

    private transient String higherGroup;

    private transient List<String> cachedPermissions;

    @Setter
    private transient Skin skin;

    public Member(UUID uniqueId, String name, LoginConfiguration.AccountType accountType) {
        this.uniqueId = uniqueId;
        this.name = name;

        if (accountType == LoginConfiguration.AccountType.CRACKED) {
            this.playerSkin = CommonPlugin.getInstance().getDefaultSkin().getPlayerName();
        } else {
            this.playerSkin = name;
        }

        this.groupMap = new HashMap<>();
        this.permissions = new HashMap<>();
        this.tag = CommonPlugin.getInstance().getPermissionManager().getDefaultTag().map(Tag::getTagName)
                               .map(String::toLowerCase).orElse("membro");

        this.loginConfiguration = new LoginConfiguration(accountType);
        this.preferencesConfiguration = new PreferencesConfiguration();
        this.punishConfiguration = new PunishConfiguration();

        this.firstLoginAt = System.currentTimeMillis();
        this.lastLoginAt = System.currentTimeMillis();

        handleDefaultGroup(true);
    }

    public boolean isUsingCustomSkin() {
        return playerSkin != null;
    }

    public boolean isUsingFake() {
        return fakeName != null;
    }

    public void setFakeName(String fakeName) {
        this.fakeName = fakeName;
        save("fakeName");
    }

    public void setPlayerSkin(String playerSkin) {
        this.playerSkin = playerSkin;
        save("playerSkin");
    }

    public void joinServer(String server, ServerType serverType) {
        this.lastServer = this.currentServer;
        this.lastServerType = this.currentServerType;

        this.currentServer = server;
        this.currentServerType = serverType.name();
        save("lastServer", "lastServerType", "currentServer", "currentServerType");
    }

    public ServerType getCurrentServerType() {
        return Optional.ofNullable(ServerType.getByName(currentServerType)).orElse(ServerType.UNKNOWN);
    }

    public ServerType getLastServerType() {
        return Optional.ofNullable(ServerType.getByName(lastServerType)).orElse(ServerType.UNKNOWN);
    }

    public boolean setTag(Tag tag) {
        this.tag = tag.getTagName().toLowerCase();
        save("tag");
        return false;
    }

    public Optional<Tag> getTag() {
        return CommonPlugin.getInstance().getPermissionManager().getTagByName(tag);
    }

    public Group getServerGroup() {
        if (this.higherGroup == null) {
            Group defaultGroup = CommonPlugin.getInstance().getPermissionManager().getGroups().stream()
                                             .filter(group -> groupMap.containsKey(group.getGroupName().toLowerCase()))
                                             .max(Comparator.comparing(Group::getId)).orElse(null);

            if (defaultGroup != null) {
                this.higherGroup = defaultGroup.getGroupName();
            }
        }

        return CommonPlugin.getInstance().getPermissionManager().getGroupByName(higherGroup).orElse(null);
    }

    public boolean hasGroup(Group group) {
        return groupMap.containsKey(group.getGroupName().toLowerCase());
    }

    public Optional<GroupInfo> getGroupInfo(Group group) {
        return Optional.ofNullable(groupMap.get(group.getGroupName().toLowerCase()));
    }

    public GroupInfo addServerGroup(Group group, Sender sender, long expiresAt) {
        GroupInfo groupInfo = getGroupInfo(group).orElse(new GroupInfo(sender.getUniqueId(), expiresAt));

        groupInfo.setPlayerId(sender.getUniqueId());
        groupInfo.setExpiresAt(expiresAt);

        groupMap.put(group.getGroupName().toLowerCase(), groupInfo);
        setTag(getDefaultTag());
        handleDefaultGroup(true);
        return groupInfo;
    }

    public GroupInfo addServerGroup(Group group, Sender sender) {
        return addServerGroup(group, sender, -1L);
    }

    public GroupInfo setServerGroup(Group group, Sender sender) {
        groupMap.clear();
        GroupInfo groupInfo = getGroupInfo(group).orElse(new GroupInfo(sender.getUniqueId()));

        groupInfo.setPlayerId(sender.getUniqueId());

        groupMap.put(group.getGroupName().toLowerCase(), groupInfo);
        handleDefaultGroup(true);
        setTag(getDefaultTag());
        return groupInfo;
    }

    public void removeServerGroup(Group group) {
        groupMap.remove(group.getGroupName().toLowerCase());
        handleDefaultGroup(true);
        setTag(getDefaultTag());
    }

    public Collection<Group> getGroups() {
        return getGroupMap().keySet().stream().map(CommonPlugin.getInstance().getPermissionManager()::getGroupByName)
                            .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    public Collection<Tag> getTags() {
        Stream<Group> groupStream = getGroups().stream();

        return CommonPlugin.getInstance().getPermissionManager().getTags().stream()
                           .filter(tag -> hasPermission("tag." + tag.getTagName().toLowerCase()) ||
                                          groupStream.anyMatch(group -> (group.hasTag() && tag.getTagName()
                                                                                              .equals(getServerGroup().getGroupName()))))
                           .collect(Collectors.toList());
    }

    public Tag getDefaultTag() {
        return CommonPlugin.getInstance().getPermissionManager().getGroups().stream()
                           .filter(group -> groupMap.containsKey(group.getGroupName().toLowerCase()))
                           .filter(Group::hasTag).max(Comparator.comparing(Group::getId)).map(Group::getGroupTag)
                           .orElse(CommonPlugin.getInstance().getPermissionManager().getDefaultTag()).orElse(null);
    }

    public void handleDefaultGroup(boolean update) {
        boolean needUpdate = update;

        for (Group defaultGroup : CommonPlugin.getInstance().getPermissionManager().getDefaultGroups()) {
            if (!groupMap.containsKey(defaultGroup.getGroupName().toLowerCase())) {
                groupMap.put(defaultGroup.getGroupName().toLowerCase(), new GroupInfo(uniqueId, true));
                needUpdate = true;
            }
        }

        if (needUpdate) {
            higherGroup = null;
            save("groupMap");
        }
    }

    public long getSessionTime() {
        return System.currentTimeMillis() - lastLoginAt;
    }

    public long getOnlineTime() {
        return onlineTime + (online ? getSessionTime() : 0);
    }

    public void createSession(String playerName, String ipAddress) {
        this.lastLoginAt = System.currentTimeMillis();
        this.online = true;

        this.lastIpAddress = this.ipAddress;
        this.ipAddress = ipAddress;

        if (this.lastIpAddress == null) {
            this.lastIpAddress = ipAddress;
        }

        save("lastLoginAt", "lastIpAddress", "ipAddress", "online");
    }

    public void stopSession() {
        this.onlineTime += getSessionTime();
        this.lastLoginAt = System.currentTimeMillis();
        this.online = false;
        save("lastLoginAt", "onlineTime", "online");
    }

    public void loadConfiguration() {
        loginConfiguration.loadConfiguration(this);
        preferencesConfiguration.loadConfiguration(this);
        punishConfiguration.loadConfiguration(this);

        this.cachedPermissions = new ArrayList<>();
    }

    public boolean hasSilentPermission(String permission) {
        permission = permission.toLowerCase();

        if (this.permissions.containsKey(permission) || this.cachedPermissions.contains(permission)) {
            return true;
        }

        for (Group group : CommonPlugin.getInstance().getPermissionManager().getGroupMap().values().stream()
                                       .filter(group -> groupMap.containsKey(group.getGroupName().toLowerCase()))
                                       .collect(Collectors.toList())) {
            if (group.getPermissions().contains(permission)) {
                this.cachedPermissions.add(permission);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(String permission) {
        return hasSilentPermission(permission);
    }

    public abstract void sendServer(String serverId);


    public <T extends Gamer> void loadGamer(String gamerId, T gamer) {
        CommonPlugin.getInstance().getMemberManager().loadGamer(uniqueId, gamerId, gamer);
    }

    public Gamer getGamer(String gamerId) {
        return CommonPlugin.getInstance().getMemberManager().getGamerById(uniqueId, gamerId).orElse(null);
    }

    public <T extends Gamer> T getGamer(Class<T> clazz, String gamerId) {
        return CommonPlugin.getInstance().getMemberManager().getGamerById(uniqueId, gamerId).map(clazz::cast)
                           .orElse(null);
    }

    @Override
    public String getRealName() {
        return isUsingFake() ? fakeName + " (" + name + ")" : name;
    }

    public void save(String... fields) {
        CommonPlugin.getInstance().getMemberData().updateMember(this, fields);
    }

    @Override
    public boolean isPlayer() {
        return true;
    }
}
