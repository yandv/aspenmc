package br.com.aspenmc.bukkit.event.player;

import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.PlayerEvent;
import br.com.aspenmc.clan.Clan;
import br.com.aspenmc.clan.ClanTag;
import lombok.Getter;

@Getter
public class PlayerClanTagUpdateEvent extends PlayerEvent {

    private final BukkitMember member;
    private final Clan clan;

    private boolean oldClanDisplayTagEnabled;
    private boolean newClanDisplayTagEnabled;

    private ClanTag oldClanTag;
    private ClanTag newClanTag;

    public PlayerClanTagUpdateEvent(BukkitMember member, Clan clan, boolean oldClanDisplayTagEnabled,
            boolean newClanDisplayTagEnabled, ClanTag oldClanTag, ClanTag newClanTag) {
        super(member.getPlayer(), false);
        this.member = member;
        this.clan = clan;
        this.oldClanDisplayTagEnabled = oldClanDisplayTagEnabled;
        this.newClanDisplayTagEnabled = newClanDisplayTagEnabled;
        this.oldClanTag = oldClanTag;
        this.newClanTag = newClanTag;
    }
}
