package br.com.aspenmc.bukkit.event.player;

import lombok.Getter;
import me.minehurt.bukkit.event.PlayerEvent;
import me.minehurt.bukkit.member.BukkitMember;
import org.bukkit.entity.Player;

public class MemberLoginEvent extends PlayerEvent {

    @Getter
    private BukkitMember member;

    public MemberLoginEvent(BukkitMember member) {
        super(member.getPlayer());
    }


}
