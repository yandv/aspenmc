package br.com.aspenmc.manager;

import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.entity.sender.Sender;
import br.com.aspenmc.entity.sender.member.Member;
import br.com.aspenmc.utils.IndexableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClanManager {

    private IndexableMap<UUID, Clan> clanMap;
    private Map<UUID, Map<UUID, ClanInvite>> clanInviteMap;

    public ClanManager() {
        this.clanMap = new IndexableMap<>();
        this.clanInviteMap = new HashMap<>();
    }

    public void invite(Sender sender, Member target, ClanInvite clanInvite) {
        this.clanInviteMap.computeIfAbsent(target.getUniqueId(), uuid -> new HashMap<>())
                          .put(sender.getUniqueId(), clanInvite);
    }

    public void removeInvite(Member member, UUID inviter) {
        if (!clanInviteMap.containsKey(member.getUniqueId())) {
            return;
        }

        clanInviteMap.get(member.getUniqueId()).remove(inviter);

        if (clanInviteMap.get(member.getUniqueId()).isEmpty()) {
            clanInviteMap.remove(member.getUniqueId());
        }
    }

    public void removeInvites(UUID playerId) {
        clanInviteMap.remove(playerId);
    }


    public ClanInvite getInvite(Sender target, UUID inviter) {
        return clanInviteMap.getOrDefault(target.getUniqueId(), new HashMap<>()).get(inviter);
    }

    public boolean hasInvite(Sender target, UUID inviter) {
        if (!clanInviteMap.containsKey(target.getUniqueId())) {
            return false;
        }

        return clanInviteMap.get(target.getUniqueId()).containsKey(inviter);
    }

    public void loadClan(Clan clan) {
        clanMap.put(clan.getClanId(), clan);
    }

    public Optional<Clan> getClanById(UUID clanId) {
        return Optional.ofNullable(clanMap.get(clanId));
    }

    public void unloadClan(UUID clanId) {
        clanMap.remove(clanId);
    }

    public int indexOf(Clan clan) {
        return clanMap.indexOf(clan.getClanId());
    }

    public interface ClanInvite {

        void result(boolean accept);
    }
}
