package br.com.aspenmc.entity.sender.member;

import br.com.aspenmc.CommonConst;
import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.entity.ip.IpInfo;
import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.entity.sender.member.configuration.LoginConfiguration;
import br.com.aspenmc.entity.sender.member.configuration.PreferencesConfiguration;
import br.com.aspenmc.entity.sender.member.configuration.PunishConfiguration;
import br.com.aspenmc.entity.sender.member.gamer.Gamer;
import br.com.aspenmc.language.Language;
import br.com.aspenmc.permission.Group;
import br.com.aspenmc.permission.GroupInfo;
import br.com.aspenmc.permission.Tag;
import br.com.aspenmc.server.ServerType;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public abstract class Member implements Sender {

    private final UUID uniqueId;
    private final LoginConfiguration loginConfiguration;
    private final PreferencesConfiguration preferencesConfiguration;
    private final PunishConfiguration punishConfiguration;
    private String name;
    private String fakeName;
    private String playerSkin;
    private Map<String, GroupInfo> groupMap;
    private Map<String, Long> permissions;
    private String tag;
    private UUID clanId;
    private String language;

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

    private IpInfo ipInfo;

    /*
     * Server
     */

    private String currentServer;
    private ServerType currentServerType;

    private String lastServer;
    private ServerType lastServerType;

    private boolean online;

    private transient String higherGroup;
    private transient boolean staff;

    private transient List<String> cachedPermissions;

    @Setter
    private transient Skin skin;

    public Member(UUID uniqueId, String name, LoginConfiguration.AccountType accountType) {
        this.uniqueId = uniqueId;
        this.name = name;

        if (accountType == LoginConfiguration.AccountType.CRACKED) {
            this.playerSkin = CommonConst.DEFAULT_SKIN_NAME;
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

        this.language = CommonPlugin.getInstance().getDefaultLanguage().name();

        this.firstLoginAt = System.currentTimeMillis();
        this.lastLoginAt = System.currentTimeMillis();

        handleDefaultGroup(true);
    }

    public void setIpInfo(IpInfo ipInfo) {
        this.ipInfo = ipInfo;
        save("ipInfo");
    }

    public boolean hasClan() {
        return clanId != null;
    }

    public Optional<Clan> getClan() {
        return CommonPlugin.getInstance().getClanManager().getClanById(clanId);
    }

    public void setClan(Clan clan) {
        this.clanId = clan == null ? null : clan.getClanId();
        save("clanId");
    }

    public boolean isUsingCustomSkin() {
        return !playerSkin.equals(name);
    }

    public boolean isUsingDefaultSkin() {
        return playerSkin.equals(CommonConst.DEFAULT_SKIN_NAME);
    }

    public boolean isUsingFake() {
        return fakeName != null;
    }

    public void setFakeName(String fakeName) {
        this.fakeName = fakeName;
        save("fakeName");
    }

    public void setPlayerSkin(String playerSkin) {
        if (playerSkin == null) {
            playerSkin = this.name;
        }

        this.playerSkin = playerSkin;
        save("playerSkin");
    }

    public Language getLanguage() {
        return Language.getByName(this.language);
    }

    public Language setLanguage(Language language) {
        this.language = language.name();
        save("language");
        return getLanguage();
    }

    public void joinServer(String server, ServerType serverType) {
        this.lastServer = this.currentServer;
        this.lastServerType = this.currentServerType;

        this.currentServer = server;
        this.currentServerType = serverType;
        save("lastServer", "lastServerType", "currentServer", "currentServerType");
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
                this.staff = defaultGroup.isStaff();
            }
        }

        return CommonPlugin.getInstance().getPermissionManager().getGroupByName(higherGroup).orElse(null);
    }

    public boolean hasGroup(Group group) {
        return groupMap.containsKey(group.getGroupName().toLowerCase());
    }

    public boolean hasGroup(String groupName) {
        return groupMap.containsKey(groupName.toLowerCase());
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

    public boolean hasTag(Tag tag) {
        return getDefaultTag().equals(tag) || hasPermission("tag." + tag.getTagName().toLowerCase());
    }

    public Collection<Group> getGroups() {
        return getGroupMap().keySet().stream().map(CommonPlugin.getInstance().getPermissionManager()::getGroupByName)
                            .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    public Collection<Tag> getTags() {
        Stream<Group> groupStream = getGroups().stream();

        return CommonPlugin.getInstance().getPermissionManager().getTags().stream()
                           .filter(tag -> hasPermission("tag." + tag.getTagName().toLowerCase()) ||
                                   groupStream.anyMatch(group -> (group.hasTag() &&
                                           tag.getTagName().equals(getServerGroup().getGroupName()))))
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

    public void resetHigherGroup() {
        higherGroup = null;
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

    public void loadSkin() {
        setSkin(CommonPlugin.getInstance().getDefaultSkin());

        if (!isUsingDefaultSkin()) {
            CommonPlugin.getInstance().getSkinService().loadUserData(getPlayerSkin())
                        .whenComplete((skin, throwable) -> {
                            if (skin != null) {
                                setSkin(skin);
                            }
                        });
        }
    }

    public boolean addPermission(String permission) {
        return addPermission(permission, -1L);
    }

    public boolean addPermission(String permission, long expiresAt) {
        permission = permission.toLowerCase();

        if (!this.permissions.containsKey(permission)) {
            this.permissions.put(permission, expiresAt);
            save("permissions");
            return true;
        }

        return false;
    }

    public boolean removePermission(String permission) {
        permission = permission.toLowerCase();

        if (this.permissions.containsKey(permission)) {
            this.permissions.remove(permission);
            save("permissions");
            return true;
        }

        return false;
    }

    public void clearPermissions() {
        this.permissions.clear();
        save("permissions");
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


    public <E, T extends Gamer<E>> void loadGamer(String gamerId, T gamer) {
        CommonPlugin.getInstance().getMemberManager().loadGamer(uniqueId, gamerId, gamer);
    }

    public Gamer<?> getGamer(String gamerId) {
        return CommonPlugin.getInstance().getMemberManager().getGamerById(uniqueId, gamerId).orElse(null);
    }

    public <E, T extends Gamer<E>> T getGamer(Class<T> clazz, String gamerId) {
        return CommonPlugin.getInstance().getMemberManager().getGamerById(uniqueId, gamerId).map(clazz::cast)
                           .orElse(null);
    }

    @Override
    public String getRealName() {
        return isUsingFake() ? fakeName + " (" + name + ")" : name;
    }

    public void save(String... fields) {
        CommonPlugin.getInstance().getMemberService().updateMember(this, fields);
    }

    @Override
    public boolean isPlayer() {
        return true;
    }
}