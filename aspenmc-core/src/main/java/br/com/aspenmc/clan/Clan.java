package br.com.aspenmc.clan;

import br.com.aspenmc.CommonPlugin;
import br.com.aspenmc.entity.Member;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Clan {

    private static final long MAX_PLAYERS = 12;

    private final UUID clanId;

    private String clanName;
    private String clanAbbreviation;


    private long createdAt;
    private final Map<UUID, ClanMember> memberMap;

    private long maxPlayers;

    public Clan(UUID clanId, String clanName, String clanAbbreviation, Member owner) {
        this.clanId = clanId;

        this.clanName = clanName;
        this.clanAbbreviation = clanAbbreviation;

        this.createdAt = System.currentTimeMillis();

        this.memberMap = new HashMap<>();
        this.memberMap.put(owner.getUniqueId(), new ClanMember(owner.getUniqueId(), owner.getName(), ClanRole.OWNER));

        this.maxPlayers = MAX_PLAYERS;
    }

    public void setClanRole(UUID uniqueId, ClanRole role) {
        if (!isClanMember(uniqueId)) {
            return;
        }

        memberMap.get(uniqueId).setRole(role);
        save("memberMap");
    }

    public boolean isClanMember(UUID playerId) {
        return memberMap.containsKey(playerId);
    }

    public ClanRole getClanRole(UUID playerId) {
        if (!isClanMember(playerId)) {
            return null;
        }

        return memberMap.get(playerId).getRole();
    }

    public Collection<? extends Member> getOnlineMembers() {
        return memberMap.keySet().stream().map(CommonPlugin.getInstance().getMemberManager()::getMemberById)
                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    public void sendMessage(String translation, String defaultMessage, String... replaces) {
        memberMap.keySet().stream().map(CommonPlugin.getInstance().getMemberManager()::getMemberById)
                 .filter(Optional::isPresent).map(Optional::get)
                 .forEach(member -> member.sendMessage(member.t(translation, defaultMessage, replaces)));
    }

    public void sendMessage(String message) {
        memberMap.keySet().stream().map(CommonPlugin.getInstance().getMemberManager()::getMemberById)
                 .filter(Optional::isPresent).map(Optional::get).forEach(member -> member.sendMessage(message));
    }

    public void sendMessage(TextComponent... components) {
        memberMap.keySet().stream().map(CommonPlugin.getInstance().getMemberManager()::getMemberById)
                 .filter(Optional::isPresent).map(Optional::get).forEach(member -> member.sendMessage(components));
    }

    public void leave(Member sender) {
        sendMessage("clan-system.player-leave", "&c%player% saiu do clan.", "%player%", sender.getName());
        memberMap.remove(sender.getUniqueId());
        save("memberMap");
    }

    public void join(Member sender) {
        memberMap.put(sender.getUniqueId(), new ClanMember(sender.getUniqueId(), sender.getName(), ClanRole.MEMBER));
        save("memberMap");
        sendMessage("clan-system.player-join", "&a%player% entrou no clan.", "%player%", sender.getName());
    }

    public void disband() {
        sendMessage("clan-system.disband", "&cO clan foi desfeito.");
        CommonPlugin.getInstance().getMemberData().updateMany("clanId", null, memberMap.keySet().toArray(new UUID[0]));
        CommonPlugin.getInstance().getClanData().deleteClan(this);
        CommonPlugin.getInstance().getClanManager().unloadClan(clanId);
    }

    public long getMemberCount() {
        return memberMap.size();
    }

    public long getMaxPlayers() {
        return maxPlayers;
    }

    public ClanMember getOwner() {
        return memberMap.values().stream().filter(clanMember -> clanMember.getRole() == ClanRole.OWNER).findFirst()
                        .orElse(null);
    }


    public ClanMember getMemberByName(String arg) {
        return memberMap.values().stream().filter(clanMember -> clanMember.getLastName().equalsIgnoreCase(arg))
                        .findFirst().orElse(null);
    }

    public void save(String... fields) {
        CommonPlugin.getInstance().getClanData().updateClan(this, fields);
    }

    public boolean update(Member member) {
        ClanMember clanMember = memberMap.get(member.getUniqueId());

        if (clanMember == null) {
            member.setClan(null);
            return false;
        }

        clanMember.setLastName(member.getName());
        save("memberMap");
        return true;
    }
}
