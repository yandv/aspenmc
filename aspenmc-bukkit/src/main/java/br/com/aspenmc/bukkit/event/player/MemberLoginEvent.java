package br.com.aspenmc.bukkit.event.player;

import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.PlayerEvent;
import lombok.Getter;

public class MemberLoginEvent extends PlayerEvent {

    @Getter
    private BukkitMember member;

    public MemberLoginEvent(BukkitMember member) {
        super(member.getPlayer());
    }


}
