package br.com.aspenmc.bukkit.event.player;

import br.com.aspenmc.bukkit.entity.BukkitMember;
import br.com.aspenmc.bukkit.event.NormalEvent;
import br.com.aspenmc.entity.member.Skin;
import lombok.Getter;

@Getter
public class PlayerChangedSkinEvent extends NormalEvent {

    private final BukkitMember member;
    private final Skin skin;

    public PlayerChangedSkinEvent(BukkitMember member, Skin skin) {
        this.member = member;
        this.skin = skin;
    }
}
